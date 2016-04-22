package myblog.richard.vewe.libservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import myblog.richard.vewe.libusersprovider.App;
import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-24.
 */
public class MainHandler {
    private static final String tag = "main";
    private static final boolean LOGD = true;

    private String mUserDir;
    private String mCacheDir;
    private User mUser;
    private ArrayList<App> mApplist = new ArrayList<App>();

    private MyService mService;
    private boolean mLogin;
    private GetTaskRunable mRunable;
    private GetServer mServer;

    public User getmUser() {
        return mUser;
    }

    public ArrayList<App> getmApplist() {
        return mApplist;
    }

    MainHandler(MyService svc)
    {
        mService = svc;
    }

    public String getmUserDir() {
        return mUserDir;
    }

    public String getmCacheDir() {
        return mCacheDir;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MessageType.Request.SU:
                msg.obj = msg.getData().getString(MessageType.Request.SERL1);
                if(LOGD)
                {
                    Log.d(tag, "su " + (String)msg.obj);
                }
                su(msg);
                break;
            case MessageType.Request.LOGOUT:
                if(LOGD)
                {
                    Log.d(tag, "logout");
                }
                logout();
                break;
            case MessageType.Request.UPDATE_USER_SETTING:
                User u;
                u = getCurrentUser();
                //Let EventRecords to handle it
                msg.obj = u;
                if(mRunable != null) {
                    mRunable.handleMessage(msg);
                }

                mUser = u;
                break;
            case MessageType.Request.UPDATE_APPLIST:
                getApplist();
                break;
            case MessageType.Request.COMPLETE_ACTION:
            case MessageType.Request.GETACTION_R:
            case MessageType.Request.GETABOUTPROJECT_R:
                mServer.handleMessage(msg);
                break;
            default:
                mRunable.handleMessage(msg);
                break;
        }
    }

    //when service is destroyed
    public void destroy()
    {
        logout();
    }

    public void logout()
    {
        if(!mLogin){
            return;
        }
        mLogin = false;
        mRunable.stop();
    }

    private void su(Message msg)
    {
        String user = (String)msg.obj;
        if(user == null){
            return;
        }

        if(mLogin)
        {
//            logout();
            return;     //currently only consider one user
        }
        mLogin = true;
        //get User base dir
        mUserDir = mService.getFilesDir().getAbsolutePath() + "/" + user;
        mCacheDir = mService.getCacheDir().getAbsolutePath() + "/" + user;
        if(LOGD){
            Log.d(tag, "user base:" + mUserDir);
            Log.d(tag, "cache base:" + mCacheDir);
        }

        //create directory if it is not exist
        File dir = new File(mUserDir);
        if(!dir.exists()) dir.mkdir();

        dir = new File(mCacheDir);
        if(!dir.exists()) dir.mkdir();

        //get User and Applist setting
        mUser = getCurrentUser();
        getApplist();

        //create get task runable
        mRunable = new GetTaskRunable(mService);
        mRunable.start();

        mServer = new GetServer(mService);
    }

    public void updateLeftTime(int lefttime)
    {
        ContentResolver resolver = mService.getContentResolver();
        ContentValues update = new ContentValues();
        update.put(UsersContract.TableUsers.Column.LEFTTIME, lefttime);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.DIGA;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update lefttime");
        }
        else
        {
            Log.d(tag, "update lefttime");
        }
    }

    public void updatePrevLeftTime(int date, int leftTime)
    {
        ContentResolver resolver = mService.getContentResolver();
        ContentValues update = new ContentValues();
        update.put(UsersContract.TableUsers.Column.PREV_LEFT_TIME, leftTime);
        update.put(UsersContract.TableUsers.Column.PREV_LEFT_DATE, date);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.DIGA;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update prev lefttime");
        }
        else
        {
            Log.d(tag, "update prev lefttime");
        }
    }

    private void getApplist()
    {
        mApplist.clear();
        Cursor cursor = mService.getContentResolver().query(
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
    }

    public int getBaseTime()
    {
        if(mUser == null){
            return UsersContract.TableUsers.DEFAULT_BASETIME;
        }
        Integer bs = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.BASETIME);
        if(bs == null) {
            return UsersContract.TableUsers.DEFAULT_BASETIME;
        }
        return bs;
    }

    public int getPrevLeftDate()
    {
        if(mUser == null){
            return -1;
        }
        Integer val = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.PREV_LEFT_DATE);
        if(val == null){
            return -1;
        }
        return val;
    }

    private boolean mIsForeground;

    public void runningPackage(String pkg)
    {
        if(pkg.contentEquals(mService.getPackageName()))
        {
            //stop foreground
            if(mIsForeground) {
                mService.stopForeground(true);
            }
            mIsForeground = false;

        } else {
            //start foreground
            if(!mIsForeground)
            {
                Notification notification=new Notification(R.drawable.ic_account,
                        "Desktop", System.currentTimeMillis());
                Intent i = new Intent();
                i.setComponent(new ComponentName(mService.getPackageName(),
                        "myblog.richard.vewe.launcher3.ParentSettingLauncher"));

                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent pi=PendingIntent.getActivity(mService, 0,
                        i, 0);
                notification.setLatestEventInfo(mService, null,
                        mService.getResources().getString(R.string.notification),
                        pi);

                mService.startForeground(1337, notification);

            }
            mIsForeground = true;
        }
    }

    public int getPrevLeftTime()
    {
        if(mUser == null) return -1;
        Integer val = (Integer)mUser.getProperities().get(UsersContract.TableUsers.Column.PREV_LEFT_TIME);
        if(val == null)
        {
            return -1;
        }
        return val;
    }

    private User getCurrentUser()
    {
        User u = null;

        //get current user
        ContentResolver resolver = mService.getContentResolver();

        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        try {
            Cursor cursor = resolver.query(UsersContract.TableUsers.CONTENT_URI,
                    new String[]{UsersContract.TableUsers.Column.FLAG,
                            UsersContract.TableUsers.Column.APPLIST,
                            UsersContract.TableUsers.Column.BASETIME,
                            UsersContract.TableUsers.Column.PREV_LEFT_DATE,
                            UsersContract.TableUsers.Column.PREV_LEFT_TIME,
                            UsersContract.TableUsers.Column.HOMERULE,
                            UsersContract.TableUsers.Column.GAMERULE,
                            UsersContract.TableUsers.Column.EMAIL,
                            UsersContract.TableUsers.Column.INSTALL_DATE},
                    selection,
                    null,
                    null);
            if (cursor == null || cursor.getCount() != 1) {
                Log.e(tag, "failed to query current user");
                return null;
            }
            cursor.moveToFirst();
            u = new User(cursor);
            if (LOGD) {
                Log.d(tag, "current User " + mUser);
            }
            cursor.close();
        }catch(Exception e)
        {

        }
        return u;
    }

    public String[] getPasswords()
    {
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
        Cursor cursor = mService.getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
                new String[] {UsersContract.TableUsers.Column.PASSWORD, UsersContract.TableUsers.Column.PWD2},
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
}
