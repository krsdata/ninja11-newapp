package ninja.cricks

import android.content.Context
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import ninja.cricks.databinding.ActivityHaodaPaymentBinding

class HaodaPaymentActivity : AppCompatActivity() {

    private var mBinding: ActivityHaodaPaymentBinding? = null
    var mContext: Context? = null
    private var URL: String? = null

    companion object {
        var TAG: String = HaodaPaymentActivity::class.java.simpleName
        const val KEY_URL: String = "key_url"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setEnterAnimations()
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_haoda_payment)

        mContext = this

        mBinding!!.toolbar.title = intent.getStringExtra(WebActivity.KEY_TITLE)
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            if (mBinding!!.webBody.canGoBack()) {
                mBinding!!.webBody.goBack()
            } else {
                finish()
            }
        }

        mBinding!!.progressBar.visibility = View.VISIBLE

        URL = intent.getStringExtra(WebActivity.KEY_URL)
        loadURL()
    }

    private fun setEnterAnimations() {
        val slide = Slide()
        slide.slideEdge = Gravity.BOTTOM
        slide.duration = 400
        slide.interpolator = DecelerateInterpolator()
        window.exitTransition = slide
        window.enterTransition = slide
    }

    private fun loadURL() {
        mBinding!!.webBody.webViewClient = MyWebViewClient()
        mBinding!!.webBody.settings.javaScriptEnabled = true
        mBinding!!.webBody.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        mBinding!!.webBody.clearCache(true)
        mBinding!!.webBody.loadUrl(URL!!)
    }

    override fun onBackPressed() {
        if (mBinding!!.webBody.canGoBack()) {
            mBinding!!.webBody.goBack()
        } else {
            finish()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event!!.action === KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (mBinding!!.webBody.canGoBack()) {
                        mBinding!!.webBody.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (mBinding!!.progressBar.isVisible) {
                mBinding!!.progressBar.visibility = View.GONE
            }
        }
    }
}