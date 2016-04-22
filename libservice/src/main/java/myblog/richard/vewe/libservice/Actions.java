package myblog.richard.vewe.libservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/**
 * Created by richard on 15-12-10.
 */
public class Actions {
    private static final String ACTIONS_PREF = "actions";
    private static final String tag = "actions";
    private static final boolean LOGD = true;

    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String URL = "url";
    private static final String STATUS = "status";

    private static final String DONE = "done";

    private SharedPreferences mPf;

    Actions(MyService service)
    {
        mPf = service.getSharedPreferences(ACTIONS_PREF, Context.MODE_PRIVATE);
    }

    String getCompletedActions()
    {
        Map<String, ?> map = mPf.getAll();
        String completed = "";

        for(Map.Entry<String, ?> entry : map.entrySet())
        {
            String str = entry.getValue().toString();
            if(str.contains(STATUS) && str.contains(DONE))
            {
                try{
                    JSONObject json = new JSONObject(str);
                    completed += json.getString(ID) + ",";

                }catch(Exception e){

                }
            }
        }
        return completed;
    }

    String getAction()
    {
        Map<String, ?> map = mPf.getAll();
        String action = null;

        for(Map.Entry<String, ?> entry : map.entrySet())
        {
            String str = entry.getValue().toString();
            if(str.contains(STATUS) && str.contains(DONE))
            {
                continue;
            }
            action = str;
        }
        return action;
    }

    void completeAction(int action)
    {
        String str = mPf.getString(Integer.toString(action), null);
        if(str == null) return;
        try {
            JSONObject jsonObject = new JSONObject(str);
            jsonObject.put(STATUS, DONE);
            SharedPreferences.Editor editor = mPf.edit();
            editor.putString(Integer.toString(action), jsonObject.toString());
            editor.apply();
        }catch (Exception e)
        {

        }
    }

    void addActions(String json)
    {
        if(LOGD){
            Log.d(tag, "add actions: " + json);
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            SharedPreferences.Editor editor = mPf.edit();
            JSONArray jsonArray = jsonObject.getJSONArray("actions");
            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject action = jsonArray.getJSONObject(i);
                Log.d(tag, "action: " + action.toString());
                int id = action.getInt(ID);
                editor.putString(Integer.toString(id), action.toString());
            }
            if(jsonArray.length() > 0) {
                editor.apply();
            }

        }catch (Exception e)
        {
            Log.e(tag, "error: ", e);
        }
    }

    void finishAction(int id)
    {

    }
}
