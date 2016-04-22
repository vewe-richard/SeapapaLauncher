package myblog.richard.vewe.libusersprovider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richard on 15-12-1.
 */
public class PackageType {
    private static Context mContext;
    private static PackageManager mPM;
    private static List<String> mLaunchables = new ArrayList<String>();
    private static List<ResolveInfo> availableActivities;

    public static void setContext(Context context)
    {
        mContext = context;
        mPM = mContext.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        availableActivities = mPM.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities) {
            mLaunchables.add(ri.activityInfo.packageName);
        }
    }

    public static List<ResolveInfo> getAvailableActivities() {
        return availableActivities;
    }

    private static String[] mForbiddenList = new String[] {"com.android.packageinstaller"};

    private static int mInstallerEnable;
    public static void installerEnable(int enable){
        Log.d("installer", "enable installer " + enable);
        mInstallerEnable = enable;

        mApplistMayChanged = true;
    }

    private static boolean mApplistMayChanged;
    public static boolean getApplistMayChanged()
    {
        return mApplistMayChanged;
    }

    public static void setApplistMayChanged(boolean changed)
    {
        mApplistMayChanged = changed;
    }

    public static int getType(String pkg, List<App> applist)
    {
        int type;

        if(pkg.contains("com.android.packageinstaller")){
            if(mInstallerEnable == 0) return UsersContract.TableApplist.FORBIDDEN;
            else
            {
//                Log.d("installer", "installer is free");
                return UsersContract.TableApplist.FREE;
            }
        }


        if(applist != null) {
            for (App app : applist) {
                if (app.getPkgName().contentEquals(pkg)) {
                    type = app.getPkgType();
                    return type;
                }
            }
        }
        return getType(pkg);
    }

    public static int getType(String pkg)
    {
        int type = UsersContract.TableApplist.FREE;

        //if this package is in launcheable list, then forbidden it default
        for(String str : mLaunchables)
        {
            if(str.contentEquals(pkg))
                return UsersContract.TableApplist.FORBIDDEN;
        }

        //get it intent, if it's launcherable, forbidden it, such as new installed applications
        if(mPM == null) return type;
        Intent i = mPM.getLaunchIntentForPackage(pkg);
        //it should be system packages, let forbidden some system packages, such as install/uninstall
        if(i == null){
            for(String str : mForbiddenList)
            {
                if(str.contentEquals(pkg)) return UsersContract.TableApplist.FORBIDDEN;
            }
            return type;
        }
        else
        {
            if(i.getAction().contains("MAIN") && i.getCategories().contains("android.intent.category.LAUNCHER"))
            {
                return UsersContract.TableApplist.FORBIDDEN;
            }
        }
        return type;
    }
}
