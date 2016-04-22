package  myblog.richard.vewe.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import myblog.richard.vewe.libservice.MyService;

public class StartupReceiver extends BroadcastReceiver {

    static final String SYSTEM_READY = "myblog.richard.vewe.launcher3.SYSTEM_READY";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.d("startupreceiver", "myservice is started");
        context.sendStickyBroadcast(new Intent(SYSTEM_READY));
        MyService.startService(context);
    }
}
