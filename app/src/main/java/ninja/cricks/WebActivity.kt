package ninja.cricks

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import ninja.cricks.databinding.WebviewBinding
import ninja.cricks.ui.BaseActivity


class WebActivity : BaseActivity() {
    private var mBinding: WebviewBinding? = null

    companion object {
        var TAG: String = WebActivity::class.java.simpleName
        const val KEY_TITLE: String = "web.title"
        const val KEY_URL: String = "url.web"
        const val USER_ID: String = "user_id"
    }

    private var URL: String? = null
    private var userId: String? = ""

    public override fun onCreate(savedInstanceState: Bundle?) {
        setEnterAnimations()
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.webview
        )
        mBinding!!.toolbar.title = intent.getStringExtra(KEY_TITLE)
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        customeProgressDialog.show()
        URL = intent.getStringExtra(KEY_URL)
        userId = intent.getStringExtra(USER_ID)
        loadURL()
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
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
        if (userId != null && !userId.equals("")) {
            mBinding!!.webBody.loadUrl(URL + userId)
        } else {
            mBinding!!.webBody.loadUrl(URL)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            customeProgressDialog.dismiss()
        }
    }
}