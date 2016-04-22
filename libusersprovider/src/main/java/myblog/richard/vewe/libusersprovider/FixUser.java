package myblog.richard.vewe.libusersprovider;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by richard on 15-11-10.
 */
public class FixUser extends User {
    public FixUser(String name, Context context)
    {
        super(name, context);
        //set rule workday and weekend
        int rule = UsersContract.TableRule.setBit(0, UsersContract.TableRule.BIT_RANGE1);
        rule = UsersContract.TableRule.setBit(rule, UsersContract.TableRule.BIT_RANGE2);
        rule = UsersContract.TableRule.setBit(rule, UsersContract.TableRule.BIT_WIFI_CONNECT);
        mElements.put(UsersContract.TableUsers.Column.HOMERULE, rule);
        rule = UsersContract.TableRule.setBit(rule, UsersContract.TableRule.BIT_TIME_LIMITED);
        mElements.put(UsersContract.TableUsers.Column.GAMERULE, rule);

        mElements.put(UsersContract.TableUsers.Column.GAMERULE_ACTIVATE, 0);
        mElements.put(UsersContract.TableUsers.Column.HOMERULE_ACTIVATE, 0);

        int val = UsersContract.TableUsers.setDayType(0, UsersContract.TableUsers.WORKDAY);
        val = UsersContract.TableUsers.setFromHour(val, 16);
        val = UsersContract.TableUsers.setFromMinute(val, 00);
        val = UsersContract.TableUsers.setToHour(val, 22);
        val = UsersContract.TableUsers.setToMinute(val, 0);
        mElements.put(UsersContract.TableUsers.Column.RANGE2, val);

        val = UsersContract.TableUsers.setDayType(0, UsersContract.TableUsers.EVERYDAY);
        val = UsersContract.TableUsers.setFromHour(val, 8);
        val = UsersContract.TableUsers.setFromMinute(val, 0);
        val = UsersContract.TableUsers.setToHour(val, 22);
        val = UsersContract.TableUsers.setToMinute(val, 0);
        mElements.put(UsersContract.TableUsers.Column.RANGE1, val);

        if(name.contentEquals(UsersContract.TableUsers.ROOT))
        {
            mElements.put(UsersContract.TableUsers.Column.ID, 0);
            mElements.put(UsersContract.TableUsers.Column.NAME, UsersContract.TableUsers.ROOT);
            mElements.put(UsersContract.TableUsers.Column.PASSWORD, UsersContract.TableUsers.DEFAULT_ROOT_PWD);
            mElements.put(UsersContract.TableUsers.Column.FLAG, (UsersContract.TableUsers.USER_FLAG_ROOT |
                    UsersContract.TableUsers.USER_FLAG_APPLIST));
            mElements.put(UsersContract.TableUsers.Column.APPLIST, UsersContract.TableApplist.ROOT_PATH);
        }
        else //must be diga
        {
            mElements.put(UsersContract.TableUsers.Column.ID, 1);
            mElements.put(UsersContract.TableUsers.Column.NAME, UsersContract.TableUsers.DIGA);
            mElements.put(UsersContract.TableUsers.Column.PASSWORD, null);
            mElements.put(UsersContract.TableUsers.Column.FLAG, (UsersContract.TableUsers.USER_FLAG_APPLIST |
                    UsersContract.TableUsers.USER_SHOW_LEFTTIME |
                    UsersContract.TableUsers.USER_SHOW_PC_ICON |
                    UsersContract.TableUsers.USER_AUTO_MUTE));
            mElements.put(UsersContract.TableUsers.Column.APPLIST, UsersContract.TableApplist.DIGA_PATH);
            mElements.put(UsersContract.TableUsers.Column.BASETIME, UsersContract.TableUsers.DEFAULT_BASETIME);
        }

        synchronized (this) {
            SharedPreferences sp = getPreference();

            Map<String, ?> allEntries = sp.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                mElements.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
