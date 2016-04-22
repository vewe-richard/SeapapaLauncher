package myblog.richard.vewe.libusersprovider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by richard on 15-11-10.
 */
public class Applist extends ProviderOpts{
    private String mUser;
    private SharedPreferences mSP;
    private User mUserObj;
    private static final String tag = "Applist";
    private static final boolean LOGD = true;
    private Context mContext;

    public String userName()
    {
        return mUser;
    }

    //get applist for user
    public Applist(String user, Context context)
    {
        mUser = user;
        mSP = Applists.getInstance().getContext().getSharedPreferences("applist_" + user, Context.MODE_PRIVATE);
        //if size in sp is zero, this is the first time install
        //auto grouping
        if(mSP.getAll().size() == 0){
            GroupingApps ga = new GroupingApps(context.getPackageManager(), true);
            SharedPreferences.Editor editor = mSP.edit();
            for(String pkg : ga.getmListFree()) {
                editor.putInt(pkg, UsersContract.TableApplist.FREE);
            }
            for(String pkg : ga.getmListHome()) {
                editor.putInt(pkg, UsersContract.TableApplist.HOME);
            }
            for(String pkg : ga.getmListGame()) {
                editor.putInt(pkg, UsersContract.TableApplist.GAME);
            }
            for(String pkg : ga.getmListForbidden()) {
                editor.putInt(pkg, UsersContract.TableApplist.FORBIDDEN);
            }
            editor.apply();
        }
        mUserObj = Applists.getInstance().getUser(user);
        mContext = context;
    }

    @Override
    public synchronized int delete(String selection) {
        SharedPreferences.Editor editor = mSP.edit();
        int ret;
        if(selection == null)
        {
            ret = mSP.getAll().size();
            editor.clear();
        }
        else
        {
            int index = selection.lastIndexOf("=");
            if(index == -1) return 0;

            String key = selection.substring(index + 1);
            if(!mSP.contains(key)){
                return 0;
            }
            editor.remove(key);
            ret = 1;
        }
        editor.apply();
        return ret;
    }

    @Override
    public synchronized Uri insert(ContentValues values)
    {
        String pkg = values.getAsString(UsersContract.TableApplist.Column.PKG);
        int type = values.getAsInteger(UsersContract.TableApplist.Column.TYPE);
        SharedPreferences.Editor editor = mSP.edit();
        editor.putInt(pkg, type);
        editor.apply();
        return Uri.parse("content://" + UsersContract.AUTHORITY + "/applist/" + mUser);
    }

    private String getPrefix(int type)
    {
        String prefix = "";
        //check if the user is root
        int flag;
        flag = (Integer)mUserObj.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        if(UsersContract.TableUsers.isRoot(flag))
        {
            return prefix;
        }

        switch(type)
        {
            case UsersContract.TableApplist.FORBIDDEN:
                prefix = UsersContract.TableApplist.FORBIDDEN_PREFIX;
                break;
            case UsersContract.TableApplist.HOME:
                //prefix = "\uD83C\uDFE0";    //house
                prefix = UsersContract.TableApplist.HOME_PREFIX;
                break;
            case UsersContract.TableApplist.GAME:
                //prefix = "\uD83D\uDD50";  //clock
                //prefix = "\ud83c\udfae";
                prefix = UsersContract.TableApplist.GAME_PREFIX;          //shalou clock
                break;
            default:
                break;
        }
        return prefix;
    }

    private boolean isLock(int type, int lockflag)
    {
        //get current user and it's lock rule
        //check if the user is root
        int flag;
        flag = (Integer)mUserObj.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        if(UsersContract.TableUsers.isRoot(flag))
        {
            return false;
        }

        int rule, activate;
        switch(type)
        {
            case UsersContract.TableApplist.FORBIDDEN:
                return true;
            case UsersContract.TableApplist.HOME:
                return (lockflag != 0);
            case UsersContract.TableApplist.GAME:
                return (lockflag != 0);
        }

        return false;
    }

    private String getReason(int type, int lockflag)
    {
        int rule, activate;
        String reason = null;
        switch(type)
        {
            case UsersContract.TableApplist.FORBIDDEN:
                reason = Rules.getInstance().getForbiddenReason();
                return reason;
            case UsersContract.TableApplist.HOME:
                reason = Rules.getInstance().getReason(lockflag, mUserObj);
                break;
            case UsersContract.TableApplist.GAME:
                reason = Rules.getInstance().getReason(lockflag, mUserObj);
                break;
        }
        return reason;
    }

    private String getHint(int type, int lockflag)
    {
        //Watchdog will request hint, if hint available, it display hint, else there are only reason, it locked the app
        //we should check if we should provide the hint message to the query
        if(type != UsersContract.TableApplist.HOME && type != UsersContract.TableApplist.GAME)
            return null;

        //we only provide hint to game or home group apps
        Current cur = Current.getInstance();
        int strategy = cur.getHintStrategy();

        //no hint, just lock it by showing the reason
        if(strategy == UsersContract.TableCurrent.HINT_DIRECT_LOCK) {
            return null;
        }

        if(lockflag == 0) return null;

        int lockbit = Rules.getInstance().lockBit(lockflag);

        if(strategy == UsersContract.TableCurrent.HINT_LOCK_AFTER)
        {
            //check if it is out of range, if so, then return null, and no more any hint
            if(outofRange(lockbit)){
                return null;
            }
        }

        //we should provide hint message
        if(lockbit == UsersContract.TableRule.BIT_TIME_LIMITED)
        {
            return mContext.getString(R.string.hint_lefttime);
        }
        else if(lockbit == UsersContract.TableRule.BIT_WIFI_CONNECT)
        {
            return mContext.getString(R.string.hint_wifi);
        }
        else if(lockbit >= UsersContract.TableRule.BIT_RANGE1 && lockbit <= UsersContract.TableRule.BIT_RANGE5)
        {
            String a = mContext.getString(R.string.hint_curtime);
            Calendar cld = Calendar.getInstance();
            a += " " + cld.get(Calendar.HOUR_OF_DAY) + ":" + cld.get(Calendar.MINUTE) + ",";
            a += mContext.getString(R.string.hint_quit);
            return a;
        }


        return null;
    }

    private boolean outofRange(int lockbit)
    {
        User user = Current.getInstance().getmCurrentUserObject();

        if(lockbit == UsersContract.TableRule.BIT_TIME_LIMITED)
        {
            Integer lefttime = (Integer)user.getProperities().get(UsersContract.TableUsers.Column.LEFTTIME);
            if(lefttime == null) return false;
            if(LOGD) {
                Log.d(tag, "lefttime: " + lefttime);
            }
            int exceed = -lefttime;
            if(exceed > UsersContract.TableCurrent.DEFAULT_TIME_TOLOCK)
            {
                return true;
            }
            return false;
        }
        else if(lockbit == UsersContract.TableRule.BIT_WIFI_CONNECT)
        {
            int wifiLostSecond = (int)user.getmWifiLostSeconds();
            if(wifiLostSecond > UsersContract.TableCurrent.DEFAULT_TIME_TOLOCK)
            {
                return true;
            }
            return false;
        }
        else if(lockbit >= UsersContract.TableRule.BIT_RANGE1 && lockbit <= UsersContract.TableRule.BIT_RANGE5)
        {
            int exceed = user.getOutOfRange(lockbit);
            if(exceed > UsersContract.TableCurrent.DEFAULT_TIME_TOLOCK)
            {
                return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    private int getLockFlag(int type)
    {
        int rule, activate;
        int lockflag = 0;

        if(type == UsersContract.TableApplist.HOME)
        {
            rule = (Integer)mUserObj.getProperities().get(UsersContract.TableUsers.Column.HOMERULE);
            activate = (Integer)mUserObj.getProperities().get(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE);
            lockflag = rule&activate;
        }
        else if(type == UsersContract.TableApplist.GAME)
        {
            rule = (Integer)mUserObj.getProperities().get(UsersContract.TableUsers.Column.GAMERULE);
            activate = (Integer)mUserObj.getProperities().get(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE);
            lockflag = rule&activate;
        }
        return lockflag;
    }

    private Object[] buildRow(String[] projection, String pkg, Integer type)
    {
        int size = projection.length;
        Object[] rows = new Object[size];

        if(LOGD) {
            Log.d(tag, "buildRow pkg " + pkg + ":" + type);
        }

        //check if we should provide lock status, if so, get lock flags ahead
        int lockflag = 0;
        if(type == UsersContract.TableApplist.HOME || type == UsersContract.TableApplist.GAME) {
            for (String str : projection) {
                if (str.contentEquals(UsersContract.TableApplist.Column.LOCK)) {
                    lockflag = getLockFlag(type);
                    break;
                }
            }
        }

        for(int i = 0; i < size; i ++)
        {
            if(projection[i].contentEquals(UsersContract.TableApplist.Column.PKG))
            {
                rows[i] = pkg;
            }
            else if(projection[i].contentEquals(UsersContract.TableApplist.Column.TYPE))
            {
                rows[i] = type;
            }
            else if(projection[i].contentEquals(UsersContract.TableApplist.Column.LOCK))
            {
                rows[i] = isLock(type, lockflag);
            }
            else if(projection[i].contentEquals(UsersContract.TableApplist.Column.PREFIX))
            {
                rows[i] = getPrefix(type);
            }
            else if(projection[i].contentEquals(UsersContract.TableApplist.Column.REASON))
            {
                rows[i] = getReason(type, lockflag);
            }
            else if(projection[i].contentEquals(UsersContract.TableApplist.Column.HINT))
            {
                rows[i] = getHint(type, lockflag);
            }
            else
            {
                rows[i] = null;
            }
        }

        return rows;
    }

    @Override
    public synchronized Cursor query(String[] projection, String selection) {
        int ret;

        MatrixCursor matrixCursor= new MatrixCursor(projection);

        //setTimeRangeActivateRule()
        mUserObj.setTimeRangeActivateRule();
        //wifi
        mUserObj.setWifiActivateRule();
        if(selection == null)
        {
            for(Map.Entry<String, ?> entry : mSP.getAll().entrySet())
            {
                matrixCursor.addRow(buildRow(projection, entry.getKey(), (Integer) entry.getValue()));
            }
        }
        else
        {
            int index = selection.lastIndexOf("=");
            if(index == -1) return null;

            String key = selection.substring(index + 1);
            //change default value to free, I think, here should have matched packages stored
            Integer type = mSP.getInt(key, UsersContract.TableApplist.FREE);
            matrixCursor.addRow(buildRow(projection, key, type));
        }
        return matrixCursor;
    }

    @Override
    public synchronized int update(ContentValues values, String selection)
    {
        SharedPreferences.Editor editor = mSP.edit();
        int ret;
        Integer type = values.getAsInteger(UsersContract.TableApplist.Column.TYPE);
        if(type == null) return 0;

        if(selection == null)
        {
            ret = mSP.getAll().size();
            for(Map.Entry<String, ?> entry : mSP.getAll().entrySet())
            {
                editor.putInt(entry.getKey(), type);
            }
        }
        else
        {
            int index = selection.lastIndexOf("=");
            if(index == -1) return 0;

            String key = selection.substring(index + 1);
            editor.putInt(key, type);
            ret = 1;
        }
        editor.apply();
        return ret;
    }
}
