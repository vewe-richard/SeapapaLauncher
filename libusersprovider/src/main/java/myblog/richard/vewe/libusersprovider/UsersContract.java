package myblog.richard.vewe.libusersprovider;

import android.net.Uri;

/**
 * Created by richard on 15-11-9.
 */
public class UsersContract {
    public static final String AUTHORITY = "myblog.richard.vewe.usersprovider";

    public static class TableUsers {
        public static final String USERS_PATH = "users";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + USERS_PATH);

        public static class Column{
            public static final String ID = "_ID";

            public static final String NAME = "NAME";
            public static final String NICKNAME = "NICKNAME";

            public static final String PASSWORD = "PASSWORD";
            public static final String QUESTION = "QUESTION";
            public static final String ANSWER = "ANSWER";

            public static final String AGE = "AGE";

            public static final String ISMALE = "ISMALE";

            public static final String SCHOOL = "SCHOOL"; //school
            public static final String CLASS = "CLASS";

            public static final String PHONE = "PHONE";
            public static final String EMAIL = "EMAIL";

            //image path
            public static final String PROFILE = "PROFILE";

            //
            public static final String GAMERULE = "GAMERULE";
            public static final String HOMERULE = "HOMERULE";
            public static final String FLAG = "FLAG";

            //time related
            public static final String BASETIME = "BASETIME";

            public static final String RANGE1 = "RANGE1";
            public static final String RANGE2 = "RANGE2";
            public static final String RANGE3 = "RANGE3";
            public static final String RANGE4 = "RANGE4";
            public static final String RANGE5 = "RANGE5";

            public static final String EXTRA = "EXTRA";

            public static final String APPLIST = "APPLIST";
            public static final String LEFTTIME = "LEFTTIME";

            public static final String GAMERULE_ACTIVATE = "GAMERULE_ACTIVATE";
            public static final String HOMERULE_ACTIVATE = "HOMERULE_ACTIVATE";

            public static final String PWD2 = "pwd2";

            public static final String PREV_LEFT_TIME = "PREV_LT";
            public static final String PREV_LEFT_DATE = "PREV_DATE";

            public static final String INSTALL_DATE = "INSTALL_DATE";
        } //column

        public static final String[] BASIC_COLUMNS = {
            Column.ID, Column.NAME, Column.FLAG, Column.LEFTTIME, Column.APPLIST, Column.PROFILE, Column.INSTALL_DATE
        };

        public static final String[] ALL_COLUMNS = {
                Column.ID,
                Column.NAME,
                Column.NICKNAME,
                Column.PASSWORD,
                Column.QUESTION,
                Column.ANSWER,
                Column.AGE,
                Column.ISMALE,
                Column.SCHOOL,
                Column.CLASS,
                Column.PHONE,
                Column.EMAIL,
                //image path
                Column.PROFILE,
                //
                Column.GAMERULE,
                Column.HOMERULE,
                Column.FLAG,
                //time related
                Column.BASETIME,
                Column.RANGE1,
                Column.RANGE2,
                Column.RANGE3,
                Column.RANGE4,
                Column.RANGE5,
                Column.EXTRA,
                Column.APPLIST,
                Column.LEFTTIME,
                Column.GAMERULE_ACTIVATE,
                Column.HOMERULE_ACTIVATE,
                Column.PWD2,
        };

        public static final int DEFAULT_BASETIME = 30;
        //for user root/diga/current
        public static final String ROOT = "root";
        public static final String DIGA = "diga";
        public static final String CURRENT = "current";

        public static final String DEFAULT_ROOT_PWD = "111111";

        //User FLAG Bit
        public static final int USER_FLAG_ROOT = 0x00000001;
        public static final int USER_FLAG_APPLIST = 0x00000002;
        public static final int USER_FLAG_PARENT_CONTROL_DISABLE = 0x00000004;
        public static final int USER_SHOW_LEFTTIME = 0x00000008;
        public static final int USER_SHOW_PC_ICON = 0x00000010;
        public static final int USER_AUTO_MUTE = 0x00000020;

        public static boolean showApplist(int flag){
            return (flag & USER_FLAG_APPLIST) == USER_FLAG_APPLIST;
        }

        public static int enableShowApplist(int flag)
        {
            return (flag | USER_FLAG_APPLIST);
        }

        public static int disableShowApplist(int flag)
        {
            return (flag & (~USER_FLAG_APPLIST));
        }

        public static boolean showLeftTime(int flag){
            return (flag & USER_SHOW_LEFTTIME) == USER_SHOW_LEFTTIME;
        }

        public static int enableShowLeftTime(int flag)
        {
            return (flag | USER_SHOW_LEFTTIME);
        }

        public static int disableShowLeftTime(int flag)
        {
            return (flag & (~USER_SHOW_LEFTTIME));
        }

        public static boolean showParentControlIcon(int flag){
            return (flag & USER_SHOW_PC_ICON) == USER_SHOW_PC_ICON;
        }

        public static int enableShowParentControlIcon(int flag)
        {
            return (flag | USER_SHOW_PC_ICON);
        }

        public static int disableShowParentControlIcon(int flag)
        {
            return (flag & (~USER_SHOW_PC_ICON));
        }


        public static boolean isRoot(int flag){
            return (flag & USER_FLAG_ROOT) == USER_FLAG_ROOT;
        }

        public static boolean parentControlDisabled(int flag){
            return (flag & USER_FLAG_PARENT_CONTROL_DISABLE) == USER_FLAG_PARENT_CONTROL_DISABLE;
        }

        public static int enableParentControl(int flag)
        {
            return (flag & (~USER_FLAG_PARENT_CONTROL_DISABLE));
        }

        public static int disableParentControl(int flag)
        {
            return (flag | USER_FLAG_PARENT_CONTROL_DISABLE);
        }

        public static boolean autoMuteEnable(int flag){
            return (flag & USER_AUTO_MUTE) == USER_AUTO_MUTE;
        }

        public static int disableAutoMute(int flag)
        {
            return (flag & (~USER_AUTO_MUTE));
        }

        public static int enableAutoMute(int flag)
        {
            return (flag | USER_AUTO_MUTE);
        }


        //the default value also means lefttime is not exist
        public static final int LEFTTIME_DEFAULT = 0x1FFFFFFF;
        public static final int BASETIME_NOLIMIT = (24*60);

        //time range format use a integer
        //first three bit indicate work day or holiday
        public static final int HOLIDAY = 1;
        public static final int WORKDAY = 2;
        public static final int EVERYDAY = 3;
        public static final int DAYTYPE_OFFSET = 29;
        public static final int DAYTYPE_MASK = (0x7 << DAYTYPE_OFFSET);
        public static int dayType(int value)
        {
            return (value >> DAYTYPE_OFFSET) & 0x7;
        }
        public static int setDayType(int value, int daytype)
        {
            return (value & (~DAYTYPE_MASK)) | (daytype << DAYTYPE_OFFSET);
        }

        //the left 5, 8, 8, 8 bytes are used to store from hour.minutes to hour.minutes
        public static final int FROM_HOUR_OFFSET = 24;
        public static final int FROM_HOUR_MASK = (0x1F << FROM_HOUR_OFFSET);
        public static final int fromHour(int value)
        {
            return (value >> FROM_HOUR_OFFSET) & 0x1F;
        }
        public static final int setFromHour(int value, int fromhour)
        {
            return (value & (~FROM_HOUR_MASK)) | (fromhour << FROM_HOUR_OFFSET);
        }

        public static final int FROM_MINUTE_OFFSET = 16;
        public static final int FROM_MINUTE_MASK = (0xFF << FROM_MINUTE_OFFSET);
        public static final int fromMinute(int value)
        {
            return (value >> FROM_MINUTE_OFFSET) & 0xFF;
        }
        public static final int setFromMinute(int value, int fromMinute)
        {
            return (value & (~FROM_MINUTE_MASK)) | (fromMinute << FROM_MINUTE_OFFSET);
        }
        //
        public static final int TO_HOUR_OFFSET = 8;
        public static final int TO_HOUR_MASK = (0xFF << TO_HOUR_OFFSET);
        public static final int toHour(int value)
        {
            return (value >> TO_HOUR_OFFSET) & 0xFF;
        }
        public static final int setToHour(int value, int tohour)
        {
            return (value & (~TO_HOUR_MASK)) | (tohour << TO_HOUR_OFFSET);
        }

        public static final int TO_MINUTE_OFFSET = 0;
        public static final int TO_MINUTE_MASK = (0xFF << TO_MINUTE_OFFSET);
        public static final int toMinute(int value)
        {
            return (value >> TO_MINUTE_OFFSET) & 0xFF;
        }
        public static final int setToMinute(int value, int toMinute)
        {
            return (value & (~TO_MINUTE_MASK)) | (toMinute << TO_MINUTE_OFFSET);
        }
    } //table users

    public static class TableApplist
    {
        public static final String BASE_URI = "content://" + AUTHORITY + "/applist/";
        public static final String ROOT_PATH = "applist/root";
        public static final String DIGA_PATH = "applist/diga";
        public static final Uri ROOT_URI = Uri.parse("content://" + AUTHORITY + "/" + ROOT_PATH);
        public static final Uri DIGA_URI = Uri.parse("content://" + AUTHORITY + "/" + DIGA_PATH);
        public static class Column {
            public static final String PKG = "PKG";
            public static final String TYPE = "TYPE";
            public static final String PREFIX = "PREFIX";
            public static final String LOCK = "LOCK";
            public static final String REASON = "REASON";
            public static final String HINT = "HINT";

        }//column
        public static final int FREE = 1;
        public static final int HOME = 2;
        public static final int GAME = 3;
        public static final int FORBIDDEN = 4;
        public static final int UNKNOWN = 5;
        public static final String[] BASIC_COLUMNS = {
            Column.PKG, Column.TYPE, Column.PREFIX
        };

        public static final String FORBIDDEN_PREFIX = "\uD83D\uDD12";
        public static final String HOME_PREFIX = "\uD83D\uDC27";
        public static final String GAME_PREFIX = "\u23F3";
    } //table applist

    public static class TableRule
    {
        public static final String RULE_PATH = "rule";
        public static final Uri RULE_URI = Uri.parse("content://" + AUTHORITY + "/" + RULE_PATH);

        public static class Column {
            public static final String BIT = "BIT";
            public static final String REASON = "REASON";

        }//column

        public static final String[] COLUMNS = {
                Column.BIT, Column.REASON
        };

        //rule bit definition
        public static final int BIT_RANGE1 = 3;
        public static final int BIT_RANGE2 = 4;
        public static final int BIT_RANGE3 = 5;
        public static final int BIT_RANGE4 = 6;
        public static final int BIT_RANGE5 = 7;

        public static final int BIT_TIME_LIMITED = 10;
        public static final int BIT_WIFI_CONNECT = 16;

        public static int setBit(int value, int bit)
        {
            value = (0x1 << bit) | value;
            return value;
        }

        public static boolean isBitSet(int value, int bit)
        {
            return (((value >> bit) & 0x1) == 0x1);
        }

        public static int clearBit(int value, int bit)
        {
            value = (~(0x1 << bit)) & value;
            return value;
        }
    } //table rule

    public static class TableCurrent
    {
        public static final String CURRENT_PATH = "current";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CURRENT_PATH);
        public static class Column {
            public static final String KEY = "KEY";
            public static final String VAL = "VAL";
        }
        public static final String KEY_CURRENT = "CURRENT";
        public static final String KEY_HINT_STRATAGE = "HINT_STRATAGE";
        public static final String KEY_INSTALLER = "INSTALLER";

        public static final int HINT_ONLY_HINT = 0;
        public static final int HINT_LOCK_AFTER = 1;
        public static final int HINT_DIRECT_LOCK = 2;

        public static final int DEFAULT_TIME_TOLOCK = (5*60);   //lock after 5 minutes

    }

    //Table URI map to integer
    public static final int TABLE_USERS = 1;
    public static final int TABLE_CURRENT = 2;
    public static final int TABLE_RULE = 3;
    public static final int TABLE_APPLIST_BASE1 = 100; //for predefined users
    public static final int TABLE_APPLIST_ROOT = (TABLE_APPLIST_BASE1 + 1);
    public static final int TABLE_APPLIST_DIGA = (TABLE_APPLIST_BASE1 + 2);
    public static final int TABLE_APPLIST_BASE2 = 200;

    public static final String LOCAL_SERVER = "http://10.0.2.2:8080/";
    public static final String BUCCANEER_SERVER = "http://buccaneer.aliapp.com/";
    private static String mServerUrl = BUCCANEER_SERVER;
    public static final String FIRSTTIME_URL = "myblog2/firsttime.html";
    public static final String USERGUIDE_URL = "myblog2/userguide.html";
    public static final String ABOUT_URL = "myblog2/aboutproject.html";
    public static final String DEVICE_SPECIFIC = "HelloAce.do?device_specific";

    public static boolean FIRST_TIME_INSTALLATION = false;

    public static void setServerUrl(String serverUrl){
        mServerUrl = serverUrl;
    }

    public static String getServerUrl(){
        return mServerUrl;
    }
}
