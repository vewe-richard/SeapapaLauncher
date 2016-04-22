package  myblog.richard.vewe.launcher3;

import android.content.ComponentName;
import android.text.TextUtils;
import android.util.Log;

public abstract class AppFilter {

    private static final boolean DBG = false;
    private static final String TAG = "AppFilter";

    public abstract boolean shouldShowApp(ComponentName app);

    public static AppFilter loadByName(String className) {
        if (TextUtils.isEmpty(className)) return MyAppFilter.getInstance();
        if (DBG) Log.d(TAG, "Loading AppFilter: " + className);
        try {
            Class<?> cls = Class.forName(className);
            return (AppFilter) cls.newInstance();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Bad AppFilter class", e);
            return null;
        } catch (InstantiationException e) {
            Log.e(TAG, "Bad AppFilter class", e);
            return null;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Bad AppFilter class", e);
            return null;
        } catch (ClassCastException e) {
            Log.e(TAG, "Bad AppFilter class", e);
            return null;
        }
    }

}

class MyAppFilter extends AppFilter {
    private static final String tag = "myappfilter";
    static MyAppFilter getInstance(){
        return mInstance;
    }

    LauncherApplication mApplication;

    private static MyAppFilter mInstance = new MyAppFilter();

    public void setApplication(LauncherApplication app){
        mApplication = app;
    }

    @Override
    public boolean shouldShowApp(ComponentName app) {
        return mApplication.shouldShowApp(app.getPackageName());
    }
}