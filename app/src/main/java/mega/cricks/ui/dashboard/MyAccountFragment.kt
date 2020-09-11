package mega.cricks.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import mega.cricks.*
import mega.cricks.models.UserInfo
import mega.cricks.models.WalletInfo
import mega.cricks.network.IApiMethod
import mega.cricks.network.RequestModel
import mega.cricks.network.WebServiceClient
import mega.cricks.ui.BaseFragment
import mega.cricks.ui.home.models.UsersPostDBResponse
import mega.cricks.ui.myaccounts.MyAccountBalanceFragment
import mega.cricks.ui.myaccounts.PlayingHistoryFragment
import mega.cricks.ui.myaccounts.TransactionFragment
import mega.cricks.utils.BindingUtils
import mega.cricks.utils.MyPreferences
import mega.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import mega.cricks.R
import mega.cricks.databinding.FragmentMyaccountsBinding

class MyAccountFragment : BaseFragment() {


    private lateinit var walletInfo: WalletInfo
    private lateinit var userInfo: UserInfo
    private var mBinding: FragmentMyaccountsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_myaccounts, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userInfo = (activity!!.applicationContext as SportsFightApplication).userInformations
        walletInfo = (activity!!.applicationContext as SportsFightApplication).walletInfo
        (activity as MainActivity).hideToolbar()

        mBinding!!.notificationClick.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity!!, NotificationListActivity::class.java)
            startActivityForResult(intent,MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        })
        val viewpager: ViewPager = view.findViewById(R.id.account_viewpager)
        val tabs: TabLayout = view.findViewById(R.id.account_tabs)
        setupViewPager(viewpager)
        // viewpager.addOnPageChangeListener(this)
        tabs.setupWithViewPager(viewpager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if(resultCode == Activity.RESULT_OK) {
//
//        }
        customeProgressDialog!!.show()
        getWalletBalances()
    }
    override fun onResume() {
        super.onResume()
        initProfile()
    }

    override fun onStart() {
        super.onStart()
        getWalletBalances()
    }
    fun getWalletBalances() {
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity,"No Internet connection found")
            return
        }
        //mBinding!!.progressBarPlayingHistory.visibility  =View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        models.token = MyPreferences.getToken(activity!!)!!

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getWallet(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if(isAdded) {
                        //mBinding!!.progressBarPlayingHistory.visibility = View.GONE
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if(isVisible) {
                        customeProgressDialog!!.dismiss()
                        //mBinding!!.progressBarPlayingHistory.visibility = View.GONE
                        var res = response!!.body()
                        if(res!=null) {
                            var responseModel = res.walletObjects
                            if(responseModel!=null) {
                                (activity!!.applicationContext as SportsFightApplication).saveWalletInformation(responseModel)
                                initProfile()

//                                var fragment = activity!!.getSupportFragmentManager()
//                                    .findFragmentById("myFragmentTag") as MyAccountBalanceFragment
//                                if (fragment != null) {
//                                    fragment!!.initViews()
//                                }
                            }
                        }
                    }


                }

            })

    }

    fun initProfile() {
        if(!isVisible){
            return
        }
        if(userInfo!=null) {
            mBinding!!.profileName.text = userInfo.fullName
            Glide.with(activity!!)
                .load(userInfo.profileImage)
                .placeholder(R.drawable.player_blue)
                .into(mBinding!!.profileImage)
        }else {
            mBinding!!.profileName.text = "GUEST"
        }

        mBinding!!.btnEditProfile.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity!!, EditProfileActivity::class.java)
            startActivity(intent)
        })
        walletInfo = (activity!!.applicationContext as SportsFightApplication).walletInfo
        if(walletInfo!=null){
            var accountStatus = walletInfo.accountStatus
            if(accountStatus!=null){
                if(walletInfo.bankAccountVerified==BindingUtils.BANK_DOCUMENTS_STATUS_REJECTED){
                    mBinding!!.btnVerifyAccount.text = "REJECTED"
                    mBinding!!.btnVerifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_red)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })

                }else
                if(walletInfo.bankAccountVerified==BindingUtils.BANK_DOCUMENTS_STATUS_VERIFIED){
                    mBinding!!.btnVerifyAccount.text = "Account Verified"
                    mBinding!!.btnVerifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_green)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })
                }else if(walletInfo.bankAccountVerified==BindingUtils.BANK_DOCUMENTS_STATUS_APPROVAL_PENDING){
                    mBinding!!.btnVerifyAccount.text = "Approval Pending"
                    mBinding!!.btnVerifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_white)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.BLACK)
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        gotoDocumentsListActivity()
                    })
                }else {
                    mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                        val intent = Intent(activity!!, VerifyDocumentsActivity::class.java)
                        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                    })
                }
            }
        }else {
            mBinding!!.btnVerifyAccount.setOnClickListener(View.OnClickListener {
                val intent = Intent(activity!!, VerifyDocumentsActivity::class.java)
                startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
            })
        }



    }

    private fun gotoDocumentsListActivity() {
        val intent = Intent(activity!!, DocumentsListActivity::class.java)
        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = MyAccountViewPagerAdapter(activity!!.supportFragmentManager)
        var bundle = Bundle()
       // bundle.putSerializable(SERIALIZABLE_ACCOUNT_BAL,this)
        adapter.addFragment(MyAccountBalanceFragment.newInstance(bundle),"BALANCE")
        adapter.addFragment(PlayingHistoryFragment.newInstance(bundle),"PLAYING HISTORY")
        adapter.addFragment(TransactionFragment.newInstance(bundle),"TRANSACTION")
        viewPager.adapter = adapter
    }


    internal inner class MyAccountViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }
}