package myblog.richard.vewe.libusersprovider;

import android.database.Cursor;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 15-11-9.
 */
public class App {
    private static final String tag = "app";
    private static final boolean LOGD = true;
    private Map<String, Object> mElements = new HashMap<String, Object>();

    App()
    {

    }

    public String getPkgName()
    {
        return (String)mElements.get(UsersContract.TableApplist.Column.PKG);
    }

    public int getPkgType()
    {
        return (int)mElements.get(UsersContract.TableApplist.Column.TYPE);
    }

    public boolean isLocked()
    {
        String lock = (String)mElements.get(UsersContract.TableApplist.Column.LOCK);
        return lock.contentEquals("true");
    }

    public String getReason()
    {
        return (String)mElements.get(UsersContract.TableApplist.Column.REASON);
    }

    public String getHint()
    {
        return (String)mElements.get(UsersContract.TableApplist.Column.HINT);
    }

    public String getPrefix() {
        return (String)mElements.get(UsersContract.TableApplist.Column.PREFIX);
    }

    public App(Cursor cursor)
    {
        this();

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

    @Override
    public String toString() {
        return "App{" +
                "mElements=" + mElements +
                '}';
    }
}
