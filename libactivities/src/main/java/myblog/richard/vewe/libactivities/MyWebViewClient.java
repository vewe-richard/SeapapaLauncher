package myblog.richard.vewe.libactivities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-12-14.
 */
public class MyWebViewClient extends WebViewClient {
    private static final String tag = "webview";
    private static final boolean LOGD = true;
    private Activity mActivity;
    public static final int REQUEST_SET_PASSWORD = 2;
    public static final int REQUEST_PASSWORD_QA = 3;
    public static final int REQUEST_PASSWORD_EXIT = 4;
    public static final int REQUEST_SETTINGS = 5;
    private boolean mExitOnError;

    MyWebViewClient(Activity activity)
    {
        mActivity = activity;
    }

    MyWebViewClient(Activity activity, boolean exitOnError)
    {
        mActivity = activity;
        mExitOnError = exitOnError;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Log.d(tag, "failed to load " + failingUrl);
        if(mExitOnError){
            mActivity.setResult(Activity.RESULT_OK);
            mActivity.finish();
        }
    }

    private String getPassword()
    {
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
        Cursor cursor = mActivity.getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
                new String[] {UsersContract.TableUsers.Column.PASSWORD},
                selection,
                null,
                null);
        if(cursor == null || cursor.getCount() != 1)
        {
            Log.e(tag, "failed to query root password");
            return null;
        }
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(UsersContract.TableUsers.Column.PASSWORD);
        String ret = cursor.getString(index);
        cursor.close();
        return ret;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(LOGD) {
            Log.d(tag, "url " + url);
        }

        if(!url.contains("/myblog2/")){
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(intent);
            return true;
        }

        if(url.contains("local_password_change")){
            Log.d(tag, "call password change activity");
            Intent i = new Intent(mActivity,
                    myblog.richard.vewe.libactivities.NewPasswordActivity.class);
            mActivity.startActivityForResult(i, REQUEST_SET_PASSWORD);
            return true;
        }
        else if(url.contains("local_password_qa")){
            Log.d(tag, "call password question answer");
            Intent i = new Intent(mActivity,
                    myblog.richard.vewe.libactivities.PasswordQA2.class);
            mActivity.startActivityForResult(i, REQUEST_PASSWORD_QA);
            return true;
        }
        else if(url.contains("local_password_exit")){
            Log.d(tag, "call password to exit");
            Intent i = new Intent(mActivity,
                    myblog.richard.vewe.libactivities.PasswordCheckActivity.class);
            i.putExtra(PasswordInput.PWD, getPassword());
            mActivity.startActivityForResult(i, REQUEST_PASSWORD_EXIT);
            return true;
        }
        else if(url.contains("local_settings")){
            Intent i = new Intent(Settings.ACTION_SETTINGS);
            mActivity.startActivityForResult(i, REQUEST_SETTINGS);
            return true;
        }
        else if(url.contains("local_exit"))
        {
            mActivity.finish();
        }

        return false;
    }
}
