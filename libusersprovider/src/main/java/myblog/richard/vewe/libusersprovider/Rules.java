package myblog.richard.vewe.libusersprovider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 15-11-10.
 */
public class Rules extends ProviderOpts {
    private static final String tag = "rules";
    private static boolean LOGD = true;
    private static Rules ourInstance = new Rules();
    private Map<Integer, Object> mItems;    //the object can be resource id or string
    private SharedPreferences mSP;

    public static Rules getInstance() {
        return ourInstance;
    }

    private Rules()
    {
        mItems = new HashMap<Integer, Object>();
        mItems.put(UsersContract.TableRule.BIT_TIME_LIMITED, R.string.rule_app_timelimited);    //resource id
        mItems.put(UsersContract.TableRule.BIT_RANGE1, R.string.rule_period_limited);
        mItems.put(UsersContract.TableRule.BIT_RANGE2, R.string.rule_period_limited);
        mItems.put(UsersContract.TableRule.BIT_RANGE3, R.string.rule_period_limited);
        mItems.put(UsersContract.TableRule.BIT_RANGE4, R.string.rule_period_limited);
        mItems.put(UsersContract.TableRule.BIT_RANGE5, R.string.rule_period_limited);
        mItems.put(UsersContract.TableRule.BIT_WIFI_CONNECT, R.string.rule_app_wifi_disable);
    }

    public Map<Integer, Object> getRuleItems()
    {
        return mItems;
    }

    public String getForbiddenReason()
    {
        return mContext.getString(R.string.rule_app_locked);
    }

    public int lockBit(int lockflag)
    {
        for(int i = 0; i < 32; i ++) {
            if (((lockflag >> i) & 0x1) == 0x1) {
                return i;
            }
        }
        return 0;
    }

    public String getReason(int lockbits, User user)
    {
        int bit;
        String reason = null;

        for(int i = 0; i < 32; i ++)
        {
            Object val = mItems.get(i);
            if(val == null) continue;
            if(((lockbits >> i) & 0x1) == 0x1)
            {
                if(val instanceof Integer) {
                    reason = mContext.getString((int) val);
                    if((int)val == R.string.rule_period_limited)
                    {
                        //need append time range
                        String[] ss = user.timeRange(i);
                        reason = ss[0] + reason + ss[1];
                    }
                }
                else
                {
                    reason = (String)val;
                }
                break;
            }
        }
        return reason;
    }

    private Context mContext;
    public void setContext(Context context)
    {
        //first time read user rules from preference file
        if(mContext == null)
        {
            mSP = context.getSharedPreferences("rule", Context.MODE_PRIVATE);
            Map<String, ?> allEntries = mSP.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                mItems.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }
        }
        mContext = context;
    }

    private Object[] buildRow(Map.Entry<Integer, Object> entry)
    {
        Object[] ao = new Object[2];

        ao[0] = entry.getKey();
        ao[1] = entry.getValue();
        return ao;
    }

    private Object[] buildRow(Integer i, Object o)
    {
        Object[] ao = new Object[2];

        ao[0] = i;
        ao[1] = o;
        return ao;
    }

    @Override
    public synchronized Cursor query(String[] projection, String selection) {
        MatrixCursor matrixCursor= new MatrixCursor(UsersContract.TableRule.COLUMNS);
        //add rows for every user
        if(selection == null)
        {
            for(Map.Entry<Integer, Object> entry : mItems.entrySet())
            {
                matrixCursor.addRow(buildRow(entry));
            }
        }
        else
        {
            int bit;
            int index;

            index = selection.lastIndexOf("=");
            if(index == -1)
            {
                return null;
            }
            bit = Integer.parseInt(selection.substring(index + 1));

            matrixCursor.addRow(buildRow(bit, mItems.get(bit)));
        }
        return matrixCursor;
    }

    @Override
    public synchronized Uri insert(ContentValues values) {
        Integer i = values.getAsInteger(UsersContract.TableRule.Column.BIT);
        String r = values.getAsString(UsersContract.TableRule.Column.REASON);
        mItems.put(i, r);
        SharedPreferences.Editor editor = mSP.edit();
        editor.putString(i.toString(), r);
        editor.apply();
        return Uri.parse("content://" + UsersContract.AUTHORITY + "/rule/" + i);
    }
}
