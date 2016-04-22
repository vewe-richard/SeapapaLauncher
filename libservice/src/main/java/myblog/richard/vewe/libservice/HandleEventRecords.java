package myblog.richard.vewe.libservice;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.util.Date;

import myblog.richard.vewe.libcommon.test.LogFile;
import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-25.
 */
public class HandleEventRecords {
    private static final String tag = "handleERs";
    private static final boolean LOGD = true;
    public static final int DEFAULT_WARNING_AHEAD = 60;

    MyService mService;
    private EventRecordsIO mERIO;
    private MainHandler mMainHandler;
//    private Ringtone mRing;
    private EventRecords mEventRecords;
//    private boolean mRingPlayed;

    HandleEventRecords(MyService svc)
    {
        mService = svc;
        mMainHandler = svc.getmMainHandler();
        //create an instance of
        mERIO = new EventRecordsIO(mMainHandler.getmUserDir());
        mERIO.clear();
        //get ring
//        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//        mRing = RingtoneManager.getRingtone(mService, notification);

        mEventRecords = mERIO.read(EventRecords.getCurDate());
        if(mEventRecords == null)
        {
            LogFile.write("new event records");

            mEventRecords = new EventRecords(mERIO);
            mEventRecords.setmBaseTime(mMainHandler.getBaseTime());
            mEventRecords.setPrevLeftTime(readLeftTime(EventRecords.getCurDate()));
            LogFile.write("create event records done " + mEventRecords);
            mEventRecords.addEvent(EventRecord.CREATE_EVENT, new Date().getTime(),
                    UsersContract.TableApplist.UNKNOWN, EventRecord.CREATE_EVENT);

        }
        else
        {
            //do not apply previous left time, as it's installed in file
            //any transient field should be applied
            mEventRecords.setErio(mERIO);
            LogFile.write("old event records: " + mEventRecords);
            mEventRecords.addEvent(EventRecord.START_EVENT, new Date().getTime(),
                    UsersContract.TableApplist.UNKNOWN, EventRecord.START_EVENT);
        }
        storeLeftTime(mEventRecords.getmDate(), mEventRecords.getLeftTime());
        mMainHandler.updateLeftTime(mEventRecords.getLeftTime());
    }

    public void stop()
    {
//        stopWarning();
        mEventRecords.quit();
    }

    private void updateUserSetting(User u, User prev)
    {
        if(u == null || prev == null) return;

        //check if basetime is changed
        Integer baseprev = (Integer)prev.getProperities().get(UsersContract.TableUsers.Column.BASETIME);
        Integer basenow = (Integer)u.getProperities().get(UsersContract.TableUsers.Column.BASETIME);
        if(baseprev == null || basenow == null) return;
        if(baseprev == basenow) return;
        if(LOGD){
            Log.d(tag, "basetime is changed to " + basenow);
            mEventRecords.setmBaseTime(basenow);
            mMainHandler.updateLeftTime(mEventRecords.getLeftTime());
        }
    }

    public void handleMessage(Message msg) {
        Message resp = null;
        switch (msg.what) {
            case MessageType.Request.UPDATE_USER_SETTING:
                updateUserSetting((User)msg.obj, mMainHandler.getmUser());
                break;
            case MessageType.Request.ADDTIME:
                mEventRecords.addBonus(msg.arg1*60);
                //we should update content provider
                mMainHandler.updateLeftTime(mEventRecords.getLeftTime());
                break;
            case MessageType.Request.LEFTTIME_R:
                resp = Message.obtain(null, MessageType.Response.LEFTTIME);
                try {
                    resp.arg1 = mEventRecords.getLeftTime();
                    msg.replyTo.send(resp);
                }catch(Exception e)
                {
                    Log.d(tag, "MyError", e);
                }
                break;
            case MessageType.Request.RECORDS_R:
                resp = Message.obtain(null, MessageType.Response.RECORDS);
                Bundle b = new Bundle();
                if(msg.arg1 == mEventRecords.getmDate()) //current date
                {
                    Log.d(tag, "get Current event records " + mEventRecords);
                    b.putSerializable(MessageType.Response.SERL1, mEventRecords);
                }
                else
                {
                    EventRecords ers = (EventRecords)mERIO.read(msg.arg1);
                    b.putSerializable(MessageType.Response.SERL1, ers);
                }
                resp.setData(b);
                try {
                    msg.replyTo.send(resp);
                }catch(Exception e)
                {
                    Log.d(tag, "MyError", e);
                }

                break;
        }
    }

    public void reportEvent(String pkg, int type, String label)
    {
        int leftTime;   // = mEventRecords.getLeftTime();

        if(LOGD){
            Log.d(tag, "new event " + pkg);
        }
        if(mEventRecords.addEvent(pkg, new Date().getTime(), type, label))
        {
            leftTime = mEventRecords.getLeftTime();
            storeLeftTime(mEventRecords.getmDate(), leftTime);
            mMainHandler.updateLeftTime(leftTime);
        }

        /*
        if(type == UsersContract.TableApplist.GAME) {
            if (leftTime < DEFAULT_WARNING_AHEAD) {
                warning();
            }
            else
            {
                stopWarning();
            }
        }
        else
        {
            stopWarning();
        }*/
    }

    //read prev left time from preference, if it out of range, so give it out
    //if it's day latest, added to a totoal
    private int readLeftTime(int curDate)
    {
        int date = mMainHandler.getPrevLeftDate();
        int val = mMainHandler.getPrevLeftTime();
        /*
        if(LOGD) {
            Log.d(tag, "stored date " + date + " " + val);
        }*/
     //   D/logfile ( 2034): 6/9:8:59 readLeftTime date 735814 val 230 curDate 735815

        LogFile.write("readLeftTime date " + date + " val " + val + " curDate " + curDate);
        if(date == -1 || val == -1) return 0;   //no preference

        if(date >= curDate) return 0; //it's the same day or impossible day

        //the new design is applied on 2015/11/28, so impossible be less than it
        //disable to test stored left time
        if(date < EventRecords.DATA_PREVIOUS_2015_11_28) return 0;

        int total = val;    //keep the prevLeftTime
        total += (curDate - date - 1)*mEventRecords.getmBaseTime()*60;

        //max 3days can be left
        //disable to test stored left time
        int max, min;
        if(mEventRecords.getmBaseTime() == 0)
        {
            max = MAX_STORE_MINUTES_ON_BASTTIME_ZERO * 60;
            min = -(MAX_STORE_MINUTES_ON_BASTTIME_ZERO * 60);

        }
        else {
            max = MAX_STORE_TIME * mEventRecords.getmBaseTime() * 60;
            min = -(MAX_STORE_TIME * mEventRecords.getmBaseTime() * 60);
        }
        
        if(total > max)
            total = max;
        else if(total < min)
            total = min;
        return total;
    }

    public static final int MAX_STORE_TIME = 5;
    public static final int MAX_STORE_MINUTES_ON_BASTTIME_ZERO = 100;

    public void storeLeftTime(int date, int leftTime) {
        LogFile.writeflush("store prev lefttime: date " + date + " lefttime " + leftTime);
        mMainHandler.updatePrevLeftTime(date, leftTime);
    }
}
