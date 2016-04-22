package myblog.richard.vewe.test.provider;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;

import myblog.richard.vewe.libservice.MessageType;
import myblog.richard.vewe.libservice.MyService;
import myblog.richard.vewe.libservice.RemoteMyService;
import myblog.richard.vewe.test.R;

public class TestUIActivity extends myblog.richard.vewe.libcommon.test.TestActivity {
    private static final String tag = "test";

    static String[] sButtonNames = {
            "ParentSetting","testprovider","appselected",  //row0
            "visitorSetting", "startservice", "stopservice",
            "bindservice", "unbind", "su",
            "logout", "lefttime", "records",     //row3
            "addtime", "set authorised", "clear authorised ",
            "update User", "update applist", "event records"

    };

    @Override
    protected String getButtonName(int row, int button)
    {
        int index = row * 3 + button;
        //Log.d("test", "index " + index + " length" + sButtonNames.length);
        if(index < sButtonNames.length){
            return sButtonNames[index];
        }
        return super.getButtonName(row, button);
    }

    @Override
    protected void onClickRow0(int button) {
        if(button == 0)
        {
        }
        else if(button == 1)
        {
            Intent i = new Intent(this, myblog.richard.vewe.test.provider.TestProviderActivity.class);
            startActivity(i);
        }
        else
        {
            Intent i = new Intent(this, myblog.richard.vewe.libactivities.SelectProtectAppsActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onClickRow1(int button) {
        if(button == 0)
        {
            Intent i = new Intent(this, myblog.richard.vewe.libactivities.VisitorSettingActivity.class);
            startActivity(i);
        }
        else if(button == 1)
        {
            MyService.startService(this);
        }
        else
        {
            MyService.stopService(this);
        }
    }

    private RemoteMyService mService = new RemoteMyService(this){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case MessageType.Response.LEFTTIME:
                    Log.d(tag, "response lefttime " + msg.arg1);
                    break;
                default:
                    Log.d(tag, "get response " + msg.what);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.unbind();
    }

    @Override
    protected void onClickRow2(int button) {
        if(button == 0)//bind to service
        {
            mService.bind();
        }
        else if(button == 1)
        {
            mService.unbind();
        }
        else        //su
        {
            mService.su("diga");
        }
    }

    @Override
    protected void onClickRow3(int button) {
        if(button == 0) //logout
        {
            mService.logout();
        }
        else if(button == 1)    //lefttime
        {
            mService.lefttimeR();
        }
        else        //records
        {
            mService.recordsR(100);
        }
    }

    @Override
    protected void onClickRow4(int button) {
        if(button == 0) //addtime
        {
            mService.addtime(600);
        }
        else if(button == 1)    //set authorised
        {
            mService.setAuthorised("com.android.contacts");
        }
        else    //clear authorised
        {
            mService.clearAuthorised(null);
        }
    }

    @Override
    protected void onClickRow5(int button) {
        if(button == 0) //
        {
            mService.updateUserSetting();
        }
        else if(button == 1){
            mService.updateApplist();
        }
        else
        {
            Intent i = new Intent(this, myblog.richard.vewe.libactivities.AppRecordsActivity.class);
            startActivity(i);

        }
    }
}
