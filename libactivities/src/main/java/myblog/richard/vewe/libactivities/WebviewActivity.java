package myblog.richard.vewe.libactivities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.Toast;

import myblog.richard.vewe.libusersprovider.UsersContract;

public class WebviewActivity extends Activity {

    public static final String URL = "url";
    public static final String STRING = "string";

    private WebView mWebView;
    private static final String tag = "webviewAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_install);

        Intent i = getIntent();
        String url  = i.getStringExtra(URL);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new MyWebViewClient(this));
        if(url != null) {
            mWebView.loadUrl(url);
        }
        else{
            String str = i.getStringExtra(STRING);
            if(str != null){
                mWebView.loadDataWithBaseURL("", str, "text/html", "UTF-8", "");
            }
        }
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
        if (requestCode == MyWebViewClient.REQUEST_SET_PASSWORD) {
            if (resultCode == Activity.RESULT_OK) {
                String newpwd = data.getStringExtra(PasswordInput.PWD);
                Log.d(tag, "set password ok: " + newpwd);
                if (newpwd == null) return;
                //update to content provider
                ContentResolver resolver = getContentResolver();
                ContentValues update = new ContentValues();
                update.put(UsersContract.TableUsers.Column.PASSWORD, newpwd);

                int rowsUpdated;
                String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
                rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                        update,
                        selection,
                        null);
                if (rowsUpdated != 1) {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    @Override
    protected void onResume() {
        UsersContract.FIRST_TIME_INSTALLATION = true;
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        UsersContract.FIRST_TIME_INSTALLATION = false;
    }
}
