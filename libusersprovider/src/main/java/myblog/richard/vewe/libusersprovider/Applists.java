package myblog.richard.vewe.libusersprovider;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by richard on 15-11-10.
 */
public class Applists {
    private static Applists ourInstance = new Applists();
    private ArrayList<Applist> mLists = new ArrayList<Applist>();

    public static Applists getInstance() {
        return ourInstance;
    }

    private Applists() {
    }

    private Context mContext;
    private Users mUsers;
    public void setContext(Context context, Users users)
    {
        mContext = context;
        mUsers = users;
    }

    public User getUser(String name)
    {
        return mUsers.getUser(name);
    }

    public Context getContext() {
        return mContext;
    }

    //different user different applist
    public Applist getUserApplist(String user)
    {
        for(Applist al : mLists)
        {
            if(al.userName().contentEquals(user))
            {
                return al;
            }
        }
        Applist newAl = new Applist(user, mContext);
        mLists.add(newAl);
        return newAl;
    }
}
