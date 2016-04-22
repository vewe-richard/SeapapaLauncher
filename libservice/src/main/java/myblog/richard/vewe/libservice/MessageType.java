package myblog.richard.vewe.libservice;

/**
 * Created by richard on 15-10-13.
 */
public class MessageType {
    public class Request
    {
        public static final String SERL1 = "serial1";
        public static final int LEFTTIME_R  = 1;
        public static final int RECORDS_R   = 2;
        public static final int GETACTION_R = 3;
        public static final int GETABOUTPROJECT_R = 4;

        public static final int SU          = 100;
        public static final int ADDTIME     = 101;
        public static final int LOGOUT      = 102;
        public static final int SET_AUTHORISED = 103;
        public static final int CLEAR_AUTHORISED = 104;
        public static final int UPDATE_APPLIST = 105;
        public static final int UPDATE_USER_SETTING = 106;
        public static final int COMPLETE_ACTION = 107;

    }

    public class Response
    {
        //used as key to identify objects in bundle
        public static final String SERL1 = "serial1";
        public static final int LEFTTIME    = 1000;
        public static final int RECORDS     = 1001;
        public static final int GETACTION  = 1002;
        public static final int GETABOUTPROJECT = 1003;
    }

}
