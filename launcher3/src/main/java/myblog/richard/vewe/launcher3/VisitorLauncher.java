package myblog.richard.vewe.launcher3;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import myblog.richard.vewe.libactivities.PasswordInput;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-17.
 */
public class VisitorLauncher extends Launcher {
    private static final String tag = "launcher";
    private static final boolean LOGD = true;
    private LauncherApplication mApplication;
    private static View mView;
    private boolean applistMayChanged = false;

    private static final int REQUEST_BASE = (REQUEST_LAST + 1000);
    private static final int REQUEST_PASSWORD_FOR_SETTING = REQUEST_BASE + 1;
    private static final int REQUEST_PASSWORD_FOR_LAUNCH_APPINFO = REQUEST_BASE + 2;
    private static final int REQUEST_PASSWORD_FOR_LAUNCH_SHORTCUT = REQUEST_BASE + 3;
    private static final int REQUEST_SETTING_FINISH = (REQUEST_BASE + 4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (LauncherApplication)getApplication();
        mApplication.setIntent(PendingIntent.getActivity(getBaseContext(), 0,
                new Intent(getIntent()), getIntent().getFlags()));

    }

    @Override
    protected void onClickAppInfo(View v) {
        clickAndLaunch(v);
    }

    @Override
    protected void onClickAppShortcut(View v) {
        clickAndLaunch(v);
    }

    private void clickAndLaunch(View v)
    {
        ComponentName comp = null;
        Object vtag = v.getTag();
        boolean isAppInfo = false;

        if(vtag instanceof AppInfo)
        {
            comp = ((AppInfo)vtag).componentName;
            isAppInfo = true;
        }
        else if(vtag instanceof ShortcutInfo)
        {
            comp = ((ShortcutInfo)vtag).getTargetComponent();
        }
        else{
            Log.e(tag, "Input must be a Shortcut or AppInfo");
            return;
        }

        int type = mApplication.getType(comp.getPackageName());
        if(type == UsersContract.TableApplist.FORBIDDEN)
        {
            mView = v;
            Intent i = new Intent(this,
                    myblog.richard.vewe.libactivities.PasswordCheckActivity.class);
            String password = mApplication.getPassword();
            i.putExtra(PasswordInput.PWD, password);
            if(isAppInfo) {
                startActivityForResult(i, REQUEST_PASSWORD_FOR_LAUNCH_APPINFO);
            }
            else
            {
                startActivityForResult(i, REQUEST_PASSWORD_FOR_LAUNCH_SHORTCUT);
            }
        }
        else
        {
            if(isAppInfo) super.onClickAppInfo(v);
            else super.onClickAppShortcut(v);
        }
    }

    @Override
    protected void onClickAddWidgetButton(View view) {
        Intent i = new Intent(this,
                myblog.richard.vewe.libactivities.PasswordCheckActivity.class);
        String password = mApplication.getPassword();
        if(LOGD) {
            Log.d(tag, "password " + password);
        }
        i.putExtra(PasswordInput.PWD, password);
        startActivityForResult(i, REQUEST_PASSWORD_FOR_SETTING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent i;
        boolean superHandled = false;

        if(requestCode == REQUEST_PASSWORD_FOR_SETTING)
        {
            if(resultCode ==RESULT_OK){
                i = new Intent(this, myblog.richard.vewe.libactivities.VisitorSettingActivity.class);
                startActivityForResult(i, REQUEST_SETTING_FINISH);
            }
        }
        else if(requestCode == REQUEST_SETTING_FINISH)
        {
            applistMayChanged = true;
        }
        else if(requestCode == REQUEST_PASSWORD_FOR_LAUNCH_APPINFO)
        {
            if (resultCode == RESULT_OK) {
                if(mView != null) {
                    super.onClickAppInfo(mView);
                }
            }
        }
        else if(requestCode == REQUEST_PASSWORD_FOR_LAUNCH_SHORTCUT)
        {
            if (resultCode == RESULT_OK) {
                if(mView != null) {
                    super.onClickAppShortcut(mView);
                }
            }
        }
        else {
            superHandled = true;
            super.onActivityResult(requestCode, resultCode, data);
        }

        if(superHandled == false)
        {
            setWaitingForResult(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(applistMayChanged == false)
            return;

        applistMayChanged = false;
        //regain applist and compare to previous one
        if(mApplication.applistChanged())
        {
            Log.d(tag, "restart needed");
            mApplication.restart();
        }
        else
        {
            Log.d(tag, "restart is not needed");
        }
    }
}
