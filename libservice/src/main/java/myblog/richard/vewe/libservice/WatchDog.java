package myblog.richard.vewe.libservice;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import myblog.richard.vewe.libusersprovider.App;
import myblog.richard.vewe.libusersprovider.DeviceSpecific;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-25.
 */
public class WatchDog {
    private static final String tag = "watchdogzz";
    private static final boolean LOGD = true;

    private MyService mService;
    private MainHandler mMainHandler;
    private PackageManager mManager;
    private boolean mIsMyLauncher = true;

    WatchDog(MyService service, ActivityManager am, PackageManager manager)
    {
        mService = service;
        mMainHandler = mService.getmMainHandler();
        mManager = manager;
        mCurrentState = mNormalState;
        LoadAllLaunchers();
        createAD4Selector();
    }

    public void handleMessage(Message msg) {
        String pkg;
        Bundle b;
        switch (msg.what) {
            case MessageType.Request.SET_AUTHORISED:
                b = msg.getData();
                pkg = b.getString(MessageType.Request.SERL1);
                if(pkg == null) break;
                if(pkg.contentEquals(LAUNCHER_AUTHORISED))
                {
                    mIsLauncherAuthorised = true;
                    mWaitForAuthorisedLauncher = false;
                }
                else
                {
                    mAuthorisedPkg = pkg;
                }
                break;
            case MessageType.Request.CLEAR_AUTHORISED:
                b = msg.getData();
                pkg = b.getString(MessageType.Request.SERL1);
                if(pkg == null) break;
                if(pkg.contentEquals(LAUNCHER_AUTHORISED))
                {
                    mIsLauncherAuthorised = false;
                    mWaitForAuthorisedLauncher = false;
                }
                else
                {
                    mAuthorisedPkg = null;
                }
                break;
        }
    }

    public boolean watchTask(String pkg, int type)
    {
        if(LOGD) {
            Log.d(tag, "" + mCurrentState + "=>pkg: " + pkg + " type: " + type);
        }
        mCurrentState.onPackage(pkg, type);

        return true;
    }

    private String mAuthorisedPkg = null;

    private State mCurrentState;

    private State mNormalState = new NormalState(), mLauncherSelectorState = new LauncherSelectorState();

    private int mSelectCount = 0;
    private static final int MAX_SELECT_COUNT = 4;

    abstract class State{
        abstract void onPackage(String pkg, int type);
    }

    class NormalState extends State{
        @Override
        public String toString() {
            return "NormalState{}";
        }

        @Override
        void onPackage(String pkg, int type) {
            //different procedure for launcher and activity
            if(isLauncher(pkg))
            {
                Log.d(tag, "pkg is launcher");
                //for xiaomi device, the launcher selector would not work
                if(DeviceSpecific.getInstance().getType() == DeviceSpecific.GOOGLE_TYPE) {
                    onLauncher(pkg, type);
                }
                else {
                    if (pkg.contentEquals(mService.getPackageName())) {
                        mIsMyLauncher = true;
                    } else {
                        mIsMyLauncher = false;
                    }
                }
            }
            else
            {
                Log.d(tag, "pkg is activity, authorised:" + mAuthorisedPkg);
                //it would not be my case to handle the case if it is not my launcher
                if(mIsMyLauncher) {
                    onActivity(pkg, type);
                }
            }
        }

        private void onLauncher(String pkg, int type)
        {
            mPreviousFreeRunPackage = pkg;
            if(mIsLauncherAuthorised) return;

            if(pkg.contentEquals(mService.getPackageName()))
            {
                if(!isMyAppLauncherPreferred())
                {
                    mAD4Selector.show();
                    mCurrentState = mLauncherSelectorState;
                }
                else if(!isMyAppLauncherDefault())
                {
                    Log.d(tag, "my launcher is not the default, select count " + mSelectCount);
                    if(mSelectCount < MAX_SELECT_COUNT)
                    {
                        mAD4Selector.show();
                        mCurrentState = mLauncherSelectorState;
                    }
                }
                mIsMyLauncher = true;
            }
            else
            {  //it's on another type of launcher, so, ask the user to switch it
                mAD4Selector.show();
                mCurrentState = mLauncherSelectorState;
                mIsMyLauncher = false;
            }
        }

        private String mPreviousFreeRunPackage;
        private void onActivity(String pkg, int type)
        {
            boolean isLocked = false;
            String reason = null;
            String hint = null;

            if(type == UsersContract.TableApplist.FORBIDDEN)
            {
                isLocked = true;
                try {
                    reason = mManager.getApplicationLabel(mManager.getApplicationInfo(pkg, 0)).toString();
                }catch(Exception e)
                {
                    reason = pkg;
                }
                reason += UsersContract.TableApplist.FORBIDDEN_PREFIX;
            }
            else if(type == UsersContract.TableApplist.FREE)
            {
                isLocked = false;
            }
            else
            {
                //query lock and reason
                LockAndReason lr = getLockAndReason(pkg);
                isLocked = lr.locked;
                reason = lr.reason;
                hint = lr.hint;
            }

            if(isLocked == false)
            {
                mPreviousFreeRunPackage = pkg;
                mAD4HintDisplayed = false;
                return;
            }

            if(mAuthorisedPkg != null && mAuthorisedPkg.contentEquals(pkg)) return;

            Log.d(tag, "reason: " + reason + " hint:" + hint);
            //even hint is available, if user get started from recent UI, it should be blocked
            //so, it should check if previous free run package is the same package
            //if hint is available, display hint, but only for once, and return
            if(mPreviousFreeRunPackage != null && mPreviousFreeRunPackage.contentEquals(pkg))
            {
                if(hint != null)
                {
                    //now show the hint
                    if(!mAD4HintDisplayed) {
                        showAD4Hint(hint);
                        mAD4HintDisplayed = true;
                    }
                    return;
                }
            }
            mPreviousFreeRunPackage = null;

            //if reason is available, display password input activity
            //after it is displayed, the routine go to the launcher handle
            if(reason != null)
            {
                showPasswordLock(type, pkg, reason);
            }
        }
    }

    private void showPasswordLock(int type, String pkg, String reason)
    {
        String[] passwords = mMainHandler.getPasswords();
        String password;

        if(passwords == null || passwords[0] == null) return;

        Intent i = new Intent();
        i.setComponent(new ComponentName(mService.getPackageName(),
                "myblog.richard.vewe.libactivities.PasswordCheckByService"));

        password = passwords[0];
        if(type == UsersContract.TableApplist.FORBIDDEN) {
            i.putExtra(PWD2, passwords[1]);
        }

        i.putExtra(PWD, password);

        if(reason != null)
        {
            i.putExtra(INFO, reason);
        }

        i.putExtra(PKG, pkg);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mService.startActivity(i);
        return;
    }

    private boolean mAD4HintDisplayed;
    private void showAD4Hint(String hint)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mService);
        builder.setMessage(hint);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog ad = builder.create();
        ad.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        ad.show();
    }

    private class LockAndReason{
        boolean locked;
        String reason;
        String hint;

        public LockAndReason(boolean locked, String reason, String hint) {
            this.locked = locked;
            this.reason = reason;
            this.hint = hint;
        }
    }

    private LockAndReason getLockAndReason(String pkgName)
    {
        LockAndReason lr = new LockAndReason(false, null, null);

        ContentResolver resolver = mService.getContentResolver();
        String selection = UsersContract.TableApplist.Column.PKG + "=" + pkgName;
        Cursor cursor = resolver.query(
                UsersContract.TableApplist.DIGA_URI,
                new String[]{UsersContract.TableApplist.Column.LOCK, UsersContract.TableApplist.Column.REASON,
                        UsersContract.TableApplist.Column.HINT},
                selection,
                null,
                null);
        if(cursor == null || cursor.getCount() != 1)
        {
            Log.e(tag, "could not query " + pkgName + " from applist");
            return lr;
        }
        cursor.moveToFirst();
        App app = new App(cursor);
        cursor.close();
        lr.locked = app.isLocked();
        lr.reason = app.getReason();
        lr.hint = app.getHint();
        return lr;
    }

    class LauncherSelectorState extends State{
        @Override
        public String toString() {
            return "LauncherSelectorState{}";
        }

        @Override
        void onPackage(String pkg, int type) {
            if(isLauncher(pkg))
            {
                Log.d(tag, "pkg is launcher: waitAuthorised " + mWaitForAuthorisedLauncher + " Authorised: " + mIsLauncherAuthorised);
                //if it is my launcher
                onLauncher(pkg, type);
            }
            else {
                Log.d(tag, "pkg is activity");
                onActivity(pkg, type);
            }
        }

        private void onLauncher(String pkg, int type)
        {
            boolean isMyLauncher = pkg.contentEquals(mService.getPackageName());

            if(mIsLauncherAuthorised) return;

            if(isMyLauncher && isMyAppLauncherPreferred()) {
                if(isMyAppLauncherDefault() || mSelectCount >= MAX_SELECT_COUNT)
                {
                    mCurrentState = mNormalState;
                    return;
                }
                Log.d(tag, "my launcher is not the default one, select count:" + mSelectCount);
            }

            if(mAD4Selector.isShowing()) return;

            //user just click ok to start the selector, so, don't show it immediately
            if(mStartSelector){
                mStartSelectorCounter ++;
                if(mStartSelectorCounter < 3) {
                    return;
                }
                //reset it to false
                mStartSelector = false;
                mStartSelectorCounter = 0;
            }

            //if we are waiting user to enter password
            if(mWaitForAuthorisedLauncher && isMyLauncher) return;

            //Let judge if we should show the alert dialog again
            mAD4Selector.show();
        }

        private void onActivity(String pkg, int type)
        {
            mStartSelector = false;
            mStartSelectorCounter = 0;
            mCurrentState = mNormalState;
        }
    }

    private AlertDialog mAD4Selector;
    private int mFakeFlag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    private boolean mStartSelector;
    private int mStartSelectorCounter;

    private boolean mWaitForAuthorisedLauncher;
    private boolean mIsLauncherAuthorised;

    private void createAD4Selector()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mService);
        builder.setMessage(R.string.choose_this_desktop);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ComponentName componentName = new ComponentName(mService, FAKE_LAUNCHER);

                mManager.setComponentEnabledSetting(componentName, mFakeFlag, PackageManager.DONT_KILL_APP);

                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mService.startActivity(selector);

                //mManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                if (mFakeFlag == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    mFakeFlag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
                } else {
                    mFakeFlag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                }
                mStartSelector = true;
                mStartSelectorCounter = 0;

                mSelectCount ++;
            }
        });
        builder.setNeutralButton(R.string.password_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent();
                i.setComponent(new ComponentName(mService.getPackageName(),
                        "myblog.richard.vewe.libactivities.PasswordCheckByService"));
                String[] passwords = mMainHandler.getPasswords();
                String password;
                mWaitForAuthorisedLauncher = true;

                password = passwords[0];
                i.putExtra(PWD, password);

                i.putExtra(PKG, LAUNCHER_AUTHORISED);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mService.startActivity(i);
            }
        });
        mAD4Selector = builder.create();
        mAD4Selector.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    //---------------------------- NO relative to state -----------------------
    private List<ResolveInfo> mLauncherList;

    private void LoadAllLaunchers()
    {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_HOME);

        mLauncherList = mManager.queryIntentActivities(mainIntent, 0);
        if(LOGD){
            for(ResolveInfo info : mLauncherList)
            {
                Log.d(tag, "launcher "  + info.activityInfo.packageName);
            }
        }
    }

    private boolean isLauncher(String pkgName)
    {
        for (ResolveInfo info : mLauncherList) {
            if(info.activityInfo.packageName.contentEquals(pkgName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMyAppLauncherPreferred() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = mService.getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();

        mManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                if(LOGD){
                    Log.d(tag, "mylauncher is preferred one");
                }
                return true;
            }
        }
        if(LOGD){
            Log.d(tag, "mylauncher is not the preffered one");
        }
        return false;
    }

    private boolean isMyAppLauncherDefault()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = mManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        return currentHomePackage.contentEquals(mService.getPackageName());
    }

    //----------------------- Copy from passwordinput ----------------
    private static final String PWD = "pwd";
    private static final String TITLE = "title";
    private static final String INITIAL = "initial";
    private static final String INFO = "info";
    private static final String PWD2 = "pwd2";
    private static final String PKG = "pkg";
    //-----------------------------------------------------------------
    private static final String LAUNCHER_AUTHORISED = "launcher_authorised";
    private static final String FAKE_LAUNCHER = "myblog.richard.vewe.launcher3.FakeLauncher";
}
