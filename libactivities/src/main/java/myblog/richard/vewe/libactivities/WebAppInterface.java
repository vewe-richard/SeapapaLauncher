package myblog.richard.vewe.libactivities;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by richard on 15-12-16.
 */
public class WebAppInterface {
    Activity mActivity;

    /** Instantiate the interface and set the context */
    WebAppInterface(Activity a) {
        mActivity = a;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mActivity, toast, Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void exit()
    {
        mActivity.setResult(Activity.RESULT_OK);
        mActivity.finish();
    }
}
