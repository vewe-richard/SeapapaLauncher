package myblog.richard.vewe.libservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;


/**
 * Created by richard on 15-10-15.
 */
public class RemoteMyService extends Handler implements ServiceConnection {
    private static final String tag = "remote";
    private static final boolean LOGD = true;

    private Messenger mService;
    private boolean mBound;
    private Context mContext;

    private final Messenger mResponse = new Messenger(this);

    public RemoteMyService(Context context)
    {
        mContext = context;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = new Messenger(service);
        mBound = true;
        if(LOGD) {
            Log.d(tag, "onServiceConnected");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
        mBound = false;
        if(LOGD) {
            Log.d(tag, "onServiceDisconnected");
        }
    }

    public boolean isBound()
    {
        return mBound;
    }

    public void bind()
    {
        Intent i = new Intent(mContext, myblog.richard.vewe.libservice.MyService.class);
        mContext.bindService(i, this, Context.BIND_AUTO_CREATE);
    }

    public void unbind()
    {
        if(mBound)
        {
            mContext.unbindService(this);
            mBound = false;
        }
    }

    public void sendMessageWithResp(int msgId)
    {
        if(!mBound) return;

        Message msg = Message.obtain(null, msgId, 0, 0);
        msg.replyTo = mResponse;
        try{
            mService.send(msg);
        }
        catch(Exception e){
            Log.e(tag, "My Error", e);
        }
    }

    public void sendMessageWithResp(int msgId, int arg1, int arg2, Object obj)
    {
        if(!mBound) return;

        Message msg = Message.obtain(null, msgId, arg1, arg2, obj);
        msg.replyTo = mResponse;
        try{
            mService.send(msg);
        }
        catch(Exception e){
            Log.e(tag, "My Error", e);
        }
    }

    public void sendMessage(int msgId)
    {
        if(!mBound) return;

        Message msg = Message.obtain(null, msgId, 0, 0);
        try{
            mService.send(msg);
        }
        catch(Exception e){
            Log.e(tag, "My Error", e);
        }
    }

    public void sendMessage(int msgId, int arg1, int arg2, Object obj)
    {
        if(!mBound) return;

        Message msg = Message.obtain(null, msgId, arg1, arg2, obj);
        try{
            mService.send(msg);
        }
        catch(Exception e){
            Log.e(tag, "My Error", e);
        }
    }

    private void sendMessage(int msgId, int arg1, int arg2, Object obj, Bundle b)
    {
        if(!mBound) return;

        Message msg = Message.obtain(null, msgId, arg1, arg2, obj);
        msg.setData(b);
        try{
            mService.send(msg);
        }
        catch(Exception e){
            Log.e(tag, "My Error", e);
        }
    }

    //obj:username
    public void su(String name)
    {
        Bundle b = new Bundle();
        b.putString(MessageType.Request.SERL1, name);
        sendMessage(MessageType.Request.SU, 0, 0, null, b);
    }

    public void logout()
    {
        sendMessage(MessageType.Request.LOGOUT);
    }

    public void lefttimeR()
    {
        sendMessageWithResp(MessageType.Request.LEFTTIME_R);
    }

    //arg1: date
    public void recordsR(int date)
    {
        sendMessageWithResp(MessageType.Request.RECORDS_R, date, 0, null);
    }

    //arg1: time added
    public void addtime(int time)
    {
        sendMessage(MessageType.Request.ADDTIME, time, 0, null);
    }

    public void setAuthorised(String pkg)
    {
        Bundle b = new Bundle();
        b.putString(MessageType.Request.SERL1, pkg);

        sendMessage(MessageType.Request.SET_AUTHORISED, 0, 0, null, b);
    }

    public void clearAuthorised(String pkg)
    {
        Bundle b = new Bundle();
        b.putString(MessageType.Request.SERL1, pkg);

        sendMessage(MessageType.Request.CLEAR_AUTHORISED, 0, 0, null, b);
    }

    public void updateUserSetting()
    {
        sendMessage(MessageType.Request.UPDATE_USER_SETTING);
    }

    public void updateApplist()
    {
        sendMessage(MessageType.Request.UPDATE_APPLIST);
    }

    public void getAction(){
        sendMessageWithResp(MessageType.Request.GETACTION_R);
    }

    public void getAboutProject(){
        sendMessageWithResp(MessageType.Request.GETABOUTPROJECT_R);
    }

    public void completeAction(int action){
        sendMessage(MessageType.Request.COMPLETE_ACTION, action, 0, null);
    }
}
