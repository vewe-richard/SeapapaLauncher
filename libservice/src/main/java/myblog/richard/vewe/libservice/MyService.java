package myblog.richard.vewe.libservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class MyService extends Service {
    private static final String tag = "service";
    private static final boolean LOGD = true;

    private volatile Looper mServiceLooper;
    private volatile IncomingHandler mServiceHandler;
    private Messenger mMessenger;

    private MainHandler mMainHandler;


    public IncomingHandler getmServiceHandler() {
        return mServiceHandler;
    }

    public MainHandler getmMainHandler() {
        return mMainHandler;
    }

    //Start & Stop
    public static void startService(Context context) {
        Intent intent = new Intent(context, MyService.class);
        context.startService(intent);
    }

    public static void stopService(Context context)
    {
        Intent intent = new Intent(context, MyService.class);
        context.stopService(intent);
    }

    public MyService() {
        super();
    }

    //Handler
    class IncomingHandler extends Handler {
        public IncomingHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                default:
                    mMainHandler.handleMessage(msg);
                    break;
            }

        }
    }

    @Override
    public void onCreate() {
        if(LOGD) {
            Log.d(tag, "onCreate");
        }
        super.onCreate();

        HandlerThread thread = new HandlerThread("MyBlogService");
        thread.start();

        mServiceLooper = thread.getLooper();
        mMainHandler = new MainHandler(this);
        mServiceHandler = new IncomingHandler(mServiceLooper);
        mMessenger = new Messenger(mServiceHandler);
    }

    @Override
    public void onDestroy() {
        if(LOGD) {
            Log.d(tag, "onDestroy");
        }
        mServiceLooper.quit();
        mMainHandler.destroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(LOGD) {
            Log.d(tag, "onStartCommand");
        }
        //send message to start app recording
        //send message to start position recording
        //send message to start network???
        //ues Message msg = mServiceHandler.obtainMessage();
        //mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(LOGD) {
            Log.d(tag, "onBind");
        }
        return mMessenger.getBinder();
    }
}
