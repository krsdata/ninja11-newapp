package ninja.cricks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch.BranchLinkCreateListener
import io.branch.referral.util.LinkProperties
import io.branch.referral.util.ShareSheetStyle
import ninja.cricks.databinding.InviteFriendsBinding
import ninja.cricks.models.UserInfo
import ninja.cricks.utils.MyPreferences
import java.util.*
import java.util.prefs.Preferences

class InviteFriendsActivity : AppCompatActivity() {

    var userInfo: UserInfo? = null

    private var mBinding: InviteFriendsBinding? = null
    var url: String? = null
    var lp: LinkProperties? = null
    var buo: BranchUniversalObject? = null
    var shareSheetStyle: ShareSheetStyle? = null
    var mContext: Context? = null

    var TAG: String = InviteFriendsActivity::class.java.simpleName

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.invite_friends)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.invite_friends
        )

        mContext = this

        mBinding!!.toolbar.title = "Refer & Earn"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        userInfo = (application as NinjaApplication).userInformations

        mBinding!!.rereralCode.text = userInfo!!.referalCode

        buo = BranchUniversalObject()
            .setTitle("Ninja11")
            .setContentDescription("Cricket Fantasy App")
            .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
            .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)

        lp = LinkProperties()
            .setFeature("sharing")
            .setCampaign("launch")
            .setStage("new user")
            .addControlParameter(
                "refer_code",
                userInfo!!.referalCode
            )
            .addControlParameter(
                "custom_random",
                java.lang.Long.toString(Calendar.getInstance().timeInMillis)
            )

        if (MyPreferences.getInviteUrl(mContext!!)!! == "") {
            buo!!.generateShortUrl(mContext!!, lp!!,
                BranchLinkCreateListener { url, error ->
                    if (error == null) {
                        this@InviteFriendsActivity.url = url
                        MyPreferences.setInviteUrl(mContext!!, url)

                        Log.e(TAG, "got my Branch link to share  =====> $url")
                        FirebaseCrashlytics.getInstance().log("share url =========> $url")
                    } else {
                        FirebaseCrashlytics.getInstance()
                            .log("error generating url =========> $error")
                        Log.e(TAG, "error errorCode =========> ${error.errorCode}")
                        Log.e(TAG, "error message =========> ${error.message}")
                    }
                })
        } else {
            url = MyPreferences.getInviteUrl(mContext!!)!!
        }

        shareSheetStyle = ShareSheetStyle(
            mContext!!, "Ninja11",
            "Welcome to Ninja11. Register on Ninja11 application with this link.\n\nUse my referral code \"" + userInfo!!.referalCode +
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/InviteFriendsActivity.kt
                    "\" and get extra Rs. 100 Bonus on Joining.".trimIndent()
        )
            .setAsFullWidthStyle(false)
            .setDefaultURL(url)
            .setSharingTitle("Refer and Earn Rs 100")
=======
                    "\" and get Joining Bonus ₹100. ".trimIndent()
        )
            .setAsFullWidthStyle(false)
            .setDefaultURL(url)
            .setSharingTitle("Refer and Earn ₹25")
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/InviteFriendsActivity.kt

        mBinding!!.inviteFriends.setOnClickListener {
            shareReferCode()
        }
    }

    fun setAnimation() {
        val slide = Slide()
        slide.slideEdge = Gravity.START
        slide.duration = 400
        slide.interpolator = DecelerateInterpolator()
        window.exitTransition = slide
        window.enterTransition = slide
    }

    private fun shareReferCode() {

        if (url == null || url == "") {
            url = "https://www.ninja11.in/Ninja11.apk"
        }

        val msg: String =
            "Welcome to Ninja11.\n\nRegister on Ninja11 application with this link.\n\nUse my referral code \"" + userInfo!!.referalCode +
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/InviteFriendsActivity.kt
                    "\" and get extra Rs. 100 Bonus on Joining.\n\n $url".trimIndent()
=======
                    "\" and get ₹100 Bonus on Joining.\n\n $url".trimIndent()
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/InviteFriendsActivity.kt
        FirebaseCrashlytics.getInstance().log("share message =========> $msg")

        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg)
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Ninja11")
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, "Ninja11")
        startActivity(shareIntent)
    }
}