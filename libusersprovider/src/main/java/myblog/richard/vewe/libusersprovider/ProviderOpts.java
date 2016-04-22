package myblog.richard.vewe.libusersprovider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by richard on 15-11-10.
 */
public class ProviderOpts {
    public int delete(String selection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Uri insert(ContentValues values)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Cursor query(String[] projection, String selection) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int update(ContentValues values, String selection)
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
