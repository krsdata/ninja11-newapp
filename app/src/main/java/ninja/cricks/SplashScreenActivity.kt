package ninja.cricks

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import io.branch.referral.Branch
import ninja.cricks.databinding.ActivitySplashBinding
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RetrofitClient
import ninja.cricks.ui.BaseActivity
import ninja.cricks.ui.login.LoginScreenActivity
import ninja.cricks.utils.MyPreferences
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SplashScreenActivity : BaseActivity() {

    private lateinit var mContext: Context
    private var mBinding: ActivitySplashBinding? = null
    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAYED: Long = 2500
    private var TAG: String = SplashScreenActivity::class.java.simpleName

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            checkUserLoggedIn()
        }
    }

    private fun checkUserLoggedIn() {
        if (!MyPreferences.getLoginStatus(mContext)!!) {
            loginRequired()
        } else {
            val intent = Intent(
                applicationContext,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }

    private fun loginRequired() {
        val intent = Intent(
            applicationContext,
            LoginScreenActivity::class.java
        )
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_splash
        )

        MainActivity.CHECK_APK_UPDATE_API = false
        MainActivity.CHECK_WALLET_ONCE = false
        updateFireBase()

        val splashScreen = MyPreferences.getSplashScreen(mContext)

        if (splashScreen != null && splashScreen != "") {
            Log.e(TAG, "splashScreen =======> $splashScreen")
            if (splashScreen.contains(".gif")) {
                Glide.with(mContext).asGif()
                    .load(splashScreen)
                    .placeholder(R.drawable.splash_ninja_red_new)
                    .into(mBinding!!.splashView)
            } else {
                Glide.with(mContext)
                    .load(splashScreen)
                    .placeholder(R.drawable.splash_ninja_red_new)
                    .into(mBinding!!.splashView)
            }
        } else {
            Glide.with(mContext)
                .load(R.drawable.splash_ninja_red_new)
                .placeholder(R.drawable.splash_ninja_red_new)
                .into(mBinding!!.splashView)
        }

        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAYED)

        val branch = Branch.getAutoInstance(mContext)
        branch.setRetryCount(5)

        branch.initSession({ referringParams, error ->
            if (error != null) {
                //Log.e("onCreate", error.getMessage());
            } else if (referringParams != null) {
                //Log.e("onCreate", referringParams.toString());
                if (referringParams.has("refer_code")) {
                    MyPreferences.setTempReferCode(
                        mContext,
                        referringParams.optString("refer_code")
                    )
                }
            }
        }, this.intent.data, this)
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }


    override fun onStart() {
        super.onStart()
        val intent = this.intent
        intent.putExtra("branch_force_new_session", true)
        Branch.getInstance()
            .initSession({ branchUniversalObject, linkProperties, error ->

            }, intent.data, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Branch.getInstance()
            .reInitSession(
                this
            ) { referringParams, error ->
                if (error != null) {
                    //Log.e("onCreate", error.getMessage());
                } else if (referringParams != null) {
                    //Log.e("onCreate", referringParams.toString());
                    if (referringParams.has("refer_code")) {
                        MyPreferences.setTempReferCode(
                            mContext,
                            referringParams.optString("refer_code")
                        )
                    }
                }
            }
    }
}