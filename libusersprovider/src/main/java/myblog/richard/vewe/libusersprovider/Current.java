package myblog.richard.vewe.libusersprovider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;

/**
 * Created by richard on 15-11-10.
 */
public class Current {
    private static Current ourInstance = new Current();
    private static final String PREFERENCE_NAME = "CURRENT";

    public static Current getInstance() {
        return ourInstance;
    }

    private Context mContext;
    private SharedPreferences mPref;
    private String mCurrentUser;
    private Users mUsers;
    private User mCurrentUserObject;
    private Integer mInstallerEnable = 0;

    private static final int DEFAULT_HINT_STRATEGY =  UsersContract.TableCurrent.HINT_LOCK_AFTER;

    public void setContext(Context context, Users users)
    {
        mContext = context;
        mPref = mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        //default set to diga, boot and set to diga
        mCurrentUser = UsersContract.TableUsers.DIGA;
        mUsers = users;
        mCurrentUserObject = mUsers.getUser(mCurrentUser);
    }

    private Current() {
    }

    public synchronized String getCurrentUser()
    {
        return mCurrentUser;
    }

    public User getmCurrentUserObject()
    {
        return mCurrentUserObject;
    }

    public synchronized Cursor query(String[] projection, String selection)
    {
        //I don't care projection and selection, current only have one cursor
        MatrixCursor matrixCursor= new MatrixCursor(new String[]{UsersContract.TableCurrent.Column.KEY, UsersContract.TableCurrent.Column.VAL});
        if(selection == null) {
            matrixCursor.addRow(new Object[]{UsersContract.TableCurrent.KEY_CURRENT, mCurrentUser});
            int hint = mPref.getInt(UsersContract.TableCurrent.KEY_HINT_STRATAGE, DEFAULT_HINT_STRATEGY);
            matrixCursor.addRow(new Object[]{UsersContract.TableCurrent.KEY_HINT_STRATAGE, hint});
        }
        else
        {
            //
            if(selection.contains("=" + UsersContract.TableCurrent.KEY_CURRENT))
            {
                matrixCursor.addRow(new Object[]{UsersContract.TableCurrent.KEY_CURRENT, mCurrentUser});
            }
            else if(selection.contains("=" + UsersContract.TableCurrent.KEY_HINT_STRATAGE))
            {
                int hint = mPref.getInt(UsersContract.TableCurrent.KEY_HINT_STRATAGE, DEFAULT_HINT_STRATEGY);
                matrixCursor.addRow(new Object[]{UsersContract.TableCurrent.KEY_HINT_STRATAGE, hint});
            }
            else if(selection.contains("=" + UsersContract.TableCurrent.KEY_INSTALLER))
            {
                matrixCursor.addRow(new Object[]{UsersContract.TableCurrent.KEY_INSTALLER, mInstallerEnable});
            }

        }
        return matrixCursor;
    }

    public synchronized int update(ContentValues values, String selection) {
        //we assure the selection like: "KEY=CURRENT"
        if(selection == null) return 0;
        if(selection.contains("=" + UsersContract.TableCurrent.KEY_CURRENT)) {
            String user = values.getAsString(UsersContract.TableCurrent.Column.VAL);
            if (user == null) return 0;
            //Need check if user exist???
            if (!mUsers.isUserExist(user)) return 0;
            mCurrentUser = user;
        }
        else if(selection.contains("=" + UsersContract.TableCurrent.KEY_HINT_STRATAGE)){
            Integer hint = values.getAsInteger(UsersContract.TableCurrent.Column.VAL);
            if(hint == null) return 0;
            SharedPreferences.Editor edit = mPref.edit();
            edit.putInt(UsersContract.TableCurrent.KEY_HINT_STRATAGE, hint);
            edit.apply();
        }
        else if(selection.contains("=" + UsersContract.TableCurrent.KEY_INSTALLER)){
            mInstallerEnable = values.getAsInteger(UsersContract.TableCurrent.Column.VAL);
            if(mInstallerEnable == null) mInstallerEnable = 0;
            PackageType.installerEnable(mInstallerEnable);
        }
        return 1;
    }

    public int getHintStrategy()
    {
        return mPref.getInt(UsersContract.TableCurrent.KEY_HINT_STRATAGE, DEFAULT_HINT_STRATEGY);
    }
}
