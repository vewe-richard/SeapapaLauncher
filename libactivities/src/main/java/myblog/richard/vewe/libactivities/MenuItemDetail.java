package myblog.richard.vewe.libactivities;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by richard on 15-11-14.
 */
public class MenuItemDetail {
    private String mName;
    private Drawable mIcon;
    private int mId;

    public static final int INVALID_RESOURCE_ID = -1;

    public MenuItemDetail(Context context, int name, int icon, int id)
    {
        if(name != INVALID_RESOURCE_ID) this.mName = context.getString(name);
        if(icon != INVALID_RESOURCE_ID) this.mIcon = context.getResources().getDrawable(icon);
        this.mId = id;
    }

    public MenuItemDetail(String mName, Drawable mIcon, int mId) {
        this.mName = mName;
        this.mIcon = mIcon;
        this.mId = mId;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public Drawable getmIcon() {
        return mIcon;
    }

    public void setmIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }
}
