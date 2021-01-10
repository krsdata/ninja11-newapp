package ninja.cricks.ui.myaccounts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja.cricks.*
import ninja.cricks.databinding.FragmentMyAccountBalanceBinding
import ninja.cricks.models.WalletInfo
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RequestModel
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseFragment
import ninja.cricks.ui.home.models.UsersPostDBResponse
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
    ): View {

        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_account_balance, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding!!.btnAddCash.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireActivity(), AddMoneyActivity::class.java)
            startActivity(intent)
        })

        mBinding!!.btnWithdraw.setOnClickListener(View.OnClickListener {
            if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_VERIFIED) {
                val amount = walletInfo.walletAmount
                if (amount >= 200) {
                    val intent = Intent(requireActivity(), WithdrawAmountsActivity::class.java)
                    startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                } else {
                    MyUtils.showToast(
                        requireActivity() as AppCompatActivity,
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
                MyUtils.showToast(requireActivity() as AppCompatActivity, message)
            }
        })

        mBinding!!.refferalList.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireActivity(), InviteFriendsActivity::class.java)
            startActivity(intent)
        })
        initViews()
    }

    fun initViews() {
        walletInfo = (requireActivity().applicationContext as NinjaApplication).walletInfo
        if (walletInfo != null) {
            mBinding!!.progressBarPlayingHistory.visibility = View.GONE
            initWalletViews(walletInfo)
        }
    }

    private fun initWalletViews(responseModel: WalletInfo) {
        mBinding!!.addedAmount.text = String.format("₹%.2f", responseModel.depositAmount)
        mBinding!!.winningAmount.text = String.format("₹%.2f", responseModel.prizeAmount)
        mBinding!!.cashBonus.text = String.format("₹%.2f", responseModel.bonusAmount)
        mBinding!!.extraCashBonus.text = String.format("₹%.2f", responseModel.extraCash)
        // mBinding!!.earningRefferal.text = String.format("₹ %s",responseModel.referralAmount)

        mBinding!!.earnedPoints.text = String.format(
            "My Reward points: %s",
            responseModel.ninja_point
        )

        val totalBalance =
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
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(requireActivity())!!
        models.token = MyPreferences.getToken(requireActivity())!!

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java).getWallet(models)
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
                        val res = response!!.body()
                        if (res != null) {
                            val responseModel = res.walletObjects
                            if (responseModel != null) {
                                MyPreferences.setRazorPayId(requireActivity(), res.razorPay)
                                MyPreferences.setShowPaytm(requireActivity(), res.paytm_show)
                                MyPreferences.setShowGpay(requireActivity(), res.gpay_show)
                                MyPreferences.setShowRazorPay(requireActivity(), res.rozarpay_show)

                                MyPreferences.setShowPaytmWithdraw(requireActivity(), res.paytm_withdrawal)
                                MyPreferences.setShowBankWithdraw(requireActivity(), res.bank_withdrawal)
                                MyPreferences.setShowUPIWithdraw(requireActivity(), res.upi_withdrawal)

                                (activity!!.applicationContext as NinjaApplication).saveWalletInformation(
                                    responseModel
                                )
                                initViews()
                            }
                        }
                    }
                }
            })
    }
}