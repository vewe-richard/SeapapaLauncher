/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package  myblog.richard.vewe.launcher3;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import myblog.richard.vewe.libactivities.Action;
import myblog.richard.vewe.libcommon.test.LogFile;
import myblog.richard.vewe.libservice.EventRecords;
import myblog.richard.vewe.libservice.MessageType;
import myblog.richard.vewe.libservice.MyService;
import myblog.richard.vewe.libservice.RemoteMyService;
import myblog.richard.vewe.libusersprovider.App;
import myblog.richard.vewe.libusersprovider.PackageType;
import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

public class LauncherApplication extends Application {
    private static final String tag = "myapplication";
    private static final boolean LOGD = true;
    private User mUser;
    private ArrayList<App> mApplist = new ArrayList<App>();
    private PendingIntent mIntent;
    private Thread.UncaughtExceptionHandler defaultUEH;
    private RequestQueue mQueue;
    private Action mAction;

    public Action getAction() {
        if(mAction == null) return null;
        if(mAction.isValid() == false) return null;
        return mAction;
    }

    public void completeAction() {
        if(mAction == null) return;
        mService.completeAction(mAction.getId());
        mAction = null;
    }

    private RemoteMyService mService = new RemoteMyService(this){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MessageType.Response.GETACTION:
                    Log.d(tag, "get action: " + (String)msg.obj);
                    String action = (String)msg.obj;
                    if(action == null) return;
                    mAction = new Action(action);
                    /*
                    try{
                        JSONObject json = new JSONObject(action);
                        int id = Integer.parseInt(json.getString("id"));
                        completeAction(id);
                    }catch(Exception e){

                    }*/
                    break;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            super.onServiceConnected(name, service);
            su(UsersContract.TableUsers.DIGA);
        }
    };

    public void setIntent(PendingIntent intent)
    {
        mIntent = intent;
    }

    public void restart()
    {
        if(mIntent != null)
        {
            AlarmManager alarmManager;
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    15000, mIntent);
            System.exit(2);
        }
    }

    public LauncherApplication()
    {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        // setup handler for uncaught exception
        //TODO we should uncomment this
        //Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    // here I do logging of exception to a db
                    restart();
                    Log.d("application", "got exception");
                    // re-throw critical exception further to the os (important)
                    defaultUEH.uncaughtException(thread, ex);
                }
            };



    @Override
    public void onCreate() {
        PackageType.setContext(this);
        loadFromContentProvider();
        MyAppFilter.getInstance().setApplication(this);
        super.onCreate();
        LauncherAppState.setApplicationContext(this);
        LauncherAppState.getInstance();
        LogFile.setup(getFilesDir().getAbsolutePath());
        LogFile.write("begin message");

        mQueue = Volley.newRequestQueue(this);

//        android.os.Process.killProcess(28463);
        MyService.startService(this);
        mService.bind();
    }

    public RequestQueue getQueue() {
        return mQueue;
    }

    public RemoteMyService getMyService()
    {
        return mService;
    }

    @Override
    public void onTerminate() {
        mService.unbind();
        MyService.stopService(this);
        super.onTerminate();
        LauncherAppState.getInstance().onTerminate();
    }

    private void loadFromContentProvider()
    {
        //get current user
        ContentResolver resolver = getContentResolver();

        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        try {
            Cursor cursor = resolver.query(UsersContract.TableUsers.CONTENT_URI,
                    UsersContract.TableUsers.BASIC_COLUMNS,
                    selection,
                    null,
                    null);
            if(cursor == null || cursor.getCount() != 1)
            {
                Log.e(tag, "failed to query current user");
                return;
            }
            cursor.moveToFirst();
            mUser = new User(cursor);
            cursor.close();
            if(LOGD) {
                Log.d(tag, "current User " + mUser);
            }

            mApplist.clear();
            cursor = resolver.query(
                    UsersContract.TableApplist.DIGA_URI,
                    UsersContract.TableApplist.BASIC_COLUMNS,
                    null,
                    null,
                    null);
            if(cursor == null)
            {
                Log.e(tag, "could not query all from table applist diga");
            }
            else if(cursor.getCount() < 1)
            {
                if(LOGD) {
                    Log.d(tag, "no entries in table applist diga");
                }
            }
            else {
                while(cursor.moveToNext()){
                    App app = new App(cursor);
                    if(LOGD) {
                        Log.d(tag, "app: " + app);
                    }
                    mApplist.add(app);
                }
                cursor.close();
            }

        }catch(Exception e)
        {
            Log.e(tag, "exception to query current user or applist");
        }
    }

    public boolean shouldShowApp(String pkgName)
    {
        if(mUser == null) return true;
        int flag = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.FLAG);

        if(UsersContract.TableUsers.parentControlDisabled(flag)){
            return true;
        }

        //TODO: we may check if we should disable display of forbidden apps

        //OK, check the applist
        for(App app : mApplist){
            if(app.getPkgName().contentEquals(pkgName)){
                if(app.getPkgType() == UsersContract.TableApplist.FORBIDDEN){
                    return false;
                }
            }
        }
        return true;
    }

    public String getPrefix(String pkgName)
    {
        String prefix = "";
        if(mApplist != null) {
            for (App app : mApplist) {
                if (app.getPkgName().contentEquals(pkgName)) {
                    return app.getPrefix();
                }
            }
        }
        if(PackageType.getType(pkgName) == UsersContract.TableApplist.FORBIDDEN) {
            prefix = UsersContract.TableApplist.FORBIDDEN_PREFIX;
        }
        return prefix;
    }

    public boolean isDisabledParentControl()
    {
        if(mUser == null) return true;
        int flag = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        return UsersContract.TableUsers.parentControlDisabled(flag);
    }

    public int installDate(){
        if(mUser == null) return 0; //it is not dertimined
        Integer date = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.INSTALL_DATE);
        if(date == null){
            return -1;
        }
        return date;

    }

    public void setInstallDate()
    {
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        int installdate = EventRecords.getCurDate();
        update.put(UsersContract.TableUsers.Column.INSTALL_DATE, installdate);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update install date");
        }
        if(mUser != null){
            mUser.getProperities().put(UsersContract.TableUsers.Column.INSTALL_DATE, installdate);
        }
    }

    public void enableParentControl(boolean enable)
    {
        if(mUser == null) return;

        int flag = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        if(enable) {
            flag = UsersContract.TableUsers.enableParentControl(flag);
        }
        else
        {
            flag = UsersContract.TableUsers.disableParentControl(flag);
        }
        //update flag to content provider
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        update.put(UsersContract.TableUsers.Column.FLAG, flag);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the current user flag");
        }
        else
        {
            mUser.getProperities().put(UsersContract.TableUsers.Column.FLAG, flag);
            mService.updateUserSetting();
        }
    }


    public boolean isVisitAllAppsAllowed()
    {
        if(mUser == null) return true;
        int flag = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        return UsersContract.TableUsers.showApplist(flag);
    }

    public int getType(String pkgName)
    {
        return PackageType.getType(pkgName, mApplist);
    }

    //dynamic query content provider lock and its reason
    class LockAndReason{
        boolean locked;
        String reason;

        public LockAndReason(boolean locked, String reason) {
            this.locked = locked;
            this.reason = reason;
        }
    }
    public LockAndReason getLockAndReason(String pkgName)
    {
        LockAndReason lr = new LockAndReason(false, null);

        ContentResolver resolver = getContentResolver();
        String selection = UsersContract.TableApplist.Column.PKG + "=" + pkgName;
        Cursor cursor = resolver.query(
                UsersContract.TableApplist.DIGA_URI,
                new String[]{UsersContract.TableApplist.Column.LOCK, UsersContract.TableApplist.Column.REASON},
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
        return lr;
    }

    public String[] getPasswords()
    {
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
        Cursor cursor = getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
                new String[]{UsersContract.TableUsers.Column.PASSWORD, UsersContract.TableUsers.Column.PWD2},
                selection,
                null,
                null);
        if(cursor == null || cursor.getCount() != 1)
        {
            Log.e(tag, "failed to query root password");
            return null;
        }
        String[] as = new String[2];

        cursor.moveToFirst();
        int index = cursor.getColumnIndex(UsersContract.TableUsers.Column.PASSWORD);
        as[0] = cursor.getString(index);
        index = cursor.getColumnIndex(UsersContract.TableUsers.Column.PWD2);
        as[1] = cursor.getString(index);
        cursor.close();
        return as;
    }

    public String getPassword()
    {
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
        Cursor cursor = getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
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

    public static String packageName()
    {
        return "myblog.richard.vewe.launcher3";
    }

    public User getmUser()
    {
        return mUser;
    }

    public void setmUser(User u)
    {
        mUser = u;
    }

    public User latestUser()
    {
        User u = null;

        ContentResolver resolver = getContentResolver();

        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        try {
            Cursor cursor = resolver.query(UsersContract.TableUsers.CONTENT_URI,
                    UsersContract.TableUsers.BASIC_COLUMNS,
                    selection,
                    null,
                    null);
            if (cursor == null || cursor.getCount() != 1) {
                Log.e(tag, "failed to query current user");
                return null;
            }
            cursor.moveToFirst();
            u = new User(cursor);
            cursor.close();
            if (LOGD) {
                Log.d(tag, "current User " + u);
            }
        } catch(Exception e)
        {

        }
        finally {
            return u;
        }
    }

    public boolean applistChanged()
    {
        Cursor cursor = getContentResolver().query(
                UsersContract.TableApplist.DIGA_URI,
                UsersContract.TableApplist.BASIC_COLUMNS,
                null,
                null,
                null);
        if(cursor == null || cursor.getCount() < 1)
        {
            if(mApplist.size() == 0) return false;
            else return true;
        }
        else {
            if(mApplist.size() != cursor.getCount()) return true;

            while(cursor.moveToNext()){
                App app = new App(cursor);
                boolean match = false;
                for(App cmp : mApplist)
                {
                    if(app.getPkgName().contentEquals(cmp.getPkgName()))    //same package
                    {
                        match = true;
                        if(app.getPkgType() != cmp.getPkgType()){
                            return true;
                        }
                        break;
                    }
                }
                if(match == false)
                {
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

}