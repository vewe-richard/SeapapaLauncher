package myblog.richard.vewe.libusersprovider;

import android.database.Cursor;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 15-11-11.
 */
public class Rule {
    private static final String tag = "rule";
    private static final boolean LOGD = true;
    private Map<String, Object> mElements = new HashMap<String, Object>();

    @Override
    public String toString() {
        return "Rule{" +
                "mElements=" + mElements +
                '}';
    }

    public Rule(Cursor cursor)
    {
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

}
