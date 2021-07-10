package ninja.cricks

import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.JsonObject
import ninja.cricks.customviews.CircleImageView
import ninja.cricks.databinding.ActivityMainBinding
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RetrofitClient
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseActivity
import ninja.cricks.ui.dashboard.FragmentDrawer
import ninja.cricks.ui.dashboard.MoreOptionsFragment
import ninja.cricks.ui.dashboard.MyAccountFragment
import ninja.cricks.ui.dashboard.MyMatchesFragment
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

    var fragment: Fragment? = null
    private var mBinding: ActivityMainBinding? = null
    private lateinit var mContext: Context
    private var drawerFragment: FragmentDrawer? = null

    companion object {
        var menuArrayList = ArrayList<JSONObject>()
        var showScore: Boolean = false
        var CHECK_WALLET_ONCE: Boolean? = false
        var updatedApkUrl: String = ""
        var releaseNote: String = ""
        var CHECK_APK_UPDATE_API: Boolean = false
        var CHECK_FORCE_UPDATE: Boolean = true
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

        getWalletBalances()
        setProfileData()

        mBinding!!.navigation.setOnNavigationItemSelectedListener(this)

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
    }

    override fun onResume() {
        super.onResume()
        updateCheckApk()
        userInfo = (application as NinjaApplication).userInformations
        if (userInfo != null) {
            Glide.with(this)
                .load(userInfo!!.profileImage)
                .placeholder(R.drawable.player_blue)
                .into(mBinding!!.profileImage)
        }
    }

    fun viewUpcomingMatches() {
        mBinding!!.navigation.selectedItemId = R.id.navigation_home
    }

    fun viewAllMatches() {
        mBinding!!.navigation.selectedItemId = R.id.navigation_home
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
        } else if (position == 3) {
            val intent = Intent(mContext, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_FANTASY_POINTS)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
            val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity)
            startActivity(intent, options.toBundle())
        } else if (position == 4) {
            fragment = MoreOptionsFragment()
            loadFragment()
            mBinding!!.navigation.selectedItemId = R.id.navigation_notifications
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