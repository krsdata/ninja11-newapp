package mega.cricks

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import mega.cricks.network.IApiMethod
import mega.cricks.network.RequestModel
import mega.cricks.network.WebServiceClient
import mega.cricks.ui.BaseActivity
import mega.cricks.ui.home.models.UsersPostDBResponse
import mega.cricks.ui.login.LoginScreenActivity
import mega.cricks.utils.MyPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import mega.cricks.databinding.ActivitySplashBinding


class SplashScreenActivity : BaseActivity() {

    private lateinit var mContext: SplashScreenActivity
    private var mBinding: ActivitySplashBinding? = null
    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAYED : Long  = 4000
  //  val num = arrayOf(R.drawable.splash)
    internal  val mRunnable : Runnable = Runnable {
         if(!isFinishing){
               checkUserLoggedIn()
         }
    }

    override fun onStart() {
        super.onStart()

    }

    private fun checkUserLoggedIn() {

        if(!MyPreferences.getLoginStatus(mContext)!!){
            loginRequired()
        }else {

            val intent = Intent(
                applicationContext,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }


    private fun loginRequired() {

        val intent = Intent(applicationContext,
            LoginScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.CHECK_APK_UPDATE_API =false
        MainActivity.CHECK_WALLET_ONCE =false
        updateFireBase()
        mContext = this@SplashScreenActivity
        mBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_splash
        )
        updateCheckApk()
        var splashCreen = MyPreferences.getSplashScreen(this@SplashScreenActivity)

        if(!TextUtils.isEmpty(splashCreen)) {
//            Glide.with(this)
//                .load(splashCreen)
//                .placeholder(R.drawable.splash)
//                .into(mBinding!!.parentSplashBackground)
        }

        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAYED)

//        var infomodel = (application as SportsFightApplication).userInformations
//        if(infomodel!=null) {
//            BindingUtils.logFireBaseEvents(
//                this,
//                BindingUtils.FIREBASE_EVENT_ITEM_ID_SPLASHSCREEN,
//                infomodel!!.userId,
//                infomodel.fullName,
//                infomodel.userEmail
//            )
//        }
    }

//    private fun getRandomDrawable(): Int {
//       // val list = (1..num.size-1).filter { it % 2 == 0 }
//        var ran = Random.nextInt(0, num.size)
//        return num[ran]
//    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {


    }






    fun updateCheckApk() {
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.version_code = BuildConfig.VERSION_CODE

        WebServiceClient(this).client.create(IApiMethod::class.java).apkUpdate(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    MainActivity.CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {

                    var res = response!!.body()
                    if(!isFinishing){
                        if(res!=null) {
                            if(res.status){
                                MainActivity.CHECK_APK_UPDATE_API = true
                                MainActivity.CHECK_FORCE_UPDATE = res.forceupdate
                                MainActivity.updatedApkUrl = res.updatedApkUrl
                                MainActivity.releaseNote = res.releaseNote

                                MyPreferences.setSplashScreen(this@SplashScreenActivity,res.splash)
                            }

                        }
                    }


                }

            })

    }

}
