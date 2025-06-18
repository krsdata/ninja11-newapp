package  ninja.cricks

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import com.paytm.pgsdk.Log
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.paytm.pgsdk.TransactionManager
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import ninja.cricks.databinding.ActivityAddMoneyBinding
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.PhonePeClient
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseActivity
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.BindingUtils.Companion.GOOGLE_TEZ_PACKAGE_NAME
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.Charset
import java.security.MessageDigest


class AddMoneyActivity : BaseActivity() {

    private var mBinding: ActivityAddMoneyBinding? = null
    var paymentMode = ""
    var transactionId = ""
    var orderId = ""
    private val TAG: String = AddMoneyActivity::class.java.simpleName
    private var mContext: Context? = null
    private var ActivityRequestCode = 2
    private var paytmOrderId = ""
    private var isValidCoupon: Boolean = false
    private var appliedCouponCode: String = ""
    private var paymentLink = ""
    private var phonePe: PhonePe? = null
    var apiEndPoint: String = "/pg/v1/pay"
    var base64Body: String = ""
    var checksum: String = ""
    var salt: String = "c246fadd-6523-4def-be15-685fc96aa160"
    var saltIndex: String = "1"
    var phonePeObject = JSONObject()

    companion object {
        val ADD_EXTRA_AMOUNT: String = "add_extra_amount"
        val PAYEMENT_TYPE_PAYTM: String = "paytm"
        val PAYEMENT_TYPE_GPAY: String = "gpay"
        val PAYEMENT_TYPE_RAZORPAY: String = "razorpay"
        private const val TEZ_REQUEST_CODE = 10013
        private const val UPI_REQUEST_CODE = 10014
        private const val PAYTM_REQUEST_CODE = 10015
        private const val B2B_PG_REQUEST_CODE = 777
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_money)
        mContext = this

        mBinding!!.toolbar.title = "Add Cash"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)

        PhonePe.init(this, PhonePeEnvironment.RELEASE, "NINJA11ONLINE", "")

        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        if (intent.hasExtra(ADD_EXTRA_AMOUNT)) {
            val additionalAmount = intent.getDoubleExtra(ADD_EXTRA_AMOUNT, 0.0)
            mBinding!!.editAmounts.setText(String.format("%s", additionalAmount))
        }

        customeProgressDialog = CustomeProgressDialog(this)
        initWalletInfo()

        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }

        mBinding!!.add100rs.setOnClickListener {
            mBinding!!.editAmounts.setText("100")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
        }

        mBinding!!.add200rs.setOnClickListener {
            mBinding!!.editAmounts.setText("200")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
        }

        mBinding!!.add300rs.setOnClickListener {
            mBinding!!.editAmounts.setText("300")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
        }

        mBinding!!.add500rs.setOnClickListener {
            mBinding!!.editAmounts.setText("500")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.white))
        }

        mBinding!!.addCash.setOnClickListener {
            val amount = mBinding!!.editAmounts.text.toString()
            if (!TextUtils.isEmpty(amount)) {
                val amt = amount.toDouble()
                val minimumAmount = MyPreferences.getMinimumDeposit(this@AddMoneyActivity)
                if (amt >= minimumAmount!!) {
                    if (mBinding!!.usePaytmWallet.isChecked) {
                        startPaytmPayment(amt)
//                        MyUtils.showMessage(this@AddMoneyActivity, "Coming Soon")
                    } else if (mBinding!!.useWalletGpay.isChecked) {
//                        payUsingGooglePay(amt)
                        MyUtils.showMessage(this@AddMoneyActivity, "Coming Soon")
                    } else if (mBinding!!.useWalletPhonepay.isChecked) {
                        getPhonePeData(amt)
                    }/* else if (mBinding!!.useWalletUpi.isChecked) {
                        if (amt >= 100) {
                            startUPIPayment(amt)
                        } else {
                            MyUtils.showMessage(this@AddMoneyActivity, "Deposit amount cannot be less than ₹100")
                        }
                    }*/
                } else {
                    MyUtils.showMessage(this@AddMoneyActivity, "Deposit amount cannot be less than ₹$minimumAmount")
                }
            } else {
                MyUtils.showMessage(this@AddMoneyActivity, "Please enter amount")
            }
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

        selectedValue()
    }

    private fun selectedValue() {
        mBinding!!.editAmounts.setText("100")
        mBinding!!.add100rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
        mBinding!!.add100rs.setTextColor(resources.getColor(R.color.white))

        mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
        mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

        mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
        mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

        mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
        mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
    }

    private fun payUsingGooglePay(amount: Double) {
        paymentMode = PAYEMENT_TYPE_GPAY
        val upiId: String = MyPreferences.getGooglePayId(this@AddMoneyActivity)!!
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
            )
            intent.setPackage("com.android.vending")
            startActivity(intent)
        }
    }

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
        if (MyPreferences.getShowPhonePe(mContext!!)) {
            mBinding!!.useWalletPhonepay.visibility = View.VISIBLE
        } else {
            mBinding!!.useWalletPhonepay.visibility = View.GONE
        }

        /*if (MyPreferences.getShowUPI(mContext!!)) {
            mBinding!!.useWalletUpi.visibility = View.VISIBLE
        } else {
            mBinding!!.useWalletUpi.visibility = View.GONE
        }*/
    }

    private fun addWalletBalance() {
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
        if (appliedCouponCode != "" && isValidCoupon) {
            jsonRequest.addProperty("coupon", appliedCouponCode)
        }

        WebServiceClient(this).client.create(IApiMethod::class.java).addMoney(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>, t: Throwable) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(call: Call<UsersPostDBResponse?>, response: Response<UsersPostDBResponse?>) {
                    customeProgressDialog.dismiss()
                    val res = response.body()
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

    private fun paytmNewPayment(orderIdString: String, midString: String, txnTokenString: String, txnAmountString: String) {
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "requestCode ==========> $requestCode")
        Log.e(TAG, "data ==========> ${data?.extras.toString()}")
        if (requestCode == B2B_PG_REQUEST_CODE) {
            phonePeAddWallet()
//            MyUtils.showToast(this@AddMoneyActivity, "check callback url")
            if (data != null && data.extras != null) {
                Log.e(TAG, "on activity result print data ===============> ${data.extras.toString()}")
                for (key in data.extras!!.keySet()) {
                    Log.e(TAG, "$key==\"${data.extras!!.get(key)}\"")
                }
            } else {
                MyUtils.showToast(
                    this@AddMoneyActivity,
                    "Payment not completed, if any amount deducted, please contact us on our support system within 24hr with proof"
                )
//                setResult(Activity.RESULT_OK)
//                finish()
            }
        } else if (requestCode == TEZ_REQUEST_CODE) {
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
        if (mBinding!!.editCoupon.text.toString().isEmpty()) {
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
                        override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                            customeProgressDialog.dismiss()
                        }

                        override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                            customeProgressDialog.dismiss()

                            if (response.body() != null) {
                                val res = JSONObject(response.body().toString())
                                if (res.getBoolean("status")) {
                                    isValidCoupon = true
                                    appliedCouponCode = mBinding!!.editCoupon.text.toString()
                                    MyUtils.showToast(this@AddMoneyActivity, res.getString("message"))
                                } else {
                                    isValidCoupon = false
                                    appliedCouponCode = ""
                                    if (res.getInt("code") == 1001) {
                                        MyUtils.showMessage(this@AddMoneyActivity, res.getString("message"))
                                        MyUtils.logoutApp(this@AddMoneyActivity)
                                    } else {
                                        MyUtils.showToast(this@AddMoneyActivity, res.getString("message"))
                                    }
                                }
                            }
                        }
                    })
            } else {
                MyUtils.showToast(this@AddMoneyActivity, mContext!!.resources.getString(R.string.internetconnection))
            }
        }
    }

    private fun startUPIPayment(amt: Double) {
        //  https://rest.ninja11.in/api/v3/initiateUPIPayment

        if (MyUtils.isNetworkConnected(mContext!!)) {
            customeProgressDialog.show()
            try {
                val jsonRequest = JsonObject()
                jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
                jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
                jsonRequest.addProperty("deposit_amount", amt.toString())

                WebServiceClient(this).client.create(IApiMethod::class.java)
                    .initiateUPIPayment(jsonRequest)
                    .enqueue(object : Callback<JsonObject?> {
                        override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                            customeProgressDialog.dismiss()
                            if (response.body() != null) {
                                try {
                                    val jsonObject = JSONObject(response.body().toString())
                                    if (jsonObject.getBoolean("status")) {

                                        paymentLink = jsonObject.getJSONObject("data").getString("payment_link")

                                        if (paymentLink != "") {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink))
                                            startActivity(browserIntent)
                                            finish()
                                        } else {
                                            MyUtils.showMessage(mContext!!, "There is some issue in adding payment. Please try after sometime.")
                                        }
                                    } else {
                                        if (jsonObject.getInt("code") == 1001) {
                                            MyUtils.showMessage(mContext!!, jsonObject.getString("message"))
                                            MyUtils.logoutApp(this@AddMoneyActivity)
                                        } else {
                                            MyUtils.showMessage(mContext!!, jsonObject.getString("message"))
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
            MyUtils.showToast(this@AddMoneyActivity, resources.getString(R.string.internetconnection))
        }
    }

    private fun getPhonePeData(amt: Double) {
        if (MyUtils.isConnectedWithInternet(this)) {
            customeProgressDialog.show()
            // https://rest.ninja11.in/api/v3/phonePeInitiate
            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
            jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
            jsonRequest.addProperty("amount", (amt.toInt() * 100))
            jsonRequest.addProperty("deposit_amount", (amt.toInt() * 100))

            WebServiceClient(this).client.create(IApiMethod::class.java).phonePeInitiate(jsonRequest)
                .enqueue(object : Callback<JsonObject?> {
                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        customeProgressDialog.dismiss()
                    }

                    override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                        customeProgressDialog.dismiss()
                        if (response.body() != null) {
                            val res = JSONObject(response.body().toString())
                            Log.e(TAG, "res from phone pe init ========> $res")
                            if (res.getInt("status") == 200) {
                                Log.e(TAG, "res from phone pe init ========> $res")

                                val responseObject = res.getJSONObject("response")
                                val data = responseObject.getJSONObject("data")
                                val instrumentResponse = data.getJSONObject("instrumentResponse")

                                val amount = amt.toInt() * 100

//                                val paymentInstrument = JSONObject()
//                                paymentInstrument.put("type", instrumentResponse.getString("type"))
////                                phonePeObject.put("merchantId", data.getString("merchantId"))
////                                phonePeObject.put("merchantTransactionId", data.getString("merchantTransactionId"))
////                                phonePeObject.put("merchantUserId", userInfo?.userId)
////                                phonePeObject.put("amount", amount)
////                                phonePeObject.put("callbackUrl", "https://rest.ninja11.in/api/v3/callbackURLPhonePe")
////                                phonePeObject.put("mobileNumber", userInfo?.mobileNumber)
////                                phonePeObject.put("paymentInstrument", paymentInstrument)

//                                val transId = "${System.currentTimeMillis()}#${MyPreferences.getUserID(this@AddMoneyActivity)!!}"
                                val transId = "${System.currentTimeMillis()}${res.getString("uid")}"
//                                val transId = res.getString("transaction_id")
                                val paymentInstrument = JSONObject()
                                val device = JSONObject()
                                device.put("deviceOS", "ANDROID")
                                paymentInstrument.put("type", "PAY_PAGE")
//                                paymentInstrument.put("type", "UPI_INTENT")
//                                paymentInstrument.put("targetApp", "com.phonepe.app")
                                phonePeObject.put("merchantId", "NINJA11ONLINE")
                                phonePeObject.put("merchantTransactionId", transId)
                                phonePeObject.put("merchantUserId", userInfo?.userId)
                                phonePeObject.put("amount", amt.toInt() * 100)
                                phonePeObject.put("callbackUrl", "https://rest.ninja11.in/api/v3/callbackURLPhonePe")
                                phonePeObject.put("mobileNumber", userInfo?.mobileNumber)
                                phonePeObject.put("paymentInstrument", paymentInstrument)
//                                jsonObject.put("deviceContext", device)

                                Log.e(TAG, "jsonObject ========> $phonePeObject")

                                base64Body = encodeToBase64(phonePeObject.toString())
                                val input: String = base64Body + apiEndPoint + salt
                                android.util.Log.e(TAG, "input ====> $input")
                                checksum = sha256("$base64Body/pg/v1/payc246fadd-6523-4def-be15-685fc96aa160") + "###1"
                                android.util.Log.e(TAG, "checksum =========> $checksum")
                                val b2BPGRequest = B2BPGRequestBuilder().setData(base64Body).setChecksum(checksum).setUrl("/pg/v1/pay").build()
                                try {
                                    PhonePe.getImplicitIntent(mContext!!, b2BPGRequest, "")?.let { startActivityForResult(it, B2B_PG_REQUEST_CODE) }
                                } catch (e: PhonePeInitException) {
                                    android.util.Log.e(TAG, "error =======> $e")
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "error =======> $e")
                                }
                            } else {
                                if (res.getInt("code") == 1001) {
                                    MyUtils.showMessage(this@AddMoneyActivity, res.getString("message"))
                                    MyUtils.logoutApp(this@AddMoneyActivity)
                                } else {
                                    MyUtils.showToast(this@AddMoneyActivity, res.getString("message"))
                                }
                            }
                        }
                    }
                })
        } else {
            MyUtils.showToast(this@AddMoneyActivity, mContext!!.resources.getString(R.string.internetconnection))
        }
    }

    fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        val sha256 = digest.fold("") { str, it -> str + "%02x".format(it) }
        android.util.Log.e(TAG, "sha256 ============> $sha256")

        return sha256
    }

    fun encodeToBase64(text: String): String {
        val base64String = Base64.encodeToString(text.toByteArray(Charset.defaultCharset()), Base64.NO_WRAP)
        android.util.Log.e(TAG, "base64String =============> $base64String")
        return base64String
    }

    private fun phonePeAddWallet() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("deposit_amount", phonePeObject.getInt("amount"))
        jsonRequest.addProperty("transaction_id", phonePeObject.getString("merchantTransactionId"))
        jsonRequest.addProperty("order_id", phonePeObject.getString("merchantTransactionId"))
        jsonRequest.addProperty("payment_mode", "phonepe")
        jsonRequest.addProperty("payment_status", "success")
        jsonRequest.addProperty("base64", base64Body)
        jsonRequest.addProperty("checksum", checksum)

        if (appliedCouponCode != "" && isValidCoupon) {
            jsonRequest.addProperty("coupon", appliedCouponCode)
        }

        WebServiceClient(this).client.create(IApiMethod::class.java).addMoney(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>, t: Throwable) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(call: Call<UsersPostDBResponse?>, response: Response<UsersPostDBResponse?>) {
                    customeProgressDialog.dismiss()
                    val res = response.body()
                    if (res != null) {
                        if (res.status) {
                            setResult(Activity.RESULT_OK)
                            finish()
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

}