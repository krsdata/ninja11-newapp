package  ninja.cricks

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import ninja.cricks.databinding.ActivityAddMoneyBinding
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RequestModel
import ninja.cricks.network.WebServiceClient
import ninja.cricks.payments.PaytmHandler
import ninja.cricks.ui.BaseActivity
import ninja.cricks.ui.home.models.UsersPostDBResponse
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.BindingUtils.Companion.GOOGLE_TEZ_PACKAGE_NAME
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddMoneyActivity : BaseActivity(), PaymentResultListener {

    private var mBinding: ActivityAddMoneyBinding? = null
    var paymentMode = ""
    var transactionId = ""
    var orderId = ""
    private lateinit var paymentsClient: PaymentsClient
    val LOAD_PAYMENT_DATA_REQUEST_CODE = 0
    val TAG: String = AddMoneyActivity::class.toString()

    companion object {
        val ADD_EXTRA_AMOUNT: String? = "add_extra_amount"
        val PAYEMENT_TYPE_PAYTM: String = "paytm"
        val PAYEMENT_TYPE_GPAY: String = "gpay"
        val PAYEMENT_TYPE_RAZORPAY: String = "razorpay"
        private const val TEZ_REQUEST_CODE = 10013
        private const val UPI_REQUEST_CODE = 10014
        private const val PAYTM_REQUEST_CODE = 10015
    }

    override fun onStart() {
        super.onStart()
        val infomodel = (application as SportsFightApplication).userInformations
        if (infomodel != null) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEZ_REQUEST_CODE) {
            if (data != null && data.extras != null) {
                if (data.extras!!.getString("Status").equals("SUCCESS", ignoreCase = true)) {
//                    val rand = Random()
//                    val rand_int1 = rand.nextInt(1000000)
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

//        when (requestCode) {
//            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
//                when (resultCode) {
//                    Activity.RESULT_OK ->
//                        PaymentData.getFromIntent(data!!)?.let(::handlePaymentSuccess)!!
//
//                    Activity.RESULT_CANCELED -> {
//                        // The user cancelled without selecting a payment method.
//                    }
//
//                    AutoResolveHelper.RESULT_ERROR -> {
//                        AutoResolveHelper.getStatusFromIntent(data)?.let {
//                            handleError(it.statusCode)
//                        }!!
//                    }
//                }
//            }
//        }
    }

    private fun handleError(statusCode: Int) {

    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        MyUtils.logd("gpayPayment", paymentData.toJson())
//        val paymentMethodToken = paymentData
//            .getJSONObject("tokenizationData")
//            .getString("token")
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_add_money
        )
        mBinding!!.toolbar.title = "Add Cash"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        paymentsClient = createPaymentsClient(this)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        if (intent.hasExtra(ADD_EXTRA_AMOUNT)) {
            var additionalAmount = intent.getDoubleExtra(ADD_EXTRA_AMOUNT, 0.0)
            mBinding!!.editAmounts.setText("" + additionalAmount)

        }
        customeProgressDialog = CustomeProgressDialog(this)
        initWalletInfo()

        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        getWalletBalances()


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

        })
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


        })
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

        mBinding!!.addCash.setOnClickListener(View.OnClickListener {
            var amount = mBinding!!.editAmounts.text.toString()
            if (!TextUtils.isEmpty(amount)) {
                var amt = amount.toDouble()
                var minimumAmount = MyPreferences.getMinimumDeposit(this@AddMoneyActivity)
                if (amt >= minimumAmount!!) {
                    if (mBinding!!.usePaytmWallet.isChecked) {
                        payUsingPaytm(amt)
                    } else if (mBinding!!.useWalletGpay.isChecked) {
                        payUsingGooglePay(amt)
                    } else if (mBinding!!.useWalletPhonepay.isChecked) {
                        payUsingRazorPay(amt.toInt())
                    }
                } else {
                    MyUtils.showMessage(
                        this@AddMoneyActivity,
                        "Deposit amount cannot be less than " + minimumAmount + " Rs."
                    )
                }
            } else {
                MyUtils.showMessage(this@AddMoneyActivity, "Please enter amount")
            }

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
        }).paytmPayment("paytm" + System.currentTimeMillis(), amount)
    }

    private fun payUsingRazorPay(amount: Int) {
        customeProgressDialog.show()
        paymentMode = PAYEMENT_TYPE_RAZORPAY
        var amt = amount * 100
        val models = RequestModel()
        models.amount = amt
        //models.currency = "INR"
        WebServiceClient(this).client.create(IApiMethod::class.java).createRazorPayOrder(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                    showCommonAlert("" + t!!.message)
//                    BindingUtils.sendEventLogs(
//                        this@AddMoneyActivity,
//                        0,0,
//                        userInfo!!,
//                        BindingUtils.FIREBASE_EVENT_ITEM_ID_ADD_MONEY_ACTIVITY+t!!.message
//                    )
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    var res = response!!.body()
                    if (res != null && res.status) {
                        val co = Checkout()
                        co.setImage(R.mipmap.ic_launcher)
                        try {
                            val options = JSONObject()
                            options.put("name", getString(R.string.app_name))
                            options.put(
                                "description",
                                "Adding amount to play " + getString(R.string.app_name)
                            )

                            options.put("currency", "INR")
                            options.put("theme.color", getString(R.string.razorpaythemecolor))
                            options.put("amount", amt.toString())  //1000 means 10rs
                            options.put("order_id", res.orderId) //order Id

                            val prefill = JSONObject()
                            prefill.put("email", userInfo!!.userEmail)
                            prefill.put("contact", userInfo!!.mobileNumber)

                            options.put("prefill", prefill)
                            co.open(this@AddMoneyActivity, options)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@AddMoneyActivity,
                                "Error in payment: " + e.message,
                                Toast.LENGTH_LONG
                            ).show()
                            e.printStackTrace()
                        }
                    }

                }

            })


    }

    fun createPaymentsClient(activity: Activity): PaymentsClient {
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
            var upiId: String = MyPreferences.getGooglePayId(this@AddMoneyActivity)!!

            val uri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", "Deliverdas grocory service")
                .appendQueryParameter("tn", "Thank you for being our valued customers.")
                .appendQueryParameter("am", amount.toString())
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("url", BindingUtils.BASE_URL_API)
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
            )
            intent.setPackage("com.android.vending")
            startActivity(intent)
        }
    }

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

    private fun initWalletInfo() {
        var walletInfo = (application as SportsFightApplication).walletInfo

        MyPreferences.setGooglePayId(this, walletInfo.gPay)
        MyPreferences.setPaytmMid(this, walletInfo.paytmMid)
        MyPreferences.setPaytmCallback(this, walletInfo.callUrl)
        MyPreferences.setMinimumDeposit(this, walletInfo.minDeposit)

        var walletAmount = walletInfo.walletAmount
        mBinding!!.walletTotalAmount.text = String.format("₹%.2f", walletAmount)
        //mBinding!!.amountAdded.text = ""
    }

    fun getWalletBalances() {
        customeProgressDialog.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.token = MyPreferences.getToken(this)!!

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    var res = response!!.body()
                    if (res != null) {
                        var responseModel = res.walletObjects
                        if (responseModel != null) {
                            (application as SportsFightApplication).saveWalletInformation(
                                responseModel
                            )
                            initWalletInfo()
                        }
                    }
                }
            })
    }

    fun addWalletBalance() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        //models.token = MyPreferences.getToken(this)!!
        models.deposit_amount = mBinding!!.editAmounts.text.toString()
        models.transaction_id = transactionId
        models.order_id = orderId
        models.payment_mode = paymentMode
        models.payment_status = "success"

        WebServiceClient(this).client.create(IApiMethod::class.java).addMoney(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    var res = response!!.body()
                    if (res != null) {

                        var responseModel = res.walletObjects
                        if (responseModel != null) {
                            (application as SportsFightApplication).saveWalletInformation(
                                responseModel
                            )
                            MyUtils.showMessage(this@AddMoneyActivity, res.message)
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }

                }

            })

    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        try {
            Toast.makeText(this, "Payment failed $errorCode \n $response", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }

    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
            Toast.makeText(this, "Payment Successful $razorpayPaymentId", Toast.LENGTH_LONG).show()
            transactionId = razorpayPaymentId!!
            addWalletBalance()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }
}
