package myblog.richard.vewe.libusersprovider;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 15-11-9.
 */
public class TwoUsers extends Users {
    private static TwoUsers ourInstance = new TwoUsers();
    public static TwoUsers getInstance() {
        return ourInstance;
    }

    private Context mContext;

    private Map<String, User> mUsers = new HashMap<String, User>();
    public void setContext(Context context)
    {
        mContext = context;
        mUsers.put(UsersContract.TableUsers.ROOT, new FixUser(UsersContract.TableUsers.ROOT, context));
        mUsers.put(UsersContract.TableUsers.DIGA, new FixUser(UsersContract.TableUsers.DIGA, context));
    }

    public Context getContext(){
        return mContext;
    }

    private UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private TwoUsers() {
        mMatcher.addURI(UsersContract.AUTHORITY, UsersContract.TableApplist.ROOT_PATH, UsersContract.TABLE_APPLIST_ROOT);
        mMatcher.addURI(UsersContract.AUTHORITY, UsersContract.TableApplist.DIGA_PATH, UsersContract.TABLE_APPLIST_DIGA);
    }

    @Override
    public UriMatcher getUriMatcher() {
        return mMatcher;
    }

    @Override
    public boolean isUserExist(String name) {
        if(name == null) return false;
        if(name.contentEquals(UsersContract.TableUsers.ROOT)) return true;
        if(name.contentEquals(UsersContract.TableUsers.DIGA)) return true;
        return false;
    }

    @Override
    public User getUser(String name) {
        return mUsers.get(name);
    }

    @Override
    public Cursor query(String[] projection, String selection) {
        /* supported query
        selection is null for all users
        selection is "name=[current/diga/root]"
        */
        //create columns for MatrixCursor
        MatrixCursor matrixCursor= new MatrixCursor(projection);
        //add rows for every user
        if(selection == null)
        {
            matrixCursor.addRow(mUsers.get(UsersContract.TableUsers.ROOT).buildRow(projection));
            matrixCursor.addRow(mUsers.get(UsersContract.TableUsers.DIGA).buildRow(projection));
        }
        else
        {
            String name;
            if(selection.contains("=current"))
            {
                name = Current.getInstance().getCurrentUser();
            }
            else if(selection.contains("=" + UsersContract.TableUsers.ROOT))
            {
                name = UsersContract.TableUsers.ROOT;
            }
            else if(selection.contains("=" + UsersContract.TableUsers.DIGA))
            {
                name = UsersContract.TableUsers.DIGA;
            }
            else
            {
                return matrixCursor;
            }
            matrixCursor.addRow(mUsers.get(name).buildRow(projection));
        }
        return matrixCursor;
    }

    @Override
    public int update(ContentValues values, String selection) {
        if(selection == null) {
            mUsers.get(UsersContract.TableUsers.DIGA).update(values);
            mUsers.get(UsersContract.TableUsers.ROOT).update(values);
            return 2;
        }
        else
        {
            String name;
            if(selection.contains("=current"))
            {
                name = Current.getInstance().getCurrentUser();
            }
            else if(selection.contains("=" + UsersContract.TableUsers.ROOT))
            {
                name = UsersContract.TableUsers.ROOT;
            }
            else if(selection.contains("=" + UsersContract.TableUsers.DIGA))
            {
                name = UsersContract.TableUsers.DIGA;
            }
            else
            {
                return 0;
            }
            mUsers.get(name).update(values);
            return 1;
        }
    }
}









