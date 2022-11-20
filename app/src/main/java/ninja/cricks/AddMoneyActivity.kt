package  ninja.cricks

import android.app.Activity
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
=======
import android.content.ActivityNotFoundException
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.google.gson.JsonObject
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
=======
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import com.paytm.pgsdk.Log
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.paytm.pgsdk.TransactionManager
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import ninja.cricks.databinding.ActivityAddMoneyBinding
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
import ninja.cricks.payments.PaytmHandler
import ninja.cricks.ui.BaseActivity
=======
import ninja.cricks.ui.BaseActivity
import ninja.cricks.utils.BindingUtils
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
import ninja.cricks.utils.BindingUtils.Companion.GOOGLE_TEZ_PACKAGE_NAME
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
import org.json.JSONArray
=======
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt

=======
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
class AddMoneyActivity : BaseActivity(), PaymentResultListener {

    private var mBinding: ActivityAddMoneyBinding? = null
    var paymentMode = ""
    var transactionId = ""
    var orderId = ""
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
    private lateinit var paymentsClient: PaymentsClient
    val LOAD_PAYMENT_DATA_REQUEST_CODE = 0
    private val TAG: String? = AddMoneyActivity::class.java.simpleName
    private var mContext: Context? = null
=======
    private val TAG: String? = AddMoneyActivity::class.java.simpleName
    private var mContext: Context? = null
    private var ActivityRequestCode = 2
    private var paytmOrderId = ""
    private var isValidCoupon: Boolean = false
    private var appliedCouponCode: String = ""
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt

    companion object {
        val ADD_EXTRA_AMOUNT: String = "add_extra_amount"
        val PAYEMENT_TYPE_PAYTM: String = "paytm"
        val PAYEMENT_TYPE_GPAY: String = "gpay"
        val PAYEMENT_TYPE_RAZORPAY: String = "razorpay"
        private const val TEZ_REQUEST_CODE = 10013
        private const val UPI_REQUEST_CODE = 10014
        private const val PAYTM_REQUEST_CODE = 10015
    }

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEZ_REQUEST_CODE) {
            if (data != null && data.extras != null) {
                if (data.extras!!.getString("Status").equals("SUCCESS", ignoreCase = true)) {
                    transactionId = data.extras!!.getString("txnId")!!
                    addWalletBalance()
                } else {
                    MyUtils.showToast(
                        this@AddMoneyActivity,
                        "Payment not completed, if any amount deducted, please contact us on our support system within 24hr with proof"
                    )
                }
            } else {
                MyUtils.showToast(this@AddMoneyActivity, "Payment not completed please check")
            }
        }
    }

    private fun handleError(statusCode: Int) {

    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        MyUtils.logd("gpayPayment", paymentData.toJson())
    }

=======
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_add_money
        )
=======
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_money)
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
        mContext = this

        mBinding!!.toolbar.title = "Add Cash"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
        paymentsClient = createPaymentsClient(this)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        if (intent.hasExtra(ADD_EXTRA_AMOUNT)) {
            val additionalAmount = intent.getDoubleExtra(ADD_EXTRA_AMOUNT, 0.0)
            mBinding!!.editAmounts.setText("" + additionalAmount)

        }
=======

        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        if (intent.hasExtra(ADD_EXTRA_AMOUNT)) {
            val additionalAmount = intent.getDoubleExtra(ADD_EXTRA_AMOUNT, 0.0)
            mBinding!!.editAmounts.setText(String.format("%s", additionalAmount))
        }

>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
        customeProgressDialog = CustomeProgressDialog(this)
        initWalletInfo()

        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }

        mBinding!!.add100rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("100")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt

        })
=======
        })

>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
        mBinding!!.add200rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("200")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt


        })
=======
        })

>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
        mBinding!!.add300rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("300")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
        })
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
=======

>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
        mBinding!!.add500rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("500")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.white))
        })

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
        mBinding!!.addCash.setOnClickListener(View.OnClickListener {
=======
        mBinding!!.addCash.setOnClickListener {
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
            val amount = mBinding!!.editAmounts.text.toString()
            if (!TextUtils.isEmpty(amount)) {
                val amt = amount.toDouble()
                val minimumAmount = MyPreferences.getMinimumDeposit(this@AddMoneyActivity)
                if (amt >= minimumAmount!!) {
                    if (mBinding!!.usePaytmWallet.isChecked) {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
                        payUsingPaytm(amt)
=======
                        startPaytmPayment(amt)
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
                    } else if (mBinding!!.useWalletGpay.isChecked) {
                        payUsingGooglePay(amt)
                    } else if (mBinding!!.useWalletPhonepay.isChecked) {
                        payUsingRazorPay(amt.toInt())
                    }
                } else {
                    MyUtils.showMessage(
                        this@AddMoneyActivity,
                        "Deposit amount cannot be less than ₹$minimumAmount"
                    )
                }
            } else {
                MyUtils.showMessage(this@AddMoneyActivity, "Please enter amount")
            }
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt

        })
        //checkGpayAvalable()
    }

    private fun payUsingPaytm(amount: Double) {

        customeProgressDialog.show()
        paymentMode = PAYEMENT_TYPE_PAYTM
        PaytmHandler(this, object : PaytmHandler.OnCheckSumGenerated {
            override fun payNow(pmap: HashMap<String, String>) {
                customeProgressDialog.dismiss()
                val order = PaytmOrder(pmap)
                //PaytmPGService Service = PaytmPGService.getStagingService();
                val service = PaytmPGService.getProductionService()
                service.initialize(order, null)
                service.startPaymentTransaction(this@AddMoneyActivity,
                    true, true, object : PaytmPaymentTransactionCallback {
                        override fun onTransactionResponse(inResponse: Bundle?) {
                            var status = inResponse!!.getString("STATUS")

                            if (status!!.equals("TXN_SUCCESS", false)) {
                                transactionId = inResponse.getString("TXNID")!!
                                orderId = inResponse.getString("ORDERID")!!
                                addWalletBalance()
                            } else {
                                MyUtils.showToast(
                                    this@AddMoneyActivity,
                                    "Unable to process the payment"
                                )
                            }
                        }

                        override fun clientAuthenticationFailed(inErrorMessage: String?) {
                            MyUtils.logd("myauth", inErrorMessage)
                        }

                        override fun someUIErrorOccurred(inErrorMessage: String?) {
                            MyUtils.logd("myauth", inErrorMessage)
                        }

                        override fun onTransactionCancel(
                            inErrorMessage: String?,
                            inResponse: Bundle?
                        ) {
                            MyUtils.logd("myauth", inErrorMessage)
                        }

                        override fun networkNotAvailable() {
                            MyUtils.logd("myauth", "Paytm Network not available")
                        }

                        override fun onErrorLoadingWebPage(
                            iniErrorCode: Int,
                            inErrorMessage: String?,
                            inFailingUrl: String?
                        ) {

                        }

                        override fun onBackPressedCancelTransaction() {
                            MyUtils.showMessage(
                                this@AddMoneyActivity,
                                "You have cancelled this transactions. Please try again!!"
                            )
                        }
                    })
            }

            override fun payNowError(t: Throwable?) {
                customeProgressDialog.dismiss()
            }
        }).paytmPayment("paytm" + System.currentTimeMillis(), amount)
=======
        }

        mBinding!!.askCouponText.setOnClickListener {
            if (mBinding!!.codeLayout.visibility == View.GONE) {
                mBinding!!.codeLayout.visibility = View.VISIBLE
                mBinding!!.askCouponText.visibility = View.GONE
            }
        }

        mBinding!!.coupoCodeApply.setOnClickListener {
            checkCouponCode()
        }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
    }

    private fun payUsingRazorPay(amount: Int) {
        customeProgressDialog.show()
        paymentMode = PAYEMENT_TYPE_RAZORPAY
        val amt = amount * 100
        val models = JsonObject()
        models.addProperty("amount", amt)
        models.addProperty("user_id", MyPreferences.getUserID(this)!!)
        models.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).createRazorPayOrder(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                    showCommonAlert("" + t!!.message)
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val co = Checkout()
                            co.setImage(R.mipmap.ic_launcher)
                            co.setKeyID(MyPreferences.getRazorPayId(this@AddMoneyActivity)!!)
                            Checkout.clearUserData(applicationContext)
                            try {
                                val options = JSONObject()
                                options.put(
                                    "key",
                                    MyPreferences.getRazorPayId(this@AddMoneyActivity)!!
                                )
                                options.put("name", getString(R.string.app_name))
                                options.put(
                                    "description",
                                    "Adding amount to play " + getString(R.string.app_name)
                                )
                                options.put("order_id", res.orderId) //order Id
                                options.put("theme.color", getString(R.string.razorpaythemecolor))
                                options.put("currency", "INR")
                                options.put("amount", amt.toString())  //1000 means 10rs
                                options.put("prefill.email", userInfo!!.userEmail)
                                options.put("prefill.contact", userInfo!!.mobileNumber)
                                co.open(this@AddMoneyActivity, options)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@AddMoneyActivity,
                                    "Error in payment: " + e.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                e.printStackTrace()
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                MyUtils.logoutApp(this@AddMoneyActivity)
                            } else {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
    private fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
            .build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    fun checkGpayAvalable() {
        val readyToPayRequest =
            IsReadyToPayRequest.fromJson(googlePayBaseConfiguration.toString())
        val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)
        readyToPayTask.addOnCompleteListener { readyToPayTask ->
            try {
                readyToPayTask.getResult(ApiException::class.java)
                    ?.let { setGooglePayAvailable(true) }
            } catch (exception: ApiException) {
                // Error determining readiness to use Google Pay.
                // Inspect the logs for more details.
            }
        }

    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            mBinding!!.useWalletGpay.visibility = View.VISIBLE
            // mBinding!!.useWalletGpay.setOnClickListener { requestPayment() }
        } else {
            // Unable to pay using Google Pay. Update your UI accordingly.
        }
    }

    private val tokenizationSpecification = JSONObject().apply {
        put("type", "PAYMENT_GATEWAY")
        put(
            "parameters", JSONObject(
                mapOf(
                    "gateway" to "example",
                    "gatewayMerchantId" to "exampleGatewayMerchantId"
                )
            )
        )
    }

    private val transactionInfo = JSONObject().apply {
        put("totalPrice", "123.45")
        put("totalPriceStatus", "FINAL")
        put("currencyCode", "USD")
    }

    private val baseCardPaymentMethod = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            put("allowedCardNetworks", JSONArray(listOf("VISA", "MASTERCARD")))
            put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
        })
    }

    private val googlePayBaseConfiguration = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
        put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod))
    }

    private val merchantInfo = JSONObject().apply {
        put("merchantName", "SportsFight")
        put("merchantId", "BCR2DN6TVOXIX527")
        //put("merchantId", "BCR2DN6TVOXKVXKT")
        //put("merchantId", "01234567890123456789")
    }

    private val paymentDataRequestJson = JSONObject(googlePayBaseConfiguration.toString()).apply {
        put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod))
        put("transactionInfo", transactionInfo)
        put("merchantInfo", merchantInfo)
    }

    private fun requestPayment() {
        val paymentDataRequest =
            PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(paymentDataRequest),
            this, LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    private fun payUsingGooglePay(amount: Double) {
        paymentMode = PAYEMENT_TYPE_GPAY

        if (isAppInstalled(GOOGLE_TEZ_PACKAGE_NAME)) {
            // showProgress();
            val upiId: String = MyPreferences.getGooglePayId(this@AddMoneyActivity)!!
            //Log.e(TAG, "upiId =======> $upiId")
            /*Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", "your-merchant-vpa@xxx")
                .appendQueryParameter("pn", "your-merchant-name")
                .appendQueryParameter("mc", "your-merchant-code")
                .appendQueryParameter("tr", "your-transaction-ref-id")
                .appendQueryParameter("tn", "your-transaction-note")
                .appendQueryParameter("am", "your-order-amount")
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("url", "your-transaction-url")
                .build()*/
            val uri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", "Ninja 11 Service")
                //.appendQueryParameter("mc", "BCR2DN6T4XOJNV")
                .appendQueryParameter("tr", System.currentTimeMillis().toString())
                //.appendQueryParameter("tn", "Thank you for being our valued customers.")
                .appendQueryParameter("am", amount.toString())
                .appendQueryParameter("cu", "INR")
                //.appendQueryParameter("url", BindingUtils.BASE_URL_API)
                .build()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME)
            startActivityForResult(
                intent,
                TEZ_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(
                "https://play.google.com/store/apps/details?id=" + GOOGLE_TEZ_PACKAGE_NAME
=======
    private fun payUsingGooglePay(amount: Double) {
        paymentMode = PAYEMENT_TYPE_GPAY
        val upiId: String = MyPreferences.getGooglePayId(this@AddMoneyActivity)!!
//            Uri.Builder()
//                .scheme("upi")
//                .authority("pay")
//                .appendQueryParameter("pa", "your-merchant-vpa@xxx")
//                .appendQueryParameter("pn", "your-merchant-name")
//                .appendQueryParameter("mc", "your-merchant-code")
//                .appendQueryParameter("tr", "your-transaction-ref-id")
//                .appendQueryParameter("tn", "your-transaction-note")
//                .appendQueryParameter("am", "your-order-amount")
//                .appendQueryParameter("cu", "INR")
//                .appendQueryParameter("url", "your-transaction-url")
//                .build()
        val uri = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", "Ninja 11 Service")
            .appendQueryParameter("tr", System.currentTimeMillis().toString())
            .appendQueryParameter("am", amount.toString())
            .appendQueryParameter("cu", "INR")
            .build()
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME)
            startActivityForResult(intent, TEZ_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$GOOGLE_TEZ_PACKAGE_NAME"
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
            )
            intent.setPackage("com.android.vending")
            startActivity(intent)
        }
    }

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
    private fun isAppInstalled(packageName: String): Boolean {
        val pm: PackageManager = packageManager
        var installed = false
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return installed
    }

=======
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
    private fun initWalletInfo() {
        val walletInfo = (application as NinjaApplication).walletInfo

        MyPreferences.setGooglePayId(this, walletInfo.gPay)

        MyPreferences.setPaytmMid(this, walletInfo.paytmMid)
        MyPreferences.setPaytmCallback(this, walletInfo.callUrl)
        MyPreferences.setMinimumDeposit(this, walletInfo.minDeposit)

        val walletAmount = walletInfo.walletAmount
        mBinding!!.walletTotalAmount.text = String.format("₹%.2f", walletAmount)

        if (MyPreferences.getShowPaytm(mContext!!)) {
            mBinding!!.usePaytmWallet.visibility = View.VISIBLE
        } else {
            mBinding!!.usePaytmWallet.visibility = View.GONE
        }
        if (MyPreferences.getShowGpay(mContext!!)) {
            mBinding!!.useWalletGpay.visibility = View.VISIBLE
        } else {
            mBinding!!.useWalletGpay.visibility = View.GONE
        }
        if (MyPreferences.getShowRazorPay(mContext!!)) {
            mBinding!!.useWalletPhonepay.visibility = View.VISIBLE
        } else {
            mBinding!!.useWalletPhonepay.visibility = View.GONE
        }
    }

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
    fun addWalletBalance() {
=======
    private fun addWalletBalance() {
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("deposit_amount", mBinding!!.editAmounts.text.toString())
        jsonRequest.addProperty("transaction_id", transactionId)
        jsonRequest.addProperty("order_id", orderId)
        jsonRequest.addProperty("payment_mode", paymentMode)
        jsonRequest.addProperty("payment_status", "success")
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
=======
        if (appliedCouponCode != "" && isValidCoupon) {
            jsonRequest.addProperty("coupon", appliedCouponCode)
        }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt

        WebServiceClient(this).client.create(IApiMethod::class.java).addMoney(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.walletObjects
                            if (responseModel != null) {
                                (application as NinjaApplication).saveWalletInformation(
                                    responseModel
                                )
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                MyUtils.logoutApp(this@AddMoneyActivity)
                            } else {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        try {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
            Toast.makeText(this, "Payment failed $errorCode \n $response", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }

=======
            MyUtils.showMessage(this@AddMoneyActivity, "Transaction has been cancelled")
            Log.e(TAG, "Payment failed $errorCode \n $response")
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
            Toast.makeText(this, "Payment Successful $razorpayPaymentId", Toast.LENGTH_LONG).show()
=======
            //MyUtils.showMessage(this@AddMoneyActivity, "Payment Successfully added")
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
            transactionId = razorpayPaymentId!!
            addWalletBalance()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/AddMoneyActivity.kt
=======

    private fun startPaytmPayment(amt: Double) {
        if (MyUtils.isNetworkConnected(mContext!!)) {
            customeProgressDialog.show()
            try {
                val jsonRequest = JsonObject()
                jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
                jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
                jsonRequest.addProperty("deposit_amount", amt.toString())

                WebServiceClient(this).client.create(IApiMethod::class.java)
                    .initiateTransaction(jsonRequest)
                    .enqueue(object : Callback<JsonObject?> {
                        override fun onResponse(
                            call: Call<JsonObject?>,
                            response: Response<JsonObject?>
                        ) {
                            customeProgressDialog.dismiss()
                            if (response.body() != null) {
                                try {
                                    val jsonObject = JSONObject(response.body().toString())
                                    if (jsonObject.getBoolean("status")) {
                                        paytmOrderId =
                                            jsonObject.getJSONObject("data").getString("order_id")
                                        orderId =
                                            jsonObject.getJSONObject("data").getString("order_id")
                                        val mid = jsonObject.getJSONObject("data").getString("mid")
                                        val txnToken =
                                            jsonObject.getJSONObject("data").getString("txnToken")
                                        paytmNewPayment(paytmOrderId, mid, txnToken, amt.toString())
                                    } else {
                                        if (jsonObject.getInt("code") == 1001) {
                                            MyUtils.showMessage(
                                                mContext!!,
                                                jsonObject.getString("message")
                                            )
                                            MyUtils.logoutApp(this@AddMoneyActivity)
                                        } else {
                                            MyUtils.showMessage(
                                                mContext!!,
                                                jsonObject.getString("message")
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                            customeProgressDialog.dismiss()
                            Log.e(TAG, "paytmDeposit t =======> ${t.localizedMessage}")
                        }
                    })
            } catch (e: Exception) {
                customeProgressDialog.dismiss()
                e.printStackTrace()
            }
        } else {
            MyUtils.showToast(
                this@AddMoneyActivity,
                resources.getString(R.string.internetconnection)
            )
        }
    }

    private fun paytmNewPayment(
        orderIdString: String,
        midString: String,
        txnTokenString: String,
        txnAmountString: String
    ) {
        customeProgressDialog.show()
        paymentMode = PAYEMENT_TYPE_PAYTM
        val callBackUrl: String = BindingUtils.PAYTM.callBackUrl + orderIdString
        Log.e(TAG, "callBackUrl =======> $callBackUrl")

        val paytmOrder = PaytmOrder(
            orderIdString,
            midString,
            txnTokenString,
            txnAmountString,
            callBackUrl
        )
        val transactionManager = TransactionManager(
            paytmOrder,
            object : PaytmPaymentTransactionCallback {
                override fun onTransactionResponse(inResponse: Bundle?) {
                    try {
                        if (inResponse != null) {
                            Log.e(TAG, "Response onTransactionResponse =====> $inResponse")
                            val jsonObject = JSONObject()
                            for (key in inResponse.keySet()) {
                                Log.e(
                                    TAG,
                                    "Response Key ========> $key  value ========> ${inResponse[key]}"
                                )
                                jsonObject.put(key, inResponse[key])
                            }
                            transactionId = inResponse["TXNID"].toString()
                            addWalletBalance()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun networkNotAvailable() {
                    Log.e(TAG, mContext!!.resources.getString(R.string.internetconnection))
                }

                override fun onErrorProceed(inErrorMessage: String?) {
                    Log.e(TAG, "onErrorProceed  =======>  $inErrorMessage")
                }

                override fun clientAuthenticationFailed(inErrorMessage: String?) {
                    Log.e(TAG, "clientAuthenticationFailed  =======>  $inErrorMessage")
                }

                override fun someUIErrorOccurred(inErrorMessage: String?) {
                    Log.e(TAG, "someUIErrorOccurred  =======>  $inErrorMessage")
                }

                override fun onErrorLoadingWebPage(
                    iniErrorCode: Int,
                    inErrorMessage: String,
                    inFailingUrl: String
                ) {
                    Log.e(TAG, "someUIErrorOccurred  =======>  $inErrorMessage")
                }

                override fun onBackPressedCancelTransaction() {
                    Log.e(TAG, "onBackPressedCancelTransaction  =======>  ")
                    try {
                        val jsonObject = JSONObject()
                        jsonObject.put("STATUS", "USER_CANCELLED")
                        //updateOrderStatus(paytmOrderId, jsonObject)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onTransactionCancel(inErrorMessage: String, inResponse: Bundle) {
                    Log.e(TAG, "onTransactionCancel  =======>  $inErrorMessage")
                    try {
                        if (inResponse != null) {
                            Log.e(TAG, "onTransactionCancel  =======>  $inResponse")
                            val jsonObject = JSONObject()
                            for (key in inResponse.keySet()) {
                                jsonObject.put(key, inResponse[key])
                            }
                            //updateOrderStatus(paytmOrderId, jsonObject)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        transactionManager.setShowPaymentUrl(BindingUtils.PAYTM.PaymentUrl)
        customeProgressDialog.dismiss()
        transactionManager.startTransaction(this, ActivityRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEZ_REQUEST_CODE) {
            if (data != null && data.extras != null) {
                for (key in data.extras!!.keySet()) {
                    Log.e(TAG, "$key==\"${data.extras!!.get(key)}\"")
                }
                if (data.extras!!.getString("Status").equals("SUCCESS", ignoreCase = true)) {
                    transactionId = data.extras!!.getString("txnId")!!
                    addWalletBalance()
                } else {
                    MyUtils.showToast(
                        this@AddMoneyActivity,
                        "Payment not completed, if any amount deducted, please contact us on our support system within 24hr with proof"
                    )
                }
            } else {
                MyUtils.showToast(this@AddMoneyActivity, "Payment not completed please check")
            }
        } else if (requestCode == ActivityRequestCode && data != null) {
            Log.e(
                TAG,
                data.getStringExtra("nativeSdkForMerchantMessage") + " " + data.getStringExtra("response")
            )
            try {
                if (data.getStringExtra("response") != null) {
                    if (data.getStringExtra("response") != "") {
                        Log.e(TAG, "onActivityResult ======> " + data.getStringExtra("response"))
                        val inResponse = JSONObject(data.getStringExtra("response"))
                        Log.e(TAG, "response ======> $inResponse")
                        transactionId = inResponse.getString("TXNID")
                        addWalletBalance()

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkCouponCode() {
        if (mBinding!!.editCoupon.text.toString().length < 0) {
            MyUtils.showMessage(mContext!!, "Please add coupon code")
        } else {
            if (MyUtils.isConnectedWithInternet(this)) {
                customeProgressDialog.show()

                val jsonRequest = JsonObject()
                jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
                jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
                jsonRequest.addProperty("code", mBinding!!.editCoupon.text.toString())

                WebServiceClient(this).client.create(IApiMethod::class.java)
                    .validateCoupon(jsonRequest)
                    .enqueue(object : Callback<JsonObject?> {
                        override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                            customeProgressDialog.dismiss()
                        }

                        override fun onResponse(
                            call: Call<JsonObject?>?,
                            response: Response<JsonObject?>?
                        ) {
                            customeProgressDialog.dismiss()

                            if (response!!.body() != null) {
                                val res = JSONObject(response.body().toString())
                                if (res.getBoolean("status")) {
                                    isValidCoupon = true
                                    appliedCouponCode = mBinding!!.editCoupon.text.toString()
                                    MyUtils.showToast(
                                        this@AddMoneyActivity,
                                        res.getString("message")
                                    )
                                } else {
                                    isValidCoupon = false
                                    appliedCouponCode = ""
                                    if (res.getInt("code") == 1001) {
                                        MyUtils.showMessage(
                                            this@AddMoneyActivity,
                                            res.getString("message")
                                        )
                                        MyUtils.logoutApp(this@AddMoneyActivity)
                                    } else {
                                        MyUtils.showToast(
                                            this@AddMoneyActivity,
                                            res.getString("message")
                                        )
                                    }
                                }
                            }
                        }
                    })
            } else {
                MyUtils.showToast(
                    this@AddMoneyActivity,
                    mContext!!.resources.getString(R.string.internetconnection)
                )
            }
        }
    }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/AddMoneyActivity.kt
}