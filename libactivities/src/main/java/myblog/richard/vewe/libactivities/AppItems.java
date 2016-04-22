package myblog.richard.vewe.libactivities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myblog.richard.vewe.libusersprovider.App;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-13.
 */
public class AppItems {
    private Context mContext;
    private PackageManager mManager;
    private List<AppItem> mItems;
    private static final String tag = "appitems";
    private static boolean LOGD = true;
    ContentResolver mResolver;

    public AppItems(Context context)
    {
        mContext = context;
        mManager = mContext.getPackageManager();
        mItems = new ArrayList<AppItem>();
        mResolver = mContext.getContentResolver();
        loadApps();
    }

    private void loadApps()
    {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = mManager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppItem ai = new AppItem(ri.loadLabel(mManager).toString(), ri.activityInfo.packageName,
                    UsersContract.TableApplist.FORBIDDEN);
            /*
            if(LOGD) {
                Log.d(tag, "package name: " + ri.activityInfo.packageName);
            }*/
            mItems.add(ai);
        }
        //Merge from content provider
        Cursor cursor = mResolver.query(
                UsersContract.TableApplist.DIGA_URI,
                new String[] {UsersContract.TableApplist.Column.PKG, UsersContract.TableApplist.Column.TYPE},
                null,
                null,
                null);
        if(cursor == null)
        {
            Log.e(tag, "could not query all from table applist diga");
            return;
        }
        else if(cursor.getCount() < 1)
        {
            Log.w(tag, "failed to get any entries from table applist diga");
            return;
        }

        List<AppItem> latestItems = mItems;
        mItems = new LinkedList<AppItem>();

        for(AppItem ai : latestItems)
        {
            cursor.moveToPosition(-1);
            while(cursor.moveToNext()){
                App app = new App(cursor);

                if(ai.getPkg().contentEquals(app.getPkgName()))
                {
                    ai.setType(app.getPkgType());
                    mItems.add(0, ai);
                    break;
                }
            }
            if(cursor.isAfterLast())
            {
                mItems.add(ai);
            }
        }
        cursor.close();
    }

    public List<AppItem> getmItems() {
        return mItems;
    }

    public void changeItemType(AppItem ai, int type)
    {
        if(ai.getType() == type) return;
        if(LOGD){
            Log.d(tag, "set item type: " + ai.getLabel() + " prev type: " + ai.getType() + " now: " + type);
        }
        ai.setType(type);
        //for forbidden type, just clear it in content provider
        //for other type, just insert it, it will override current setting
        /*if(type == UsersContract.TableApplist.FORBIDDEN){
            String selection = UsersContract.TableApplist.Column.PKG + "=" + ai.getPkg();
            mResolver.delete(
                    UsersContract.TableApplist.DIGA_URI,
                    selection,
                    null
            );
        }
        else{   //insert it*/
            ContentValues values = new ContentValues();
            values.put(UsersContract.TableApplist.Column.PKG, ai.getPkg());
            values.put(UsersContract.TableApplist.Column.TYPE, type);

            mResolver.insert(UsersContract.TableApplist.DIGA_URI, values);
        //}
    }

    public PackageManager getmManager() {
        return mManager;
    }
}
