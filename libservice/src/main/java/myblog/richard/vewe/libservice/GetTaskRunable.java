package myblog.richard.vewe.libservice;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import myblog.richard.vewe.libcommon.test.LogFile;
import myblog.richard.vewe.libusersprovider.PackageType;
import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-25.
 */
public class GetTaskRunable implements Runnable {
    private static final String tag = "gettask";
    private static final boolean LOGD = true;
    public static final int DEFAULT_SCAN_INTERVAL = 1000;
    private static final int DEFAULT_EVENTS_RECORD_INTERVAL = 5000;
    private int mScanCount;

    private boolean mIsRunning;
    private MyService mService;
    private MainHandler mMainHandler;
    private ActivityManager mActivityManager;
    private PackageManager mManager;

    private WatchDog mWatchDog;
    private HandleEventRecords mHandleERs;
    private boolean mIsParantControlDisabled;
    private AutoMuteMode mAutoMute;

    GetTaskRunable(MyService service) {
        mService = service;
        mMainHandler = mService.getmMainHandler();
        mActivityManager = (ActivityManager) mService.getSystemService(Context.ACTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT > 20) {
            mUsageStatsManager = (UsageStatsManager) mService.getSystemService(Context.USAGE_STATS_SERVICE);
        }
        mManager = mService.getPackageManager();
        mWatchDog = new WatchDog(service, mActivityManager, mManager);
        mHandleERs = new HandleEventRecords(mService);
        mIsParantControlDisabled = parentControlDisabled(mMainHandler.getmUser());
        mAutoMute = new AutoMuteMode(mService);
        mAutoMute.enable(autoMute(mMainHandler.getmUser()));
    }

    private boolean autoMute(User u) {
        if (u == null) return false;
        Integer flag = (Integer) u.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        if (flag == null) return false;
        return UsersContract.TableUsers.autoMuteEnable(flag);
    }

    private boolean parentControlDisabled(User u) {
        if (u == null) return true;
        Integer flag = (Integer) u.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        if (flag == null) return true;
        return UsersContract.TableUsers.parentControlDisabled(flag);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MessageType.Request.ADDTIME:
            case MessageType.Request.LEFTTIME_R:
            case MessageType.Request.RECORDS_R:
                mHandleERs.handleMessage(msg);
                break;
            case MessageType.Request.UPDATE_USER_SETTING:
                //retrieve user flag, and check if parent control is disabled
                mIsParantControlDisabled = parentControlDisabled((User) msg.obj);
                mAutoMute.enable(autoMute((User) msg.obj));
                mHandleERs.handleMessage(msg);
                break;

            case MessageType.Request.CLEAR_AUTHORISED:
            case MessageType.Request.SET_AUTHORISED:
                mWatchDog.handleMessage(msg);
                break;
            default:
                Log.w(tag, "Unknow msg to get task runable");
                break;
        }
    }

    /**
     * Is the screen of the device on.
     *
     * @param context the context
     * @return true when (at least one) screen is on
     */
    public boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }

    public boolean isKeyLocked() {
        KeyguardManager myKM = (KeyguardManager) mService.getSystemService(Context.KEYGUARD_SERVICE);
        return (myKM.isKeyguardSecure() || myKM.isKeyguardLocked());
    }

    private UsageStatsManager mUsageStatsManager;
    private String mPreviousPkg;

    @Override
    public void run() {
        if(mIsRunning == false) return;
        if(mIsParantControlDisabled){
            mService.getmServiceHandler().postDelayed(this, DEFAULT_SCAN_INTERVAL);
            return;
        }
        mAutoMute.run();

        //do not continue if the screen is off or key guarden
        if(!isScreenOn(mService) || isKeyLocked()){
            Log.d(tag, "screen is off or key is locked - keylock " + isKeyLocked());
            mHandleERs.reportEvent(EventRecord.SCREEN_OFF_EVENT, UsersContract.TableApplist.UNKNOWN, EventRecord.SCREEN_OFF_EVENT);
            mService.getmServiceHandler().postDelayed(this, DEFAULT_SCAN_INTERVAL);
            return;
        }

        String activityName = null;
        if(Build.VERSION.SDK_INT > 20){
//                activityName =mActivityManager.getRunningAppProcesses().get(0).processName;
            long time = System.currentTimeMillis();
            // We get usage stats for the last 10 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*10, time);
            // Sort the stats by the last time used
            if(stats != null) {
                SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if(mySortedMap != null && !mySortedMap.isEmpty()) {
                    activityName =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    mPreviousPkg = activityName;
                }
            }
            if(activityName == null)
            {
                activityName = mPreviousPkg;
            }
            if(activityName == null)
            {
                Log.d(tag, "activityName is null");
                mService.getmServiceHandler().postDelayed(this, DEFAULT_SCAN_INTERVAL);
                return;
            }
        }
        else{
            activityName =   mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
//            Log.d(tag, "real " + mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName());
        }

        if(LOGD) {
            Log.d(tag, "activity " + activityName);
        }

        //set service to foreground when not student launcher
        mMainHandler.runningPackage(activityName);

        //WatchDog
        int type = PackageType.getType(activityName, mMainHandler.getmApplist());
        if(UsersContract.FIRST_TIME_INSTALLATION == false) {
            mWatchDog.watchTask(activityName, type);
        }

        mScanCount ++;
        if(mScanCount >= (DEFAULT_EVENTS_RECORD_INTERVAL/DEFAULT_SCAN_INTERVAL))
        {
            String labelName;
            try {
                labelName = mManager.getApplicationLabel(mManager.getApplicationInfo(activityName, 0)).toString();
            }catch(Exception e)
            {
                labelName = activityName;
            }
            LogFile.write("report event " + labelName);
            mHandleERs.reportEvent(activityName, type, labelName);
            mScanCount = 0;
        }

        mService.getmServiceHandler().postDelayed(this, DEFAULT_SCAN_INTERVAL);

    }

    public void start()
    {
        if(mIsRunning) return;
        mIsRunning = true;
        mScanCount = 0;

        mService.getmServiceHandler().postDelayed(this, DEFAULT_SCAN_INTERVAL);
    }

    public void stop()
    {
        if(!mIsRunning) return;
        mIsRunning = false;
    }

}
