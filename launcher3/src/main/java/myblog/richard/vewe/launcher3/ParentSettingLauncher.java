package myblog.richard.vewe.launcher3;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import myblog.richard.vewe.libactivities.Action;
import myblog.richard.vewe.libactivities.ActionActivity;
import myblog.richard.vewe.libactivities.PasswordInput;
import myblog.richard.vewe.libactivities.WebviewActivity;
import myblog.richard.vewe.libusersprovider.DeviceSpecific;
import myblog.richard.vewe.libusersprovider.PackageType;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-17.
 */
public class ParentSettingLauncher extends Launcher {
    private static final String tag = "pslauncher";
    private static final boolean LOGD = true;
    private LauncherApplication mApplication;
    private View mView;
    private String mPackage;
    private boolean applistMayChanged = false;
    private boolean firstCallResume = true;

    private static final int REQUEST_BASE = (REQUEST_LAST + 1000);
    private static final int REQUEST_PASSWORD_FOR_PARENT_SETTING = REQUEST_BASE + 1;
    private static final int REQUEST_PASSWORD_FOR_LAUNCH_APPINFO = REQUEST_BASE + 2;
    private static final int REQUEST_PASSWORD_FOR_LAUNCH_SHORTCUT = REQUEST_BASE + 3;
    private static final int REQUEST_PASSWORD_FOR_GROUPING = (REQUEST_BASE + 4);
    private static final int REQUEST_ALL_APPLIST = (REQUEST_BASE + 5);
    private static final int REQUEST_DISABLE_PARENT_CONTROL = (REQUEST_BASE + 6);
    private static final int REQUEST_ACTION = (REQUEST_BASE + 7);
    private static final int REQUEST_PASSWORD_FOR_STUDENT_SETTING = REQUEST_BASE + 8;
    private static final int REQUEST_END_GROUPING = REQUEST_BASE + 9;
    private static final int REQUEST_ACCESS_INFO = REQUEST_BASE + 11;

    private Switch mStudentModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (LauncherApplication)getApplication();
        mApplication.setIntent(PendingIntent.getActivity(getBaseContext(), 0,
                new Intent(getIntent()), getIntent().getFlags()));
        defaultLauncherCheck();
//        showIcon();
//        setupIconHandle();

        //Check if we are in the first time installation
        String pwd = mApplication.getPassword();
//        Log.d(tag, "pwd: " + pwd + " installdate " + mApplication.installDate());
        if(pwd != null && pwd.contentEquals(UsersContract.TableUsers.DEFAULT_ROOT_PWD) && mApplication.installDate() == -1)
//        if(true)
        {
            Log.d(tag, "this is first time installation");
            mApplication.setInstallDate();
            firstTimeInstallation();
        }

        setupMainEntriesHandle();

        if(Build.VERSION.SDK_INT > 20 && permissionNotGarented()) {
            ContentValues values = new ContentValues();
            values.put(UsersContract.TableApplist.Column.PKG, "com.android.settings");
            values.put(UsersContract.TableApplist.Column.TYPE, UsersContract.TableApplist.FREE);

            getContentResolver().insert(UsersContract.TableApplist.DIGA_URI, values);

            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityForResult(intent, REQUEST_ACCESS_INFO);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean permissionNotGarented(){
        final UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0,  System.currentTimeMillis());

        return queryUsageStats.isEmpty();
    }

    private void setupMainEntriesHandle()
    {
        ViewGroup vg = getOverviewPanel();
        TextView appRecords = (TextView)vg.findViewById(R.id.app_records_button);
        TextView appTypes = (TextView)vg.findViewById(R.id.app_types_button);
        TextView userGuide = (TextView)vg.findViewById(R.id.user_guide_button);
        TextView studentSettings = (TextView)vg.findViewById(R.id.student_setting_button);
        TextView parentSettings = (TextView)vg.findViewById(R.id.parent_setting_button);
        Switch studentMode = (Switch)vg.findViewById(R.id.student_mode);

        appRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ParentSettingLauncher.this, myblog.richard.vewe.libactivities.AppRecordsActivity.class);
                startActivity(i);
            }
        });
        appTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getPasswordCheckIntent(null, false);
                startActivityForResult(i, REQUEST_PASSWORD_FOR_GROUPING);
            }
        });
        userGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(tag, "click user guide");
            }
        });
        studentSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getPasswordCheckIntent(null, false);
                startActivityForResult(i, REQUEST_PASSWORD_FOR_STUDENT_SETTING);
            }
        });
        parentSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getPasswordCheckIntent(null, false);
                startActivityForResult(i, REQUEST_PASSWORD_FOR_PARENT_SETTING);
            }
        });

        if(mApplication.isDisabledParentControl()) {
            studentMode.setChecked(false);
        }
        else{
            studentMode.setChecked(true);
        }
        mStudentModeSwitch = studentMode;
        studentMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == mApplication.isDisabledParentControl()){
                    if(isChecked == true){
                        mApplication.enableParentControl(isChecked);
                        getModel().forceReload();
                    }
                    else {
                        Intent i = getPasswordCheckIntent(getString(R.string.pc_disable), false);
                        startActivityForResult(i, REQUEST_DISABLE_PARENT_CONTROL);
                    }
                }
            }
        });
    }

    private boolean isMyAppLauncherDefault()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        return currentHomePackage.contentEquals(getPackageName());
    }

    private void defaultLauncherCheck()
    {
        //final String serverurl = UsersContract.LOCAL_SERVER;
        final String serverurl = UsersContract.getServerUrl();
        //create a jason request to query device type
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                serverurl + UsersContract.DEVICE_SPECIFIC,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(tag, "device specific: " + response);
                        try {
                            JSONObject mainObject = new JSONObject(response);
                            String type = mainObject.getString("type");
                            if(type.contentEquals("google")){
                                DeviceSpecific.getInstance().setType(DeviceSpecific.GOOGLE_TYPE);
                            }else if(type.contentEquals("xiaomi")){
                                DeviceSpecific.getInstance().setType(DeviceSpecific.XIAOMI_TYPE);
                            }
                            else{
                                DeviceSpecific.getInstance().setType(DeviceSpecific.UNKNOWN_TYPE);
                            }
                            String url = mainObject.getString("url");
                            if(isMyAppLauncherDefault()) return;
                            if(type.contentEquals("google")) return;
                            if (url != null) {
                                Intent i = new Intent(ParentSettingLauncher.this, myblog.richard.vewe.libactivities.WebviewActivity.class);
                                i.putExtra(WebviewActivity.URL, serverurl + url);
                                startActivity(i);
                            }
                        }catch (Exception e)
                        {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(tag, "error to get device specific " + error);
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("manufacturer", Build.MANUFACTURER);
                params.put("model", Build.MODEL);

                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.PreferredListSettings"));
                List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                if(list.size() > 0) {
                    params.put("preferredlist", "exist");
                }
                return params;
            }
        };
        stringRequest.setShouldCache(false);
        mApplication.getQueue().add(stringRequest);

    }

    private void firstTimeInstallation()
    {
        mApplication.getQueue().add(new StringRequest(Request.Method.GET, UsersContract.getServerUrl() + UsersContract.FIRSTTIME_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Log.d(tag, "Get firsttime install: " + response);
                        //start activity to load firsttime.html
                        Intent i = new Intent(ParentSettingLauncher.this, myblog.richard.vewe.libactivities.WebviewActivity.class);
                        i.putExtra("url", UsersContract.getServerUrl() + UsersContract.FIRSTTIME_URL);
                        startActivity(i);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(tag, "failed to get first time install html");
            }
        }));
    }

/*    private void setupIconHandle()
    {
        getSearchBar().setUserOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onClickAddWidgetButton(null); //enter parent control
                if (workspaceIsOverviewMode()) {
                    switchParentControl();
                } else {
                    launchOverviewMode();
                }
            }
        });

        getSearchBar().setLeftTimeOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogFile.dump();
            }
        });
    }

    private void showIcon()
    {
        User u = mApplication.getmUser();
        if(u == null) return;
        int flag = (Integer)u.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        boolean showIcon = false;

        if(UsersContract.TableUsers.showParentControlIcon(flag))
        {
            showIcon = true;
        }
        getSearchBar().showIcon(showIcon);
    }*/

    private Intent getPasswordCheckIntent(String reason, boolean canPwd2Unlock)
    {
        Intent i = new Intent(this,
                myblog.richard.vewe.libactivities.PasswordCheckActivity.class);

        String[] passwords = mApplication.getPasswords();
        String password = null;

        if(passwords != null) {
            password = passwords[0];
            if(canPwd2Unlock) {
                i.putExtra(PasswordInput.PWD2, passwords[1]);
            }
        }

        i.putExtra(PasswordInput.PWD, password);
        if(password != null && password.contentEquals(UsersContract.TableUsers.DEFAULT_ROOT_PWD))
        {
            i.putExtra(PasswordInput.INITIAL, password);
        }

        if(reason != null)
        {
            i.putExtra(PasswordInput.INFO, reason);
        }
        return i;
    }

    @Override
    protected void onClickAllAppsButton(View v) {
        if(mApplication.isVisitAllAppsAllowed()) {
            super.onClickAllAppsButton(v);
        }
        else
        {
            Intent i = getPasswordCheckIntent(null, true);
            startActivityForResult(i, REQUEST_ALL_APPLIST);

        }
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
        CharSequence labelName = null;

        if(vtag instanceof AppInfo)
        {
            comp = ((AppInfo)vtag).componentName;
            isAppInfo = true;
            labelName = ((AppInfo)vtag).title;
        }
        else if(vtag instanceof ShortcutInfo)
        {
            comp = ((ShortcutInfo)vtag).getTargetComponent();
            labelName = ((ShortcutInfo)vtag).title;
        }
        else{
            Log.e(tag, "Input must be a Shortcut or AppInfo");
            return;
        }

        int type = mApplication.getType(comp.getPackageName());
        boolean isLocked = false;
        String reason = null;
        if(type == UsersContract.TableApplist.FORBIDDEN)
        {
            isLocked = true;
            reason = labelName.toString();
        }
        else if(type == UsersContract.TableApplist.FREE)
        {
            isLocked = false;
        }
        else
        {
            //query lock and reason
            LauncherApplication.LockAndReason lr = mApplication.getLockAndReason(comp.getPackageName());
            isLocked = lr.locked;
            reason = lr.reason;
        }

        if(mApplication.isDisabledParentControl())
        {
            isLocked = false;
        }

        if(isLocked)
        {
            mView = v;
            mPackage = comp.getPackageName();
            Intent i = getPasswordCheckIntent(reason, type == UsersContract.TableApplist.FORBIDDEN);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean superHandled = false;

        if(requestCode == REQUEST_PASSWORD_FOR_LAUNCH_APPINFO)
        {
            if (resultCode == RESULT_OK) {
                if(mView != null) {
                    if(LOGD){
                        Log.d(tag, "Call authorised to service");
                    }
                    mApplication.getMyService().setAuthorised(mPackage);
                    super.onClickAppInfo(mView);
                }
            }
        }
        else if(requestCode == REQUEST_PASSWORD_FOR_LAUNCH_SHORTCUT)
        {
            if (resultCode == RESULT_OK) {
                if(mView != null) {
                    if(LOGD){
                        Log.d(tag, "Call authorised to service");
                    }
                    mApplication.getMyService().setAuthorised(mPackage);
                    super.onClickAppShortcut(mView);
                }
            }
        }
        else if(requestCode == REQUEST_ALL_APPLIST)
        {
            if(resultCode == RESULT_OK){
                super.onClickAllAppsButton(null);
            }
        }
        else if(requestCode == REQUEST_ACTION){
            if(resultCode == RESULT_OK){
                mApplication.completeAction();
            }
        }
        else if(requestCode == REQUEST_DISABLE_PARENT_CONTROL){
            if(resultCode == RESULT_OK){
                mApplication.enableParentControl(false);
                getModel().forceReload();
            }
            else
            {
                mStudentModeSwitch.setChecked(true);    //failed to disable parent control
            }
        }
        else if(requestCode == REQUEST_PASSWORD_FOR_GROUPING){
            if(resultCode == RESULT_OK){
                Intent i = new Intent(this,
                        myblog.richard.vewe.libactivities.SelectAppsActivity.class);
                startActivityForResult(i, REQUEST_END_GROUPING);
            }
        }
        else if(requestCode == REQUEST_ACCESS_INFO)
        {/*
            ContentValues values = new ContentValues();
            values.put(UsersContract.TableApplist.Column.PKG, "com.android.settings");
            values.put(UsersContract.TableApplist.Column.TYPE, UsersContract.TableApplist.FORBIDDEN);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            getContentResolver().insert(UsersContract.TableApplist.DIGA_URI, values);*/
        }
        else if(requestCode == REQUEST_END_GROUPING){
                applistMayChanged = true;
        }
        else if(requestCode == REQUEST_PASSWORD_FOR_STUDENT_SETTING){
            if(resultCode == RESULT_OK){
                Intent i = new Intent(this,
                        myblog.richard.vewe.libactivities.BasicControlActivity.class);
                startActivity(i);
            }
        }
        else if(requestCode == REQUEST_PASSWORD_FOR_PARENT_SETTING){
            if(resultCode == RESULT_OK){
                Intent i = new Intent(this,
                        myblog.richard.vewe.libactivities.MoreSettingActivity.class);
                startActivity(i);
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

        if(firstCallResume)
        {
            firstCallResume = false;
            return;
        }

        if(LOGD) {
            Log.d(tag, "onResume");
        }
        if(applistMayChanged || PackageType.getApplistMayChanged()) {
            applistMayChanged = false;
            PackageType.setApplistMayChanged(false);
            //regain applist and compare to previous one
            if (mApplication.applistChanged()) {
                Log.d(tag, "restart needed");
                mApplication.restart();
                return;
            } else {
//                Log.d(tag, "restart is not needed");
                //check if User is changed, include lefttime, flag
//                User u = mApplication.latestUser();
//                onResumeUpdateUser(u);
            }
        }

        Action action = mApplication.getAction();
        if(action == null) {
            //check if we had any actions to do
            mApplication.getMyService().getAction();
        }
        else if(action.complete()){
            mApplication.completeAction();
        }
        else
        {
            Log.d(tag, "to do action " + action);
            action.doIt();

            Intent i = new Intent(this,
                    myblog.richard.vewe.libactivities.ActionActivity.class);

            i.putExtra(ActionActivity.ACTION, action);
            startActivityForResult(i, REQUEST_ACTION);
        }
    }

    /*private void onResumeUpdateUser(User u)
    {
        User prevU = mApplication.getmUser();
        if(u == null || prevU == null) return;
        //update flag
        prevU.getProperities().put(UsersContract.TableUsers.Column.FLAG,
                u.getProperities().get(UsersContract.TableUsers.Column.FLAG));
        //in fact, we should check if left time changed, left time/icon is disabled ...
        if(     (int)prevU.getProperities().get(UsersContract.TableUsers.Column.FLAG) !=
                        (int)u.getProperities().get(UsersContract.TableUsers.Column.FLAG)
                )
        {
            Log.d(tag, "update as leftime changed or visiable changed");
            mApplication.setmUser(u);
            showIcon();
        }
    }*/
}
