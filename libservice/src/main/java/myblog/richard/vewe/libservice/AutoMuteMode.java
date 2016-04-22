package myblog.richard.vewe.libservice;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import java.util.Calendar;

import myblog.richard.vewe.libusersprovider.User;

/**
 * Created by richard on 15-12-28.
 */
public class AutoMuteMode {
    private static final String tag = "automute";

    private static final int CHECK_THRESHOLD = ((60*1000)/GetTaskRunable.DEFAULT_SCAN_INTERVAL);

    private boolean mEnable;
    private int mCount;

    private enum State {INIT_STATE, MUTE_STATE};
    private State mState;

    private int mPreviousMode;

    private MyService mService;

    public AutoMuteMode(MyService service){
        mService = service;
    }

    public void enable(boolean enable)
    {
        Log.d(tag, "auto enabled? " + enable);
        mEnable = enable;
        mCount = 0;
        mState = State.INIT_STATE;
    }

    public void run()
    {
        if((mCount % CHECK_THRESHOLD) != 0){
            mCount ++;
            return;
        }
        mCount ++;

        if(!mEnable) return;

        Log.d(tag, "check if perform auto mute");
        if(mState == State.INIT_STATE){
            if(inClass()){
                AudioManager audiomanage = (AudioManager)mService.getSystemService(Context.AUDIO_SERVICE);
                mPreviousMode = audiomanage.getRingerMode();

                Log.d(tag, "switch to mute state? prev " + mPreviousMode);
                if(mPreviousMode == AudioManager.RINGER_MODE_NORMAL){
                    //just mute it
                    Log.d(tag, "just mute it");
                    audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                mState = State.MUTE_STATE;
            }
            else
            {
                mState = State.INIT_STATE;
            }
        }
        else if(mState == State.MUTE_STATE){
            if(!inClass()){
                AudioManager audiomanage = (AudioManager)mService.getSystemService(Context.AUDIO_SERVICE);

                int mode = audiomanage.getRingerMode();
                Log.d(tag, "switch to unmute state");

                //previous is not mute
                if(mPreviousMode == AudioManager.RINGER_MODE_NORMAL &&
                        mode == AudioManager.RINGER_MODE_SILENT){
                    //unmute it
                    Log.d(tag, "unmute it");
                    audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
                mState = State.INIT_STATE;
            }
        }
    }

    //private int mxxCount;
    //private boolean mIsClass;

    private boolean inClass()
    {
        /*mxxCount ++;

        Log.d(tag, "" + mIsClass + " " + mxxCount);
        if(mxxCount % 12 == 0){
            mIsClass = !mIsClass;
            mxxCount = 0;
            return mIsClass;
        }else
        return mIsClass;*/
        Calendar cld = Calendar.getInstance();

        if(User.isHoliday(cld)){
            return false;
        }

        int hour = cld.get(Calendar.HOUR_OF_DAY);

        if(hour >= 8 && hour < 12){
            return true;
        }

        if(hour >= 14 && hour < 17)
        {
            return true;
        }
        return false;
    }
}
