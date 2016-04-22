package myblog.richard.vewe.libusersprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class UsersProvider extends ContentProvider {
    Context mContext;
    private UriMatcher mUriMatcher;
    private TwoUsers mUsers;
    private Current mCurrent;
    private Applists mApplists;
    private Rules mRules;

    private static final String tag = "provider";
    private static final boolean LOGD = true;

    public UsersProvider() {
        mUriMatcher = new UriMatcher(mUriMatcher.NO_MATCH);
        mUriMatcher.addURI(UsersContract.AUTHORITY, UsersContract.TableUsers.USERS_PATH, UsersContract.TABLE_USERS);
        mUriMatcher.addURI(UsersContract.AUTHORITY, UsersContract.TableCurrent.CURRENT_PATH, UsersContract.TABLE_CURRENT);
        mUriMatcher.addURI(UsersContract.AUTHORITY, UsersContract.TableRule.RULE_PATH, UsersContract.TABLE_RULE);
    }

    private int uriToTable(Uri uri)
    {
        int match = mUriMatcher.match(uri);
        if(match != -1)
        {
            return match;
        }
        //ask a matcher from users, and check if it's a applist table
        match = mUsers.getUriMatcher().match(uri);
        return match;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch(uriToTable(uri))
        {
            case UsersContract.TABLE_USERS:
                throw new UnsupportedOperationException("Not supported");
            case UsersContract.TABLE_CURRENT:
                throw new UnsupportedOperationException("Not supported");
            case UsersContract.TABLE_RULE:
                throw new UnsupportedOperationException("Not supported");
            case UsersContract.TABLE_APPLIST_DIGA:
                return mApplists.getUserApplist(UsersContract.TableUsers.DIGA).delete(selection);
            case UsersContract.TABLE_APPLIST_ROOT:
                return mApplists.getUserApplist(UsersContract.TableUsers.ROOT).delete(selection);
            default:
                throw new IllegalArgumentException("invalid uri " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch(uriToTable(uri))
        {
            case UsersContract.TABLE_USERS:
                throw new UnsupportedOperationException("Not supported");
            case UsersContract.TABLE_CURRENT:
                throw new UnsupportedOperationException("Not supported");
            case UsersContract.TABLE_RULE:
                return mRules.insert(values);
            case UsersContract.TABLE_APPLIST_DIGA:
                return mApplists.getUserApplist(UsersContract.TableUsers.DIGA).insert(values);
            case UsersContract.TABLE_APPLIST_ROOT:
                return mApplists.getUserApplist(UsersContract.TableUsers.ROOT).insert(values);
            default:
                throw new IllegalArgumentException("invalid uri " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mUsers = TwoUsers.getInstance();
        mUsers.setContext(mContext);
        mCurrent = Current.getInstance();
        mCurrent.setContext(mContext, mUsers);
        mApplists = Applists.getInstance();
        mApplists.setContext(mContext, mUsers);
        mRules = Rules.getInstance();
        mRules.setContext(mContext);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if(LOGD){
            Log.d(tag, "query " + uri);
        }
        switch(uriToTable(uri))
        {
            case UsersContract.TABLE_USERS:
                return mUsers.query(projection, selection);
            case UsersContract.TABLE_CURRENT:
                return mCurrent.query(projection, selection);
            case UsersContract.TABLE_RULE:
                return mRules.query(projection, selection);
            case UsersContract.TABLE_APPLIST_DIGA:
                return mApplists.getUserApplist(UsersContract.TableUsers.DIGA).query(projection, selection);
            case UsersContract.TABLE_APPLIST_ROOT:
                return mApplists.getUserApplist(UsersContract.TableUsers.ROOT).query(projection, selection);
            default:
                throw new IllegalArgumentException("invalid uri " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch(uriToTable(uri))
        {
            case UsersContract.TABLE_USERS:
                return mUsers.update(values, selection);
            case UsersContract.TABLE_CURRENT:
                return mCurrent.update(values, selection);
            case UsersContract.TABLE_RULE:
                throw new UnsupportedOperationException("Not supported");
            case UsersContract.TABLE_APPLIST_DIGA:
                return mApplists.getUserApplist(UsersContract.TableUsers.DIGA).update(values, selection);
            case UsersContract.TABLE_APPLIST_ROOT:
                return mApplists.getUserApplist(UsersContract.TableUsers.ROOT).update(values, selection);
            default:
                throw new IllegalArgumentException("invalid uri " + uri);
        }
    }
}
