package ninja.cricks;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ninja.cricks.utils.BindingUtils;

public class PhonePeWebViewActivity extends AppCompatActivity {

    public static String TAG = PhonePeWebViewActivity.class.getSimpleName();

    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        // Replace "your_phone_pe_url" with the actual URL
        String phonePeUrl = getIntent().getStringExtra(BindingUtils.PHONE_PE_URL);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(0x00000000);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Handle page started loading
                Log.e(TAG, "Page started loading: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Handle page finished loading
                // https://rest.krsdata.net/api/v2/redirectURLPhonePe
                if (url.startsWith("https://rest.krsdata.net/api/v2/redirectURLPhonePe")) {
                    Log.e(TAG, "Response from phone pay: " + url);
                    finish(); // Close the activity when the desired URL is reached
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // Handle page resource error
                Log.e(TAG, "Page resource error code: " + error.getErrorCode() +
                        " description: " + error.getDescription() +
                        " errorCode: " + error.getErrorCode());
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                // Handle URL change
                Log.e(TAG, "URL change to " + url);
            }
        });

        webView.loadUrl(phonePeUrl);
    }
}



/*{
  "status": 200,
  "message": "success",
  "response": {
    "amount": 100,
    "user_id": "KUNA2020",
    "redirect_url": "https://rest.krsdata.net/api/v2/redirectURLPhonePe",
    "callback_url": "https://rest.krsdata.net/api/v2/callbackURLPhonePe",
    "order_id": "0202ANUKN1703848013",
    "merchant_id": "NINJA11ONLINE",
    "phonePeKey": "c246fadd-6523-4def-be15-685fc96aa160"
  }
}*/