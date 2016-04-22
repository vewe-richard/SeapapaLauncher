package myblog.richard.vewe.libusersprovider;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;

/**
 * Created by richard on 15-11-9.
 */
public abstract class Users extends ProviderOpts{
    public abstract UriMatcher getUriMatcher();
    public abstract boolean isUserExist(String name);

    public abstract User getUser(String name);
}
