package myblog.richard.vewe.libservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-12-9.
 */
public class GetServer {
    private static final String PREF_NAME = "getserver";
    private static final String PREF_SERVER = "server";

    private static final String tag = "getserver";
    private static boolean LOGD = true;
    private MyService mService;
    private SharedPreferences mPref;

    private State mCurrentState;
    private InitState mInitState;
    private VerifyingState mVerifyingState;
    private ReportStatusState mReportStatusState;
    private ActionQueryState mActionQueryState;
    private WaitForNextDayState mWaitForNextDayState;
    private RequestQueue mQueue;

    private static final int DELAY_FIRST_CHECK_NETWORK = (1000 * 60);  //(60*1000) //1 minutes
    private static final int DELAY_CHECK_NETWORK = 1000*60;        //(60*1000) //1 minutes

    private static final String REQUEST = "/HelloAce.do";
    //Copy from server
    private static String GET_STATIC_INFO = "staticinfo";
    private static String POST_STATIC_INFO = "staticinfo";
    private static String POST_STATUS = "status";
    private static String POST_SUMMARY = "summary";
    private static String POST_PASSWORD = "password";
    private static String POST_ACTION = "action";
    private static String GET_SUMMARY = "summary";
    //Copy from server user
    public static final String ID = "id";
    public static final String FLAG = "flag";
    public static final String HOMERULE = "homerule";
    public static final String GAMERULE = "gamerule";
    public static final String INSTALLDATE = "installdate";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String PASSWORD = "mypassword";
    public static final String VERIFIED = "verified";
    public static final String BRAND = "brand";
    public static final String VERSION = "version";

    public static final String ACTIONSDONE = "actionsdone";
    //====================================================
    public static final String CLIENT_VERSION = "V01";

    private String mDeviceId;
    private String mAboutProject;
    private boolean mVerified;
    private String mServer;
    private Actions mActions;


    GetServer(MyService service)
    {
        mService = service;
        mPref = mService.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String pref = mPref.getString(PREF_SERVER, null);
        if(pref != null){
            UsersContract.setServerUrl(pref);
        }

        mInitState = new InitState();
        mVerifyingState = new VerifyingState();
        mReportStatusState = new ReportStatusState();
        mActionQueryState = new ActionQueryState();
        mWaitForNextDayState = new WaitForNextDayState();

        mCurrentState = mInitState;
        service.getmServiceHandler().postDelayed(mInitState, DELAY_FIRST_CHECK_NETWORK);

        mQueue = Volley.newRequestQueue(service);

        TelephonyManager tm = (TelephonyManager) service.getSystemService(Context.TELEPHONY_SERVICE);

        mDeviceId = Settings.Secure.getString(service.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if(tm.getDeviceId() != null) mDeviceId += ":" + tm.getDeviceId();

        mActions = new Actions(service);
    }

    public void handleMessage(Message msg)
    {
        Message resp;
        switch(msg.what)
        {
            case MessageType.Request.GETACTION_R:
                String action = mActions.getAction();
                if(action == null){
                    Log.d(tag, "do not get any action yet");
                    return;
                }
                resp = Message.obtain(null, MessageType.Response.GETACTION);
                try {
                    resp.obj = action;
                    msg.replyTo.send(resp);
                }catch(Exception e)
                {
                    Log.d(tag, "MyError", e);
                }

                break;
            case MessageType.Request.GETABOUTPROJECT_R:
                resp = Message.obtain(null, MessageType.Response.GETABOUTPROJECT);
                if(mAboutProject == null){
                    Log.d(tag, "about project did not get yet");
                    return;
                }

                try {
                    resp.obj = mAboutProject;
                    msg.replyTo.send(resp);
                }catch(Exception e)
                {
                    Log.d(tag, "MyError", e);
                }
                break;
            case MessageType.Request.COMPLETE_ACTION:
                mActions.completeAction(msg.arg1);
                break;
        }
    }

    interface State extends Runnable{
        public void handleMessage(Message msg);
    }

    class InitState implements Runnable, State{
        @Override
        public void handleMessage(Message msg)
        {
        }

        @Override
        public void run() {
            if(LOGD) {
                Log.d(tag, "check if network is available");
            }
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) mService.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
            {
                if(LOGD) {
                    Log.d(tag, "connect is available, go to next state");
                }
                mCurrentState = mVerifyingState;
                mCurrentState.run();
            }
            else
            {
                mService.getmServiceHandler().postDelayed(mInitState, DELAY_CHECK_NETWORK);
            }
        }
    }

    class VerifyingRequest extends StringRequest
    {
        VerifyingRequest(String url, Response.Listener<String> listener,
                         Response.ErrorListener errorListener)
        {
            super(Method.POST, url, listener, errorListener);
        }

        protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();
            params.put(ID, mDeviceId);
            return params;
        };

    }

    class VerifyingState implements State, Response.Listener<String>, Response.ErrorListener{
        private ArrayList<String> mServerList;
        private int mId;
        private int mTry;

        VerifyingState() {
            mServerList = new ArrayList<String>();
            //get Preferred one
            String pref = mPref.getString(PREF_SERVER, null);
            if(pref != null){
                mServerList.add(pref);
            }

            //this is the ACE in ali cloud
            mServerList.add("http://buccaneer.aliapp.com/");
            //this is as backup
            mServerList.add("http://www.seapapa.cn:8080/");
            mServerList.add("http://www.seapapa.com:8080/");
            mServerList.add("http://www.seapapa.com.cn:8080/");
            //this is the ECS server in Ali Cloud, only as a backup
            mServerList.add("http://120.24.5.162:8080/");
            mServerList.add("http://10.0.2.2:8080/");

        }

        @Override
        public void run() {
            if(mId >= mServerList.size()){
                if(mTry > 1){
                    if(LOGD) {
                        Log.d(tag, "terminate trying to connect the server");
                    }
                    mCurrentState = mWaitForNextDayState;
                    mCurrentState.run();
                    return;
                }
                mTry ++;
                mId = 0;
            }
            mServer = mServerList.get(mId) + REQUEST;
            String url = mServer + "?" + POST_STATIC_INFO;
            if(LOGD) {
                Log.d(tag, url);
            }
            VerifyingRequest req = new VerifyingRequest(url, this, this);
            req.setShouldCache(false);
            mQueue.add(req);
            mId ++;
        }

        public void handleMessage(Message msg)
        {

        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if(LOGD) {
                Log.d(tag, "error: " + error);
            }
            mService.getmServiceHandler().postDelayed(this, 1000);
        }

        @Override
        public void onResponse(String response) {
            if(response == null){
                //try to access it from another server
                mService.getmServiceHandler().postDelayed(this, 1000);
                return;
            }
            CRC32 crc = new CRC32();
            crc.update(mDeviceId.getBytes());
            String crcstr = Long.toHexString(crc.getValue());
            if(LOGD) {
                Log.d(tag, "crc should be: " + crcstr + " response: " + response);
            }

            boolean goon = false;

            try {
                JSONObject mainObject = new JSONObject(response);
                String version = mainObject.getString("version");
                if(LOGD) {
                    Log.d(tag, "version: " + version);
                }
                if(version.contains("VER1")){
                    goon = true;
                    if (version.contains(crcstr)) {
                        if (mId != 1) {
                            SharedPreferences.Editor edit = mPref.edit();
                            edit.putString(PREF_SERVER, mServerList.get(mId - 1));
                            edit.apply();
                        }
                        UsersContract.setServerUrl(mServerList.get(mId - 1));
                        mAboutProject = mainObject.getString("aboutproject");
                        mVerified = true;
                        if (LOGD) {
                            Log.d(tag, "verified done");
                        }
                    }
                }

            }catch(Exception e){
                Log.d(tag, "error: ", e);
            }

            if(goon) {
                mId = 0;
                //got to next state to report status
                mCurrentState = mReportStatusState;
                mCurrentState.run();
            }
            else
            {
                mService.getmServiceHandler().postDelayed(this, 1000);
            }
        }
    }

    class ReportStatusRequest extends StringRequest
    {
        ReportStatusRequest(String url, Response.Listener<String> listener,
                         Response.ErrorListener errorListener)
        {
            super(Method.POST, url, listener, errorListener);
        }

        protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();
            params.put(ID, mDeviceId);
            User u = mService.getmMainHandler().getmUser();
            if(u != null){
                Integer I = (Integer)u.getProperities().get(UsersContract.TableUsers.Column.FLAG);
                if(I != null) params.put(FLAG, I.toString());

                I = (Integer)u.getProperities().get(UsersContract.TableUsers.Column.HOMERULE);
                if(I != null) params.put(HOMERULE, I.toString());

                I = (Integer)u.getProperities().get(UsersContract.TableUsers.Column.GAMERULE);
                if(I != null) params.put(GAMERULE, I.toString());

                I = (Integer)u.getProperities().get(UsersContract.TableUsers.Column.INSTALL_DATE);
                if(I != null) params.put(INSTALLDATE, I.toString());

                String str = (String)u.getProperities().get(UsersContract.TableUsers.Column.EMAIL);
                if(str != null){
                    params.put(EMAIL, str);
                }

                str = (String)u.getProperities().get(UsersContract.TableUsers.Column.PHONE);
                if(str != null) {
                    params.put(PHONE, str);
                }

                params.put(BRAND, android.os.Build.MODEL + "-" + Build.MANUFACTURER);

                params.put(VERIFIED, ((Boolean)mVerified).toString());

                params.put(VERSION, CLIENT_VERSION);
            }
            return params;
        };

    }

    class ReportStatusState implements State, Response.Listener<String>, Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            if(mVerified) {
                mCurrentState = mActionQueryState;
            }
            else
            {
                mCurrentState = mWaitForNextDayState;
            }
            mCurrentState.run();
        }

        @Override
        public void run() {
            String url = mServer + "?" + POST_STATUS;

            if(LOGD){
                Log.d(tag, "new request: " + url);
            }
            ReportStatusRequest req = new ReportStatusRequest(url, this, this);
            req.setShouldCache(false);
            mQueue.add(req);
        }

        @Override
        public void onResponse(String response) {
            if(LOGD){
                Log.d(tag, "response: " + response);
            }
            if(mVerified) {
                mCurrentState = mActionQueryState;
            }
            else
            {
                mCurrentState = mWaitForNextDayState;
            }
            mCurrentState.run();
        }

        public void handleMessage(Message msg)
        {

        }
    }

    class ActionRequest extends StringRequest
    {
        ActionRequest(String url, Response.Listener<String> listener,
                         Response.ErrorListener errorListener)
        {
            super(Method.POST, url, listener, errorListener);
        }

        protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
            Map<String, String> params = new HashMap<String, String>();
            params.put(ID, mDeviceId);
            params.put(ACTIONSDONE, mActions.getCompletedActions());
            if(LOGD){
                Log.d(tag, "actionsdone: " + mActions.getCompletedActions());
            }
            return params;
        };

    }

    class ActionQueryState implements State, Response.Listener<String>, Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            mCurrentState = mWaitForNextDayState;
            mCurrentState.run();
        }

        @Override
        public void run() {
            String url = mServer + "?" + POST_ACTION;
            if(LOGD) {
                Log.d(tag, url);
            }
            ActionRequest req = new ActionRequest(url, this, this);
            req.setShouldCache(false);
            mQueue.add(req);
        }

        @Override
        public void onResponse(String response) {
            if(LOGD){
                Log.d(tag, "response: " + response);
            }
            mActions.addActions(response);
            mCurrentState = mWaitForNextDayState;
            mCurrentState.run();
        }

        public void handleMessage(Message msg)
        {

        }
    }

    class WaitForNextDayState implements State {

        @Override
        public void run() {
            if(LOGD) {
                Log.d(tag, "wait for next to report again");
            }
            mCurrentState = mInitState;
            mService.getmServiceHandler().postDelayed(mInitState, ((12*60*60)*1000));
        }

        public void handleMessage(Message msg)
        {

        }
    }

}



/* Get Example using volley
        RequestQueue queue = Volley.newRequestQueue(service);
        String url ="http://10.0.2.2:8080/HelloAce.do?staticinfo";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(tag, "response: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(tag, "error: " + error);
            }
        });
        queue.add(stringRequest);
 */