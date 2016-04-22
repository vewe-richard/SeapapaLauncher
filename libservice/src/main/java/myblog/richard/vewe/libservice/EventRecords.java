package myblog.richard.vewe.libservice;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-10-17.
 */
public class EventRecords implements Serializable {
    private static final String tag = "eventrecords";
    private static final long serialVersionUID = 13L;
    private static final boolean LOGD = true;
    public static final String LAUNCHER_LABEL = "Launcher3";
    public static final int MAX_GAP_AS_UNKOWN_STATUS = (30 * 1000);
    private static final boolean DETAIL = false;
    private static final String THIS_PACKAGE = "myblog.richard.vewe.laun";

    private transient EventRecordsIO mErio;
    private transient EventRecord mLatestRecord;

    int mDate;
    int mTotalTime;
    int mPrevLeftTime;
    int mTotalPlayTime;
    int mBonusTime;
    int mBaseTime;
    ArrayList<EventRecord> mRecords;

    public EventRecords()
    {
        mRecords = new ArrayList<EventRecord>();
    }

    //this clone only for data access from other process
    public EventRecords clone(boolean removeUnknown)
    {
        EventRecords ers = new EventRecords();
        ers.mDate = mDate;
        ers.mTotalTime = mTotalTime;
        ers.mPrevLeftTime = mPrevLeftTime;
        ers.mTotalPlayTime = mTotalPlayTime;
        ers.mBonusTime = mBonusTime;
        //ers.mRecords = new ArrayList<EventRecord>();
        ers.mBaseTime = mBaseTime;
        for(EventRecord er : mRecords)
        {
            if(removeUnknown && er.getType() == UsersContract.TableApplist.UNKNOWN) continue;
            if(LOGD) {
                Log.d(tag, er.getName() + " " + removeUnknown);
            }
            if(removeUnknown && er.getName().contains(THIS_PACKAGE)) continue;
            if(removeUnknown && er.getDuration() == 0) continue;
            if(ers.mRecords.size() == 0){
                ers.mRecords.add(new EventRecord(er));
                continue;
            }

            EventRecord latest = ers.mRecords.get(ers.mRecords.size() - 1);
            if(latest.getName().contentEquals(er.getName()))
            {
                //merge them if only 60 seconds gap
                long lasttime = latest.getTime() + latest.getDuration();
                long gap = er.getTime() - lasttime;
                if(gap <= (60*1000))
                {
                    latest.setDuration(latest.getDuration() + er.getDuration());
                }
                else
                {
                    ers.mRecords.add(new EventRecord(er));
                }
            }
            else {
                ers.mRecords.add(new EventRecord(er));
            }
        }
        return ers;
    }

    public ArrayList<EventRecord> getmRecords() {
        return mRecords;
    }

    EventRecords(EventRecordsIO erio)
    {
        mErio = erio;
        mDate = getCurDate();
        mRecords = new ArrayList<EventRecord>();
        if(LOGD) {
            Log.d(tag, "call constructor");
        }
    }

    //addEvent to event records, return true if left time is changed,
    //isTimeLimited: is this activity time limited
    public boolean addEvent(String name, long time, int type, String label)
    {
        boolean leftTimeChanged = false;
        boolean addit = true;

        if(!DETAIL)
        {
            if(type == UsersContract.TableApplist.UNKNOWN || name.contains(THIS_PACKAGE))
            {
                addit = false;
            }
        }
        //Log.d(tag, name + ":" + new Date(time).toString());
        //check if a new day, TODO we should seperate this part of section
        int curDate = getDate(time);
        if(curDate != mDate)
        {
            mErio.write(this);
            mPrevLeftTime = getLeftTime();  //last left time is used as previous left time
            mDate = curDate;
            mTotalTime = 0;
            mTotalPlayTime = 0;
            mRecords = new ArrayList<EventRecord>();
            mBonusTime = 0;
            leftTimeChanged = true;
        }

        if(mRecords.isEmpty())
        {
            EventRecord er = new EventRecord(name, time, 0, label, type);
            mRecords.add(er);
            mLatestRecord = er;
            mErio.write(this);
        }
        else
        {
            //get latest event
            EventRecord latest;
            if(mLatestRecord == null) {
                latest = mRecords.get(mRecords.size() - 1);
            }else
            {
                latest = mLatestRecord;
            }
            //check if accept gap
            long lasttime = latest.getTime() + latest.getDuration();
            long gap = time - lasttime;
            mTotalTime += (int)(gap/1000); //include power off time
            if((gap > 0 && gap < MAX_GAP_AS_UNKOWN_STATUS)
                    || latest.getName().contentEquals(EventRecord.QUIT_EVENT))
            {
                latest.setDuration((int)(time - latest.getTime()));

                //check latest app is limited app, if so, add it to play time
                if(latest.getType() == UsersContract.TableApplist.GAME)
                {
                    mTotalPlayTime += (int)(gap/1000);
                    leftTimeChanged = true;
                    mErio.write(this);
                }

                boolean samePackage = false;
                if(latest.getName().contains(name) || name.contains(latest.getName()))
                {
                    samePackage = true;
                }

                if(!samePackage)
                {
                    mLatestRecord = new EventRecord(name, time, 0, label, type);
                    if(addit) {
                        mRecords.add(mLatestRecord);
                    }
                    /* we don't need write it as it is written if a game app
                    //the quit is called
                    if(!leftTimeChanged)    //we do not write it again as it's written
                        mErio.write(this);
                        */
                }
            }
            else
            {
//                Log.d(tag, "unknow status gap" + gap);
                if(addit) {
                    mRecords.add(new EventRecord(EventRecord.UNKNOWN_STATUS_EVENT, latest.getTime(), (int) gap,
                            "Unknown", UsersContract.TableApplist.UNKNOWN));
                }
                mLatestRecord = new EventRecord(name, time, 0, label, type);
                mRecords.add(mLatestRecord);
                mErio.write(this);
            }
        }
        return leftTimeChanged;
    }

    //TODO somewhere to call this
    public void quit()
    {
        if(LOGD) {
            Log.d(tag, "good, eventrecords are called");
        }
        addEvent(EventRecord.QUIT_EVENT, new Date().getTime(), UsersContract.TableApplist.UNKNOWN, EventRecord.QUIT_EVENT);
        mErio.write(this);
    }

    //
    public int getLeftTime()
    {
        return (mBaseTime*60 + mPrevLeftTime + mBonusTime - mTotalPlayTime);
    }

    public void addBonus(int time)
    {
        mBonusTime += time;
        mErio.write(this);
    }

    public int getmPrevLeftTime() {
        return mPrevLeftTime;
    }

    public int getmTotalPlayTime() {
        return mTotalPlayTime;
    }

    public int getmBonusTime() {
        return mBonusTime;
    }

    public int getmBaseTime() {
        return mBaseTime;
    }

    public void setmBaseTime(int mBaseTime) {
        this.mBaseTime = mBaseTime;
        mErio.write(this);
    }

    @Override
    public String toString() {
        return "EventRecords{" +
                "mDate=" + mDate +
                ", mPrevLeftTime=" + (mPrevLeftTime/60) + ":" + (mPrevLeftTime%60) +
                ", mErio=" + mErio +
                '}';
    }

    //Get & Set
    public int getmDate() {
        return mDate;
    }

    public int size()
    {
        return mRecords.size();
    }

    public void setPrevLeftTime(int mPrevLeftTime) {
        this.mPrevLeftTime = mPrevLeftTime;
    }

    public void setErio(EventRecordsIO mErio) {
        this.mErio = mErio;
    }

    //Static Functions
    public static int getCurDate()
    {
        Calendar cd = Calendar.getInstance();
        return _getDate(cd);
    }

    //which should not be possible
    public static final int DATA_PREVIOUS_2015_11_28 = (2015*365 + 332);

    public static int getDate(long time)
    {
        Calendar cd = Calendar.getInstance();
        cd.setTimeInMillis(time);
        return _getDate(cd);
    }

    public static int _getDate(Calendar cd)
    {
        int year = cd.get(Calendar.YEAR);
        int day = cd.get(Calendar.DAY_OF_YEAR);
        return year * 365 + day;
        /*  used to test stored left time
        int hour = cd.get(Calendar.HOUR_OF_DAY);
        int minute = cd.get(Calendar.MINUTE);
        return (hour * 60 + minute)/10;
         */
    }
}
