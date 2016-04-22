package myblog.richard.vewe.libactivities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONObject;

import myblog.richard.vewe.libusersprovider.UsersContract;

public class ActionActivity extends Activity {

    public static final String ACTION = "action";

    private WebView mWebView;
    private static final String tag = "action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        Intent i = getIntent();
        Action action  = (Action)i.getSerializableExtra(ACTION);
        Log.d(tag, "action " + action.getUrl());
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new MyWebViewClient(this, true));
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.loadUrl(action.getUrl());

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MyWebViewClient.REQUEST_PASSWORD_EXIT) {
            Log.d(tag, "password check result " + requestCode);
            if (resultCode == Activity.RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

}
