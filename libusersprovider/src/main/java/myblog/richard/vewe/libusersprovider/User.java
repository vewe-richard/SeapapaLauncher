package myblog.richard.vewe.libusersprovider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 15-11-9.
 */
public class User {
    private static final String tag = "User";
    private static final boolean LOGD = false;
    protected Map<String, Object> mElements = new HashMap<String, Object>();
    private Context mContext;
    private String mName;
    private SharedPreferences mSP;
    private long mWifiLostInitTime;
    private long mWifiLostSeconds;

    public long getmWifiLostSeconds() {
        return mWifiLostSeconds;
    }

    protected SharedPreferences getPreference()
    {
        return mSP;
    }

    private User()
    {
        //left time should be set to -1, it means not indicate
        mElements.put(UsersContract.TableUsers.Column.LEFTTIME, UsersContract.TableUsers.LEFTTIME_DEFAULT);
    }

    protected User(String name, Context context){
        this();
        mName = name;
        mContext = context;
        mSP = mContext.getSharedPreferences("_" + name,Context.MODE_PRIVATE);
    }

    public Map<String, Object> getProperities()
    {
        return mElements;
    }

    //construct user from cursor,
    //basic: if cursor only contain BASIC_COLUMNS
    public User(Cursor cursor)
    {
        this();

        //retrieve data from cursor and set to user
        //TODO, here just give out debug information
        int index;
        int i;
        String str;

        for(String col : cursor.getColumnNames())
        {
            index = cursor.getColumnIndex(col);
            int type = cursor.getType(index);
            switch(type)
            {
                case Cursor.FIELD_TYPE_INTEGER:
                    i = cursor.getInt(index);
                    mElements.put(col, i);
                    if(LOGD){
                        Log.d(tag, col + " : " + i);
                    }
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    str = cursor.getString(index);
                    mElements.put(col, str);
                    if(LOGD){
                        Log.d(tag, col + " : " + str);
                    }
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    if(LOGD){
                        Log.d(tag, col + " is not exist");
                    }
                    break;
                default:
                    if(LOGD){
                        Log.d(tag, col + " return Unknown Type");
                    }
                    break;
            }
        }
    }

    public Object[] buildRow(String[] projection)
    {
        int size = projection.length;
        Object[] oa = new Object[size];
        int index = 0;
        Object obj;
        for(String key : projection)
        {
            obj = mElements.get(key);
            if(obj == null && LOGD)
                Log.d(tag, "key " + key + " is not exist");
            if(LOGD)
            {
                Log.d(tag, "query user " + mElements);
            }
            oa[index] = obj;
            index ++;
        }
        return oa;
    }

    public synchronized void update(ContentValues values)
    {
        SharedPreferences.Editor editor = mSP.edit();
        for(String key : values.keySet())
        {
            if(     key.contentEquals(UsersContract.TableUsers.Column.ID) ||
                    key.contentEquals(UsersContract.TableUsers.Column.AGE) ||
                    key.contentEquals(UsersContract.TableUsers.Column.GAMERULE) ||
                    key.contentEquals(UsersContract.TableUsers.Column.HOMERULE) ||
                    key.contentEquals(UsersContract.TableUsers.Column.FLAG) ||
                    key.contentEquals(UsersContract.TableUsers.Column.BASETIME) ||
                    key.contentEquals(UsersContract.TableUsers.Column.PREV_LEFT_DATE) ||
                    key.contentEquals(UsersContract.TableUsers.Column.PREV_LEFT_TIME) ||
                    key.contentEquals(UsersContract.TableUsers.Column.INSTALL_DATE)
                    )
            {
                mElements.put(key, values.getAsInteger(key));
                editor.putInt(key, values.getAsInteger(key));
            }
            else if(key.contentEquals(UsersContract.TableUsers.Column.ISMALE))
            {
                mElements.put(key, values.getAsBoolean(key));
                editor.putBoolean(key, values.getAsBoolean(key));
            }
            else if(key.contentEquals(UsersContract.TableUsers.Column.LEFTTIME))
            {
                int lefttime = values.getAsInteger(key);
                //update gamerule_activate and homerule_activate of lefttime field
                Integer gamerule_activate = (Integer)mElements.get(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE);
                Integer homerule_activate = (Integer)mElements.get(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE);
                if(gamerule_activate == null) gamerule_activate = 0;
                if(homerule_activate == null) homerule_activate = 0;
                if(lefttime > 0)
                {
                    gamerule_activate = UsersContract.TableRule.clearBit(gamerule_activate, UsersContract.TableRule.BIT_TIME_LIMITED);
                    homerule_activate = UsersContract.TableRule.clearBit(homerule_activate, UsersContract.TableRule.BIT_TIME_LIMITED);
                }
                else
                {
                    gamerule_activate = UsersContract.TableRule.setBit(gamerule_activate, UsersContract.TableRule.BIT_TIME_LIMITED);
                    homerule_activate = UsersContract.TableRule.setBit(homerule_activate, UsersContract.TableRule.BIT_TIME_LIMITED);
                }
                mElements.put(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE, gamerule_activate);
                mElements.put(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE, homerule_activate);
                mElements.put(key, lefttime);

            }
            else if(key.contentEquals(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE) ||
                    key.contentEquals(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE)
                    )
            {
                mElements.put(key, values.getAsInteger(key));
            }
            else if(key.contentEquals(UsersContract.TableUsers.Column.RANGE1) ||
                    key.contentEquals(UsersContract.TableUsers.Column.RANGE2) ||
                    key.contentEquals(UsersContract.TableUsers.Column.RANGE3) ||
                    key.contentEquals(UsersContract.TableUsers.Column.RANGE4) ||
                    key.contentEquals(UsersContract.TableUsers.Column.RANGE5))
            {
                mElements.put(key, values.getAsInteger(key));
                editor.putInt(key, values.getAsInteger(key));
            }
            else
            {
                mElements.put(key, values.getAsString(key));
                editor.putString(key, values.getAsString(key));
            }

        }
        editor.apply();
    }

    static class Holiday{
        int start;
        int end;
    }

    private static ArrayList<Holiday> mHolidayList;
    private static int mIndexYear;

    private static void holidayIndexing()
    {
        Calendar cld = Calendar.getInstance();
        Holiday holiday;

        mHolidayList = new ArrayList<Holiday>();
        mIndexYear = cld.get(Calendar.YEAR);

        //HanJia
        holiday = new Holiday();
        cld.set(Calendar.MONTH, Calendar.JANUARY);
        cld.set(Calendar.DAY_OF_MONTH, 20);
        holiday.start = cld.get(Calendar.DAY_OF_YEAR);

        cld.set(Calendar.MONTH, Calendar.FEBRUARY);
        cld.set(Calendar.DAY_OF_MONTH, 24);
        holiday.end = cld.get(Calendar.DAY_OF_YEAR);
        mHolidayList.add(holiday);

        //SuJia
        holiday = new Holiday();
        cld.set(Calendar.MONTH, Calendar.JUNE);
        cld.set(Calendar.DAY_OF_MONTH, 30);
        holiday.start = cld.get(Calendar.DAY_OF_YEAR);

        cld.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cld.set(Calendar.DAY_OF_MONTH, 1);
        holiday.end = cld.get(Calendar.DAY_OF_YEAR);
        mHolidayList.add(holiday);

        //Yuan Dan
        holiday = new Holiday();
        cld.set(Calendar.MONTH, Calendar.JANUARY);
        cld.set(Calendar.DAY_OF_MONTH, 1);
        holiday.start = cld.get(Calendar.DAY_OF_YEAR);

        cld.set(Calendar.MONTH, Calendar.JANUARY);
        cld.set(Calendar.DAY_OF_MONTH, 3);
        holiday.end = cld.get(Calendar.DAY_OF_YEAR);
        mHolidayList.add(holiday);

        //Guo Qing
        holiday = new Holiday();
        cld.set(Calendar.MONTH, Calendar.OCTOBER);
        cld.set(Calendar.DAY_OF_MONTH, 1);
        holiday.start = cld.get(Calendar.DAY_OF_YEAR);

        cld.set(Calendar.MONTH, Calendar.OCTOBER);
        cld.set(Calendar.DAY_OF_MONTH, 7);
        holiday.end = cld.get(Calendar.DAY_OF_YEAR);
        mHolidayList.add(holiday);

        //5.1
        holiday = new Holiday();
        cld.set(Calendar.MONTH, Calendar.MAY);
        cld.set(Calendar.DAY_OF_MONTH, 1);
        holiday.start = cld.get(Calendar.DAY_OF_YEAR);

        cld.set(Calendar.MONTH, Calendar.MAY);
        cld.set(Calendar.DAY_OF_MONTH, 3);
        holiday.end = cld.get(Calendar.DAY_OF_YEAR);
        mHolidayList.add(holiday);
    }

    public static boolean isHoliday(Calendar cld)
    {
        int day = cld.get(Calendar.DAY_OF_YEAR);

        if(cld.get(Calendar.YEAR) != mIndexYear){
            holidayIndexing();
        }

        for(Holiday holiday : mHolidayList){
            if(day >= holiday.start && day <= holiday.end){
                return true;
            }
        }

        int weekday = cld.get(Calendar.DAY_OF_WEEK);

        if(weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY)
        {
            return true;
        }
        return false;
    }


    private int getDayType(Calendar cld)
    {
        if(isHoliday(cld)) {
            return UsersContract.TableUsers.HOLIDAY;
        }
        return UsersContract.TableUsers.WORKDAY;
    }

    public String[] timeRange(int bit)
    {
        String range;
        switch(bit)
        {
            case UsersContract.TableRule.BIT_RANGE1:
                range = UsersContract.TableUsers.Column.RANGE1;
                break;
            case UsersContract.TableRule.BIT_RANGE2:
                range = UsersContract.TableUsers.Column.RANGE2;
                break;
            case UsersContract.TableRule.BIT_RANGE3:
                range = UsersContract.TableUsers.Column.RANGE3;
                break;
            case UsersContract.TableRule.BIT_RANGE4:
                range = UsersContract.TableUsers.Column.RANGE4;
                break;
            case UsersContract.TableRule.BIT_RANGE5:
                range = UsersContract.TableUsers.Column.RANGE5;
                break;
            default:
                return new String[]{"", ""};
        }
        if(mElements.get(range) == null) return new String[]{"", ""};
        int val = (Integer) mElements.get(range);
        int fromHour = UsersContract.TableUsers.fromHour(val);
        int fromMinute = UsersContract.TableUsers.fromMinute(val);
        int toHour = UsersContract.TableUsers.toHour(val);
        int toMinute = UsersContract.TableUsers.toMinute(val);
        int type = UsersContract.TableUsers.dayType(val);
        String stype = "";
        if(type == UsersContract.TableUsers.HOLIDAY){
            stype = mContext.getResources().getString(R.string.holiday);
        }
        else if(type == UsersContract.TableUsers.WORKDAY){
            stype = mContext.getResources().getString(R.string.workday);
        }
        else if(type == UsersContract.TableUsers.WORKDAY){
            stype = mContext.getResources().getString(R.string.everyday);
        }

        String s = String.format("[%02d:%02d - %02d:%02d]", fromHour, fromMinute, toHour, toMinute);

        return new String[]{stype, s};
    }

    private void timeRangeCheck(Calendar cld, int bit, String range)
    {
        boolean activate = false;
        if(mElements.get(range) != null){
            int val = (Integer) mElements.get(range);
            int daytype = UsersContract.TableUsers.dayType(val);
            int fromHour = UsersContract.TableUsers.fromHour(val);
            int fromMinute = UsersContract.TableUsers.fromMinute(val);
            int toHour = UsersContract.TableUsers.toHour(val);
            int toMinute = UsersContract.TableUsers.toMinute(val);

            if (LOGD) {
                Log.d(tag, "daytype " + daytype + " [" + fromHour + ":" + fromMinute + "] - ["
                        + toHour + ":" + toMinute + "]");
            }
            //mismatch rule
            if (daytype == getDayType(cld) || daytype == UsersContract.TableUsers.EVERYDAY){
                int startMinutes = fromHour * 60 + fromMinute;
                int endMinutes = toHour * 60 + toMinute;
                int currentMinutes = cld.get(Calendar.HOUR_OF_DAY)*60 + cld.get(Calendar.MINUTE);
                if(currentMinutes >= startMinutes && currentMinutes <= endMinutes)
                {
                    activate = false;
                }
                else
                {
                    activate = true;
                }
            }
        }
        Integer gamerule_activate = (Integer)mElements.get(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE);
        Integer homerule_activate = (Integer)mElements.get(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE);
        if(gamerule_activate == null) gamerule_activate = 0;
        if(homerule_activate == null) homerule_activate = 0;

        if(activate)
        {
            gamerule_activate = UsersContract.TableRule.setBit(gamerule_activate, bit);
            homerule_activate = UsersContract.TableRule.setBit(homerule_activate, bit);
        }
        else
        {
            gamerule_activate = UsersContract.TableRule.clearBit(gamerule_activate, bit);
            homerule_activate = UsersContract.TableRule.clearBit(homerule_activate, bit);
        }
        mElements.put(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE, gamerule_activate);
        mElements.put(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE, homerule_activate);
    }

    public int getOutOfRange(int lockbit)
    {
        String rangefield = null;
        switch(lockbit)
        {
            case UsersContract.TableRule.BIT_RANGE1:
                rangefield = UsersContract.TableUsers.Column.RANGE1;
                break;
            case UsersContract.TableRule.BIT_RANGE2:
                rangefield = UsersContract.TableUsers.Column.RANGE2;
                break;
            case UsersContract.TableRule.BIT_RANGE3:
                rangefield = UsersContract.TableUsers.Column.RANGE3;
                break;
            case UsersContract.TableRule.BIT_RANGE4:
                rangefield = UsersContract.TableUsers.Column.RANGE4;
                break;
            case UsersContract.TableRule.BIT_RANGE5:
                rangefield = UsersContract.TableUsers.Column.RANGE5;
                break;
        }
        if(rangefield == null) return 0;

        Integer val = (Integer) mElements.get(rangefield);
        if(val == null) return 0;

        int toHour = UsersContract.TableUsers.toHour(val);
        int toMinute = UsersContract.TableUsers.toMinute(val);

        Calendar cld = Calendar.getInstance();
        int curHour = cld.get(Calendar.HOUR_OF_DAY);
        int curMinute = cld.get(Calendar.MINUTE);

        return (curHour*60 + curMinute - toHour*60 - toMinute)*60;
    }

    public void setTimeRangeActivateRule()
    {
        Calendar cld = Calendar.getInstance();

        timeRangeCheck(cld, UsersContract.TableRule.BIT_RANGE1, UsersContract.TableUsers.Column.RANGE1);
        timeRangeCheck(cld, UsersContract.TableRule.BIT_RANGE2, UsersContract.TableUsers.Column.RANGE2);
        timeRangeCheck(cld, UsersContract.TableRule.BIT_RANGE3, UsersContract.TableUsers.Column.RANGE3);
        timeRangeCheck(cld, UsersContract.TableRule.BIT_RANGE4, UsersContract.TableUsers.Column.RANGE4);
        timeRangeCheck(cld, UsersContract.TableRule.BIT_RANGE5, UsersContract.TableUsers.Column.RANGE5);
    }

    public void setWifiActivateRule()
    {
        int gamerule = (Integer)mElements.get(UsersContract.TableUsers.Column.GAMERULE);
        int homerule = (Integer)mElements.get(UsersContract.TableUsers.Column.HOMERULE);
        if( (!UsersContract.TableRule.isBitSet(gamerule, UsersContract.TableRule.BIT_WIFI_CONNECT)) &&
                (!UsersContract.TableRule.isBitSet(homerule, UsersContract.TableRule.BIT_WIFI_CONNECT)))
        {
            return;
        }

        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        Integer gamerule_activate = (Integer)mElements.get(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE);
        Integer homerule_activate = (Integer)mElements.get(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE);
        if(gamerule_activate == null) gamerule_activate = 0;
        if(homerule_activate == null) homerule_activate = 0;

        if(mWifi.isConnected() && mWifi.isAvailable())
        {
            //clear activate rule
            gamerule_activate = UsersContract.TableRule.clearBit(gamerule_activate, UsersContract.TableRule.BIT_WIFI_CONNECT);
            homerule_activate = UsersContract.TableRule.clearBit(homerule_activate, UsersContract.TableRule.BIT_WIFI_CONNECT);
            mWifiLostInitTime = 0;
            mWifiLostSeconds = 0;
        }
        else
        {
            gamerule_activate = UsersContract.TableRule.setBit(gamerule_activate, UsersContract.TableRule.BIT_WIFI_CONNECT);
            homerule_activate = UsersContract.TableRule.setBit(homerule_activate, UsersContract.TableRule.BIT_WIFI_CONNECT);
            if(mWifiLostInitTime == 0) {
                mWifiLostInitTime = System.currentTimeMillis();
                mWifiLostSeconds = 0;
            }
            else
            {
                mWifiLostSeconds = (System.currentTimeMillis() - mWifiLostInitTime) / 1000;
            }
        }

        //set activate rule
        mElements.put(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE, gamerule_activate);
        mElements.put(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE, homerule_activate);
    }

    @Override
    public String toString() {
        return "User{" +
                "mElements=" + mElements +
                '}';
    }
}
