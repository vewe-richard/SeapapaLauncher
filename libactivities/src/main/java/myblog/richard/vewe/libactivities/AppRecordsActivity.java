package myblog.richard.vewe.libactivities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import myblog.richard.vewe.libservice.EventRecord;
import myblog.richard.vewe.libservice.EventRecords;
import myblog.richard.vewe.libservice.EventRecordsIO;
import myblog.richard.vewe.libservice.MessageType;
import myblog.richard.vewe.libservice.RemoteMyService;
import myblog.richard.vewe.libusersprovider.UsersContract;

public class AppRecordsActivity extends AppCompatActivity {
    private static final String tag = "records";
    private static final boolean LOGD = true;
    public static final String DETAIL = "detail";
    private boolean mDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_records);
        mOnCreateTime = new Date().getTime();
        changeDay(mOnCreateTime);
        mService.bind();
        mManager = getPackageManager();
        Intent i = getIntent();
        mDetail = i.getBooleanExtra(DETAIL, false);
        Log.d(tag, "mDetail " + mDetail);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.unbind();
    }
    private EventRecords mERS;
    private boolean mShowERS;
    private RemoteMyService mService = new RemoteMyService(this){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            super.onServiceConnected(name, service);
            recordsR(EventRecords.getDate(mOnCreateTime));    //first time
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case MessageType.Response.RECORDS:
                    Log.d(tag, "Get Records");
                    EventRecords ers = (EventRecords)msg.getData().
                            getSerializable(MessageType.Response.SERL1);
                    if(ers == null){
                        mERS = new EventRecords();
                        mShowERS = false;           //do not show ERS as it is construct from null
                    }
                    else {
                        mERS = ers.clone(!mDetail);
                        mShowERS = true;
                    }
                    update();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void update()
    {
        EventRecords ers = mERS;
        int lefttime = ers.getLeftTime();

        TextView tv = (TextView)findViewById(R.id.left_time);
        String content = null;
        if(lefttime > 0) {
            content = "+" + lefttime/60;
            if(mDetail)
            {
                content += "." + lefttime%60;
            }
        }
        else if (lefttime == 0)
        {
            content = "0";
        }
        else {
            content = "-" + (-lefttime)/60;
            if(mDetail)
            {
                content += "." + (-lefttime)%60;
            }
        }
        if(!mShowERS) content = "";
        tv.setText(content);

        tv = (TextView)findViewById(R.id.base_time);
        if(!mShowERS) content = "";
        else content = "+" + ers.getmBaseTime() + getString(R.string.apps_minute);
        tv.setText(content);

        int prev_left = ers.getmPrevLeftTime();
        if(prev_left == 0)
        {
            content = "0" + getString(R.string.apps_minute);
        }
        else if(prev_left > 0)
        {
            content = "+" + (prev_left/60) + getString(R.string.apps_minute) + (prev_left%60) + getString(R.string.apps_second);
        }
        else
        {
            prev_left = -prev_left;
            content = "-" + (prev_left/60) + getString(R.string.apps_minute) + (prev_left%60) + getString(R.string.apps_second);
        }
        tv = (TextView)findViewById(R.id.previous_left);
        if(!mShowERS) content = "";
        tv.setText(content);


        int extra_added = ers.getmBonusTime();
        if(extra_added == 0)
        {
            content = "0" + getString(R.string.apps_minute);
        }
        else if(extra_added > 0)
        {
            content = "+" + (extra_added/60) + getString(R.string.apps_minute) + (extra_added%60) + getString(R.string.apps_second);
        }
        else
        {
            extra_added = -extra_added;
            content = "-" + (extra_added/60) + getString(R.string.apps_minute) + (extra_added%60) + getString(R.string.apps_second);
        }
        tv = (TextView)findViewById(R.id.extra_added);
        if(!mShowERS) content = "";
        tv.setText(content);

        int played = ers.getmTotalPlayTime();
        if(played == 0)
        {
            content = "0" + getString(R.string.apps_minute);
        }
        else
        {
            content = "-" + (played/60) + getString(R.string.apps_minute) + (played%60) + getString(R.string.apps_second);
        }
        tv = (TextView)findViewById(R.id.time_used);
        if(!mShowERS) content = "";
        tv.setText(content);
        loadListView(ers);
    }

    private ListView list;
    private PackageManager mManager;
    private void loadListView(EventRecords ers){
        final ArrayList<EventRecord> listERS = ers.getmRecords();
        list = (ListView)findViewById(R.id.apps_list_view);

        ArrayAdapter<EventRecord> adapter = new ArrayAdapter<EventRecord>(this,
                R.layout.app_record_item,
                listERS) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.app_record_item, null);
                }

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.appimage);
                try {
                    Drawable drawable = mManager.getApplicationIcon(listERS.get(position).getName());
                    appIcon.setImageDrawable(drawable);

                }catch(Exception e)
                {
                    appIcon.setImageDrawable(null);
                }

                TextView appLabel = (TextView)convertView.findViewById(R.id.appText);
                appLabel.setText(listERS.get(position).getLabel());

                if(LOGD) {
                    Log.d(tag, "pkg " + listERS.get(position).getName());
                }

                TextView startTime = (TextView)convertView.findViewById(R.id.startTime);
                Calendar cld = Calendar.getInstance();
                cld.setTimeInMillis(listERS.get(position).getTime());
                String content = ""+cld.get(Calendar.HOUR_OF_DAY)+":"+cld.get(Calendar.MINUTE);
                if(mDetail)
                {
                    content += "." + cld.get(Calendar.SECOND);
                }
                startTime.setText(content);

                TextView duration = (TextView)convertView.findViewById(R.id.Duration);
                int i = listERS.get(position).getDuration()/1000;
                content = "" + (i/60) + getString(R.string.apps_minute);
                content += "" + i%60 + getString(R.string.apps_second);
                duration.setText(content);

                if(listERS.get(position).getType() == UsersContract.TableApplist.UNKNOWN) {
                    convertView.setBackgroundColor(getResources().getColor(R.color.unknown));
                }
                else if(listERS.get(position).getType() == UsersContract.TableApplist.GAME)
                {
                    convertView.setBackgroundColor(getResources().getColor(R.color.orange));
                }
                else
                {
                    convertView.setBackgroundColor(getResources().getColor(R.color.white));
                }
                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    private String dayTitle(long time)
    {
        String title = "";

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        title += (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " ";
        switch(cal.get(Calendar.DAY_OF_WEEK))
        {
            case Calendar.MONDAY:
                title += getString(R.string.monday);
                break;
            case Calendar.TUESDAY:
                title += getString(R.string.tuesday);
                break;
            case Calendar.WEDNESDAY:
                title += getString(R.string.wednesday);
                break;
            case Calendar.THURSDAY:
                title += getString(R.string.thursday);
                break;
            case Calendar.FRIDAY:
                title += getString(R.string.friday);
                break;
            case Calendar.SATURDAY:
                title += getString(R.string.saturday);
                break;
            case Calendar.SUNDAY:
                title += getString(R.string.sunday);
                break;
        }
        //change to test stored left time
        //title = EventRecords._getDate(cal);

        return title;
    }

    private long mOnCreateTime;
    private long mCurTime;
    //change to test stored left time
    //private final long ONE_DAY = 60*1000*10; //(24*60*60*1000);
    private final long ONE_DAY = (24*60*60*1000);

    private void changeDay(long time)
    {
        if(time > mOnCreateTime) return;
        if(time < (mOnCreateTime - EventRecordsIO.MAX_DAYS_EVENT_RECORDS*ONE_DAY)) return;

        mCurTime = time;
        TextView title = (TextView)findViewById(R.id.date_str);
        title.setText(dayTitle(time));
        mService.recordsR(EventRecords.getDate(mCurTime));
    }

    public void onClick(View v)
    {
        if(v.getId() == R.id.previous_day)
        {
            changeDay(mCurTime - ONE_DAY);
        }
        else
        {
            changeDay(mCurTime + ONE_DAY);
        }
    }
}
