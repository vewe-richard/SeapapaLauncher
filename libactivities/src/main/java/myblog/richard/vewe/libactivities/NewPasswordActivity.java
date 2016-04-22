package myblog.richard.vewe.libactivities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;

/**
 * Created by richard on 15-10-22.
 */
public class NewPasswordActivity extends PasswordInput {
    private boolean mFirst;
    private String mFirstEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.password_create) + getHint());

        mFirst = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void confirm(String pwdstr) {
        if(mFirst)
        {
            mFirst = false;
            mFirstEnter = pwdstr;
            reset(null);
            setTitle(getString(R.string.password_again));
        }
        else
        {
            if(mFirstEnter.contentEquals(pwdstr))
            {
                Log.d(tag, "new password " + pwdstr);
                Intent i = new Intent();
                i.putExtra(PasswordInput.PWD, pwdstr);
                setResult(RESULT_OK, i);
                finish();
            }
            else
            {
                mFirst = true;
                setTitle(getString(R.string.password_unmatch) + getHint());
                reset(null);
            }
        }
    }
}
