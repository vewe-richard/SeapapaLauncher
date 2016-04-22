package myblog.richard.vewe.libusersprovider;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import myblog.richard.vewe.libusersprovider.PackageType;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-11-29.
 */
public class GroupingApps {
    private static final String[] mHomeMatcher = new String[]{
            "QQ",
            "微信",
            "阅读",
    };

    private static final String[] mGameMatcher = new String[]{
            "爱奇艺视频",
            "部落冲突",
            "浏览器",
    };

    private static final String[] mFreeMatcher = new String[]{
            "便签",
            "备忘",
            "信息",
            "图库",
            "天气",
            "录音机",
            "输入法",
            "日历",
            "时钟",
            "电话",
            "拨号",
            "画板",
            "地图",
            "相机",
            "短信",
            "联系人",
            "通讯录",
            "邮件",
            "音乐",
    };

    public static int getAppType(CharSequence label)
    {
        for(String str : mHomeMatcher)
        {
            if(str.contentEquals(label))
            {
                return UsersContract.TableApplist.HOME;
            }
        }
        for(String str : mFreeMatcher)
        {
            if(str.contentEquals(label))
            {
                return UsersContract.TableApplist.FREE;
            }
        }
        for(String str : mGameMatcher)
        {
            if(str.contentEquals(label))
            {
                return UsersContract.TableApplist.GAME;
            }
        }
        return UsersContract.TableApplist.FORBIDDEN;
    }

    private PackageManager mPM;
    private List<String>    mListGame;
    private List<String>    mListHome;
    private List<String>    mListFree;
    private List<String>    mListForbidden;

    public GroupingApps(PackageManager pm)
    {
        mPM = pm;
        mListFree = new ArrayList<String>();
        mListHome = new ArrayList<String>();
        mListGame = new ArrayList<String>();
        mListForbidden = new ArrayList<String>();
        grouping();
    }

    public GroupingApps(PackageManager pm, boolean unused)
    {
        mPM = pm;
        mListFree = new ArrayList<String>();
        mListHome = new ArrayList<String>();
        mListGame = new ArrayList<String>();
        mListForbidden = new ArrayList<String>();

        for(ResolveInfo ri : PackageType.getAvailableActivities()){
            String pkg = ri.activityInfo.packageName;
            CharSequence label = ri.loadLabel(mPM);

            /*
            try {
                ApplicationInfo ai = mPM.getApplicationInfo(ri.activityInfo.packageName, 0);
                if((ai.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME)
                {
                    mListGame.add(pkg);
                    continue;
                }
            }catch(Exception e)
            {

            }*/

            int type = GroupingApps.getAppType(label);
            switch(type){
                case UsersContract.TableApplist.GAME:
                    mListGame.add(pkg);
                    break;
                case UsersContract.TableApplist.HOME:
                    mListHome.add(pkg);
                    break;
                case UsersContract.TableApplist.FREE:
                    mListFree.add(pkg);
                    break;
                default:
                    mListForbidden.add(pkg);
                    break;
            }
        }
        String pkg = "org.fdroid.fdroid";
        mListForbidden.remove(pkg);
        mListFree.add(pkg);
        pkg = "myblog.richard.vewe.fdroid";
        mListForbidden.remove(pkg);
        mListFree.add(pkg);
    }

    private void grouping()
    {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = mPM.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities) {
            String pkg = ri.activityInfo.packageName;
            CharSequence label = ri.loadLabel(mPM);

            try {
                ApplicationInfo ai = mPM.getApplicationInfo(ri.activityInfo.packageName, 0);
                if((ai.flags & ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME)
                {
                    mListGame.add(pkg);
                    continue;
                }
            }catch(Exception e)
            {

            }

            int type = GroupingApps.getAppType(label);
            switch(type){
                case UsersContract.TableApplist.GAME:
                    mListGame.add(pkg);
                    break;
                case UsersContract.TableApplist.HOME:
                    mListHome.add(pkg);
                    break;
                case UsersContract.TableApplist.FREE:
                    mListFree.add(pkg);
                    break;
            }
        }
    }

    public List<String> getmListGame() {
        return mListGame;
    }

    public List<String> getmListHome() {
        return mListHome;
    }

    public List<String> getmListFree() {
        return mListFree;
    }

    public List<String> getmListForbidden() {
        return mListForbidden;
    }
}
