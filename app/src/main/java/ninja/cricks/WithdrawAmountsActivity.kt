package ninja.cricks

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.anim.FlashAnim
import ninja.cricks.databinding.ActivityWithdrawAmountBinding
import ninja.cricks.models.WalletInfo
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RequestModel
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseActivity
import ninja.cricks.ui.home.models.UsersPostDBResponse
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WithdrawAmountsActivity : BaseActivity() {

    private var walletInfo: WalletInfo? = null
    private var mBinding: ActivityWithdrawAmountBinding? = null
    private var pageType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(this)
        userInfo = (application as SportsFightApplication).userInformations
        walletInfo = (application as SportsFightApplication).walletInfo
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_withdraw_amount
        )
        mBinding!!.toolbar.title = "Withdraw Money"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.winningAmount.text = String.format("₹ %s", walletInfo!!.prizeAmount)

        //mBinding!!.viewAmount.visibility = View.GONE

        mBinding!!.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            ////Log.e(TAG, "checkedId =====> " + checkedId);
            if (rb != null) {
                if (rb.text.toString().equals("paytm", true)) {
                    pageType = "paytm"
                } else {
                    pageType = "bank account"
                }
                //mBinding!!.viewAmount.visibility = View.VISIBLE
                ////Log.e(TAG, "pageType ======> " + pageType);
            }
        }

        mBinding!!.submitBtnWithdrawal.setOnClickListener(View.OnClickListener {
            var value = mBinding!!.editWithdrawalAmount.text.toString().trim()
            if (TextUtils.isEmpty(value)) {
                value = "0"
            }
            val amount = value.toInt()
            if (!pageType.equals("")) {
                if (amount >= 200) {
                    showWithdrawalAlert(amount, pageType)
                } else {
                    MyUtils.showMessage(
                        this@WithdrawAmountsActivity,
                        "You cannot withdraw amount less than 200 INR"
                    )
                }
            } else {
                MyUtils.showMessage(
                    this@WithdrawAmountsActivity,
                    "Select amount withdraw type"
                )
            }
        })

        mBinding!!.contactUs.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@WithdrawAmountsActivity, SupportActivity::class.java)
            startActivity(intent)
        })
    }

    private fun showWithdrawalAlert(amount: Int, type: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Confirmation")
        //set message for alert dialog
        if (type.equals("")) {
            builder.setMessage(
                String.format(
                    "%d will be transferred to your verified bank accounts",
                    amount
                )
            )
        } else {
            builder.setMessage(
                String.format(
                    "%d will be transferred to your verified Paytm account",
                    amount
                )
            )
        }
        builder.setIcon(android.R.drawable.ic_btn_speak_now)

        //performing positive action
        builder.setPositiveButton("Proceed") { dialogInterface, which ->
            withdrawalRequest(amount, type)
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun withdrawalRequest(amount: Int, type: String) {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.token = MyPreferences.getToken(this)!!
        models.withdraw_amount = amount
        models.payment_taken_in = type

        WebServiceClient(this).client.create(IApiMethod::class.java).withdrawAmount(models)
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
                    if (res != null && res.status) {
                        successAlert(res.message, true)
                    } else {
                        if (res != null) {
                            successAlert(res.message, false)
                        } else {
                            successAlert("Please try again! Something went wrong", false)
                        }
                    }
                }
            })
    }

    private fun successAlert(message: String, isClose: Boolean) {
        val flashbar = Flashbar.Builder(this@WithdrawAmountsActivity)
            .gravity(Flashbar.Gravity.TOP)
            .title(getString(R.string.app_name))
            .message(message)
            .backgroundDrawable(R.color.green)
            /*.showIcon()
            .icon(R.drawable.ic_photo_camera_black_24dp)
            .iconAnimation(
                FlashAnim.with(this@WithdrawAmountsActivity)
                    .animateIcon()
                    .pulse()
                    .alpha()
                    .duration(750)
                    .accelerate()
            )*/
            .build()
        flashbar.show()
        Handler().postDelayed(Runnable {
            if (isClose) {
                setResult(Activity.RESULT_OK)
                finish()
            }

        }, 2000L)
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }
}