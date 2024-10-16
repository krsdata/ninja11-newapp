package ninja.cricks

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import ninja.cricks.databinding.ActivityHaodaPaymentBinding
import ninja.cricks.utils.BindingUtils

class PhonePeWebViewActivity : AppCompatActivity() {

    var mBinding: ActivityHaodaPaymentBinding? = null
    var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_haoda_payment)
        mContext = this
        mBinding!!.apply {
            toolbar.title = intent.getStringExtra(WebActivity.KEY_TITLE)
            toolbar.setTitleTextColor(resources.getColor(R.color.white))
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener { v: View? ->
                finish()
            }
            progressBar.visibility = View.VISIBLE

            webBody.settings.javaScriptEnabled = true
            webBody.setBackgroundColor(0x00ffffff)
            webBody.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webBody.clearCache(true)
        }
        // Replace "your_phone_pe_url" with the actual URL
        val phonePeUrl = intent.getStringExtra(BindingUtils.PHONE_PE_URL)


        mBinding!!.webBody.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (mBinding!!.progressBar.isVisible) {
                    mBinding!!.progressBar.visibility = View.GONE
                }
                // Handle page finished loading
                if (url.startsWith("https://rest.ninja11.in/api/v3/paytmCallback")) {
                    Log.e(TAG, "Response from phone pay: $url")
                    finish() // Close the activity when the desired URL is reached
                }
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                // Handle URL change
                Log.e(TAG, "URL change to $url")
            }
        }
        mBinding!!.webBody.loadUrl(phonePeUrl!!)
    }

    companion object {
        var TAG = PhonePeWebViewActivity::class.java.simpleName
    }
}