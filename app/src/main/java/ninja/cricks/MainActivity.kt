package ninja.cricks

import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ninja.cricks.customviews.CircleImageView
import ninja.cricks.databinding.ActivityMainBinding
import ninja.cricks.models.JoinedMatchModel
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RetrofitClient
import ninja.cricks.network.WebServiceClient
import ninja.cricks.roomDatabase.ResponseDatabase
import ninja.cricks.ui.BaseActivity
import ninja.cricks.ui.dashboard.*
import ninja.cricks.ui.home.HomeFragment
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    FragmentDrawer.FragmentDrawerListener {

    public var resGetMessage = MutableLiveData<JsonObject>()
    var fragment: Fragment? = null
    private var mBinding: ActivityMainBinding? = null
    private lateinit var mContext: Context
    private var drawerFragment: FragmentDrawer? = null
    var resCheckinArrayList = ArrayList<UpcomingMatchesModel>()
    var resLiveCheckinArraylist = ArrayList<JoinedMatchModel>()
    var resCompletedMatchesCheckinArraylist = ArrayList<JoinedMatchModel>()

    companion object {
        var menuArrayList = ArrayList<JSONObject>()
        var showScore: Boolean = false
        var CHECK_WALLET_ONCE: Boolean? = false
        var updatedApkUrl: String = ""
        var releaseNote: String = ""
        var CHECK_APK_UPDATE_API: Boolean = false
        var CHECK_FORCE_UPDATE: Boolean = true

        const val ID_HOME = 1
        const val ID_DASHBOARD = 2
        const val ID_PREDICT_WIN = 3
        const val ID_MY_ACCOUNT = 4
        const val ID_NOTIFICATIONS = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        mContext = this
        userInfo = (application as NinjaApplication).userInformations
        setSupportActionBar(mBinding!!.toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mBinding!!.imgWalletAmount.setOnClickListener {
            val intent = Intent(mContext, MyBalanceActivity::class.java)
            startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        }
        mBinding!!.notificationId.setOnClickListener {
            val intent = Intent(mContext, NotificationListActivity::class.java)
            startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        }

      //  getWalletBalances()
        setProfileData()
        updateCheckApk()
        getMessage()

      ///  mBinding!!.navigation.setOnNavigationItemSelectedListener(this)

        fragment = HomeFragment()
        loadFragment()

        drawerFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_navigation_drawer) as FragmentDrawer?
        drawerFragment!!.setUp(
            R.id.fragment_navigation_drawer,
            mBinding!!.drawerLayout,
            mBinding!!.toolbar
        )
        drawerFragment!!.setDrawerListener(this)

        mBinding!!.profileImage.setOnClickListener {
            mBinding!!.drawerLayout.visibility = View.VISIBLE
            mBinding!!.drawerLayout.openDrawer(Gravity.LEFT)
        }
        initBottomNavigation()
    }

    private fun initBottomNavigation() {
        mBinding?.navigation?.apply {

            add(
                MeowBottomNavigation.Model(
                    ID_HOME,
                    R.drawable.ic_home_black_24dp,
                )
            )

            add(
                MeowBottomNavigation.Model(
                    ID_DASHBOARD,
                    R.drawable.ic_dashboard_black_24dp
                )
            )
            add(
                MeowBottomNavigation.Model(
                    ID_MY_ACCOUNT,
                    R.drawable.ic_wallet_new
                )
            )
            add(
                MeowBottomNavigation.Model(
                    ID_NOTIFICATIONS,
                    R.drawable.ic_more_horiz_black_24dp
                )
            )
/*
            add(
                MeowBottomNavigation.Model(
                    temp_leaderboard,
                    R.drawable.king
                )
            )
*/

            //setCount(ID_NOTIFICATION, "115")

            setOnShowListener {
                val name = when (it.id) {
                    ID_HOME -> {
                        fragment = HomeFragment()
                        loadFragment()
                    }
                    ID_DASHBOARD -> {
                        fragment = MyMatchesFragment()
                        loadFragment()
                    }
                    ID_MY_ACCOUNT -> {
                        fragment = MyAccountFragment()
                        loadFragment()
                    }
                    ID_NOTIFICATIONS -> {
                        fragment = MoreOptionsFragment()
                        loadFragment()
                    }
/*
                    temp_leaderboard -> {
                        startActivity(Intent(this@MainActivity,ContestLeaderBoardActivity::class.java))
                    }
*/
                    else -> ""
                }

                //xxtvSelected.text = getString(R.string.main_page_selected, name)
            }

            setOnClickMenuListener {
                val name = when (it.id) {
                    ID_HOME -> "HOME"
                    ID_DASHBOARD -> "EXPLORE"
                    ID_PREDICT_WIN -> "MESSAGE"
                    ID_NOTIFICATIONS -> "NOTIFICATION"
                    ID_MY_ACCOUNT -> "ACCOUNT"
                    else -> ""
                }
            }

            setOnReselectListener {
                //  Toast.makeText(this@MainActivity, "item ${it.id} is reselected.", Toast.LENGTH_LONG).show()
            }

            show(ID_HOME)

        }

    }


    override fun onResume() {
        super.onResume()
        userInfo = (application as NinjaApplication).userInformations
        if (userInfo != null) {
            Glide.with(this)
                .load(userInfo!!.profileImage)
                .placeholder(R.drawable.player_blue)
                .into(mBinding!!.profileImage)
        }
    }

    fun viewUpcomingMatches() {
       // mBinding!!.navigation.selectedItemId = R.id.navigation_home
        mBinding!!.navigation.show(ID_HOME,true)
        fragment = FixtureCricketFragment()
        loadFragment()
    }

    fun viewAllMatches() {
       // mBinding!!.navigation.selectedItemId = R.id.navigation_dashboard
        mBinding!!.navigation.show(ID_DASHBOARD,true)
        fragment = MyMatchesFragment()
        loadFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyBalanceActivity.REQUEST_CODE_ADD_MONEY) {
            getWalletBalances()
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {

    }

    override fun onStart() {
        super.onStart()
        if (CHECK_APK_UPDATE_API) {
            CHECK_APK_UPDATE_API = false

            val intent = Intent(this@MainActivity, UpdateApplicationActivity::class.java)
            intent.putExtra(UpdateApplicationActivity.REQUEST_CODE_APK_UPDATE, updatedApkUrl)
            intent.putExtra(UpdateApplicationActivity.REQUEST_RELEASE_NOTE, releaseNote)
            startActivity(intent)
        }
    }

    private fun getMessage() {
        val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(this,
            (Constant.getMessagesDatabaseId)
        )
        if (lastTimeApiCall!!+ Constant.delayApiSeconds < System.currentTimeMillis()) {
            // if (activity != null && isAdded) {
            getMessageApiCall()
            //   }
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(this@MainActivity).responseDao().getResponseJsonObject(
                    (Constant.getMessagesDatabaseId)
                )

                if (value != null && value.type == (Constant.getMessagesDatabaseId)){
                    withContext(Dispatchers.Main){getMessage2(value.res)}
                }
                else {
                    withContext(Dispatchers.Main){
                            getMessageApiCall()
                    }
                }
            }
        }


    }

    private fun getMessageApiCall() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            return
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this))
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this))
        jsonRequest.addProperty("version_code", BuildConfig.VERSION_CODE)

        WebServiceClient(this).client.create(IApiMethod::class.java)
            .getMessages(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    Log.d("api", "failed")
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                        val resObje = response!!.body().toString()
                        val jsonObject = JSONObject(resObje)
                        if (jsonObject.optBoolean("status")) {
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main){ getMessage2(response.body()!!) }
                                withContext(Dispatchers.IO){
                                    MyPreferences.saveLastTimeForApiCall(this@MainActivity,Constant.getMessagesDatabaseId, System.currentTimeMillis())
                                    ResponseDatabase.getInstance(this@MainActivity).responseDao().saveResponseJsonObject(ninja.cricks.roomDatabase.ResponseJsonObject(
                                        (Constant.getMessagesDatabaseId),System.currentTimeMillis(),
                                        response.body()!!
                                    ))
                                }
                            }
                        }
                }
            })
    }

    private fun getMessage2(resObje: JsonObject) {
        resGetMessage.value = resObje
    }


    private fun getWalletBalances() {
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    CHECK_APK_UPDATE_API = false
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.walletObjects
                            if (responseModel != null) {
                                MyPreferences.setRazorPayId(this@MainActivity, res.razorPay)
                                MyPreferences.setShowPaytm(this@MainActivity, res.paytm_show)
                                MyPreferences.setShowGpay(this@MainActivity, res.gpay_show)
                                MyPreferences.setShowRazorPay(this@MainActivity, res.rozarpay_show)

                                MyPreferences.setShowPaytmWithdraw(
                                    this@MainActivity,
                                    res.paytm_withdrawal
                                )
                                MyPreferences.setShowBankWithdraw(
                                    this@MainActivity,
                                    res.bank_withdrawal
                                )
                                MyPreferences.setShowUPIWithdraw(
                                    this@MainActivity,
                                    res.upi_withdrawal
                                )

                                MyPreferences.setMinWithdrawal(
                                    this@MainActivity,
                                    res.minWithdrawal
                                )

                                MyPreferences.setPaytmWithdrawBtn(
                                    this@MainActivity,
                                    res.paytm_withdrawal_btn
                                )

                                (application as NinjaApplication).saveWalletInformation(
                                    responseModel
                                )
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@MainActivity, res.message)
                                MyUtils.logoutApp(this@MainActivity)
                            } else {
                                MyUtils.showMessage(this@MainActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.navigation_home -> {
                fragment = HomeFragment()
                loadFragment()
                return true
            }
            R.id.navigation_dashboard -> {
                fragment = MyMatchesFragment()
                loadFragment()
                return true
            }
            /*R.id.navigation_leader->{
                fragment = GlobalLeaderBoardFragment()
                loadFragment()
                return true
            }*/
            R.id.navigation_myaccount -> {
                fragment = MyAccountFragment()
                loadFragment()
                return true
            }
            R.id.navigation_notifications -> {
                fragment = MoreOptionsFragment()
                loadFragment()
                return true
            }
        }
        return false
    }

    private fun loadFragment() {
        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_body, fragment!!)
            fragmentTransaction.commit()
        }
    }

    private fun updateCheckApk() {
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("version_code", BuildConfig.VERSION_CODE)

        RetrofitClient(mContext).client.create(IApiMethod::class.java).apkUpdate(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    if (!isFinishing) {
                        if (response!!.body() != null) {
                            val res = JSONObject(response.body().toString())
                            showScore = res.getBoolean("show_scoreboard")
                            menuArrayList.clear()

                            for (i in 0 until res.getJSONArray("menu").length()) {
                                menuArrayList.add(res.getJSONArray("menu").getJSONObject(i))
                            }

                            MyPreferences.setSplashScreen(
                                mContext,
                                res.getString("splashScreen")
                            )
                            if (res.getBoolean("status")) {
                                CHECK_APK_UPDATE_API = true
                                CHECK_FORCE_UPDATE = res.getBoolean("force_update")
                                updatedApkUrl = res.getString("url")
                                releaseNote = res.getString("release_note")
                                if (res.getString("base_url") != null && res.getString("base_url") != "") {
                                    MyPreferences.setBaseUrl(mContext, res.getString("base_url"))
                                }

                                if (CHECK_APK_UPDATE_API) {
                                    CHECK_APK_UPDATE_API = false
                                    /*val fm = supportFragmentManager
                                    val pioneersFragment =
                                        UpdateAppDialogFragment(updatedApkUrl, releaseNote)
                                    pioneersFragment.isCancelable = false
                                    pioneersFragment.show(fm, "updateapp_tag")*/

                                    if (!isActivityRunning(UpdateApplicationActivity::class.java)) {

                                        val intent = Intent(
                                            this@MainActivity,
                                            UpdateApplicationActivity::class.java
                                        )
                                        intent.putExtra(
                                            UpdateApplicationActivity.REQUEST_CODE_APK_UPDATE,
                                            updatedApkUrl
                                        )
                                        intent.putExtra(
                                            UpdateApplicationActivity.REQUEST_RELEASE_NOTE,
                                            releaseNote
                                        )
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun isActivityRunning(aClass: Class<*>): Boolean {
        return try {
            val am = mContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            componentInfo!!.className == aClass.name
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onDrawerItemSelected(view: View?, position: Int) {
        displayView(position)
    }

    private fun displayView(position: Int) {
        if (position == 0) {
            val intent = Intent(mContext, EditProfileActivity::class.java)
            intent.putExtra(FullScreenImageViewActivity.KEY_IMAGE_URL, userInfo!!.profileImage)
            startActivity(intent)
        } else if (position == 1) {
            val intent = Intent(mContext, MyBalanceActivity::class.java)
            startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        } else if (position == 2) {
            val intent = Intent(mContext, InviteFriendsActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity)
            startActivity(intent, options.toBundle())
        }/* else if (position == 3) {
            val intent = Intent(mContext, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_FANTASY_POINTS)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
            val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity)
            startActivity(intent, options.toBundle())
        }*/ else if (position == 3) {
            val intent = Intent(mContext, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_FANTASY_POINTS)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
            val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity)
            startActivity(intent, options.toBundle())
        } else if (position == 4) {
       //     mBinding!!.navigation.selectedItemId = R.id.navigation_notifications
            mBinding!!.navigation.show(ID_NOTIFICATIONS,true)
            fragment = FixtureCricketFragment()
            loadFragment()
        } else if (position == 5) {
            logoutApp("Are you sure you want to logout", true)
        }
    }

    private fun setProfileData() {
        Glide.with(this).load(userInfo!!.profileImage)
            .placeholder(R.drawable.player_blue).into(mBinding!!.profileImage)

        Glide.with(this).load(userInfo!!.profileImage)
            .placeholder(R.drawable.player_blue)
            .into(findViewById<CircleImageView>(R.id.profile_image_drawer))

        findViewById<TextView>(R.id.name).text = userInfo!!.fullName
        findViewById<TextView>(R.id.mobile).text = String.format("@%s", userInfo!!.teamName)
    }
}