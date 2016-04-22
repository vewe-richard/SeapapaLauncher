package myblog.richard.vewe.libactivities;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by richard on 15-12-15.
 */
public class Action implements Serializable {
    private int id;
    private String type;
    private String url;
    private boolean isValid;
    private int times;
    private int run_times;

    public static final int DEFAULT_TIMES = 5;

    private static final long serialVersionUID = 1L;

    public Action(String jsonstr){
        try{
            JSONObject json = new JSONObject(jsonstr);

            try {
                id = Integer.parseInt(json.getString("id"));
                type = json.getString("type");
                url = json.getString("url");
                times = DEFAULT_TIMES;
                if(type.contentEquals("PromptOnResume")) {
                    isValid = true;
                }
                times = json.getInt("times");
            }catch(Exception e){

            }
        }catch(Exception e){

        }
    }

    public boolean complete()
    {
        if(run_times >= times){
            return true;
        }
        return false;
    }

    public void doIt()
    {
        run_times ++;
    }

    public boolean isValid() {
        return isValid;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Action{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", isValid=" + isValid +
                ", times=" + times +
                ", run_times=" + run_times +
                '}';
    }
}
