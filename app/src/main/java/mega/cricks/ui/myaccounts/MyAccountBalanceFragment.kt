package mega.cricks.ui.myaccounts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import mega.cricks.*
import mega.cricks.models.WalletInfo
import mega.cricks.network.IApiMethod
import mega.cricks.network.RequestModel
import mega.cricks.network.WebServiceClient
import mega.cricks.ui.BaseFragment
import mega.cricks.ui.home.models.UsersPostDBResponse
import mega.cricks.utils.BindingUtils
import mega.cricks.utils.MyPreferences
import mega.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import mega.cricks.R
import mega.cricks.databinding.FragmentMyAccountBalanceBinding


class MyAccountBalanceFragment : BaseFragment() {
    private lateinit var walletInfo: WalletInfo

    //var myAccountFragment: MyAccountFragment?=null
    private var mBinding: FragmentMyAccountBalanceBinding? = null

    companion object {
        val SERIALIZABLE_ACCOUNT_BAL: String = "playerslist"
        fun newInstance(bundle: Bundle): MyAccountBalanceFragment {
            val fragment = MyAccountBalanceFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //myAccountFragment = arguments!!.get(SERIALIZABLE_ACCOUNT_BAL) as MyAccountFragment

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_account_balance, container, false
        )

        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding!!.btnAddCash.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity!!, AddMoneyActivity::class.java)
            startActivity(intent)
        })

        mBinding!!.btnWithdraw.setOnClickListener(View.OnClickListener {
            if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_VERIFIED) {
                var amount = walletInfo.walletAmount
                if (amount >= 200) {
                    val intent = Intent(activity!!, WithdrawAmountsActivity::class.java)
                    startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                } else {
                    MyUtils.showToast(
                        activity!! as AppCompatActivity,
                        "Amount is less than 200 INR"
                    )
                }

            } else {
                var message = "Please Verify your account"
                if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_APPROVAL_PENDING) {
                    message = "Documents Approvals Pending"
                } else if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_REJECTED) {
                    message = "Your Document Rejected Please contact admin"
                }
                MyUtils.showToast(activity!! as AppCompatActivity, message)
            }

        })

        mBinding!!.refferalList.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity!!, RefferalFriendsListActivity::class.java)
            startActivity(intent)
        })



        initViews()
    }

    fun initViews() {
        walletInfo = (activity!!.applicationContext as SportsFightApplication).walletInfo
        if (walletInfo != null) {
            mBinding!!.progressBarPlayingHistory.visibility = View.GONE
            initWalletViews(walletInfo)
        }
    }


    private fun initWalletViews(responseModel: WalletInfo) {
        mBinding!!.addedAmount.text = String.format("₹%.2f", responseModel.depositAmount)
        mBinding!!.winningAmount.text = String.format("₹%.2f", responseModel.prizeAmount)

        mBinding!!.cashBonus.text = String.format("₹%.2f", responseModel.bonusAmount)
        // mBinding!!.earningRefferal.text = String.format("₹ %s",responseModel.referralAmount)

        var totalBalance =
            responseModel.depositAmount + responseModel.prizeAmount + responseModel.bonusAmount
        mBinding!!.totalBalance.text = String.format("₹%.2f", totalBalance)

        mBinding!!.friendsCounts.text = String.format("%d", responseModel.refferalCounts)


    }


    override fun onStart() {
        super.onStart()
        if (isVisible) {
            getWalletBalances()
        }
    }

    fun getWalletBalances() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        customeProgressDialog!!.show()
        //mBinding!!.progressBarPlayingHistory.visibility  =View.VISIBLE
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        models.token = MyPreferences.getToken(activity!!)!!

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getWallet(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (isAdded) {
                        //mBinding!!.progressBarPlayingHistory.visibility = View.GONE
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (isVisible) {
                        customeProgressDialog!!.dismiss()
                        //mBinding!!.progressBarPlayingHistory.visibility = View.GONE
                        var res = response!!.body()
                        if (res != null) {
                            var responseModel = res.walletObjects
                            if (responseModel != null) {
                                (activity!!.applicationContext as SportsFightApplication).saveWalletInformation(
                                    responseModel
                                )
                                initViews()

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


}
