package myblog.richard.vewe.libactivities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import myblog.richard.vewe.libservice.RemoteMyService;
import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

public class BasicControlActivity extends AppCompatActivity {
    private static final String tag = "basic";
    private static final boolean LOGD = true;

    private EditText mBasetime;
    private CheckBox mCBBasetime;
    private EditText mExtra;
    private Button mAdd, mDecrease;
    private Switch mSwitch;
    private CheckBoxs mWifiRule = new CheckBoxs(), mWorkdayRule = new CheckBoxs(), mWeekendRule = new CheckBoxs();
    private User mUser;

    class CheckBoxs{
        CheckBox info;
        CheckBox game;
    }

    private RemoteMyService mService = new RemoteMyService(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_control);
        //read users setting and fill these fields
        readUserSetting();
        setupViews();
        diplayViewBySetting();
        setupViewHandles();
        mService.bind();
    }

    @Override
    protected void onDestroy() {
        mService.unbind();
        super.onDestroy();
    }

    private void readUserSetting()
    {
        ContentResolver resolver = getContentResolver();

        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        Cursor cursor = resolver.query(UsersContract.TableUsers.CONTENT_URI,
                new String[] {
                        UsersContract.TableUsers.Column.GAMERULE,
                        UsersContract.TableUsers.Column.HOMERULE,
                        UsersContract.TableUsers.Column.BASETIME,
                        UsersContract.TableUsers.Column.FLAG
                },
                selection,
                null,
                null);
        if(cursor == null || cursor.getCount() != 1)
        {
            Log.e(tag, "failed to query current user");
            return;
        }

        cursor.moveToFirst();
        mUser = new User(cursor);
        cursor.close();
        if(LOGD) {
            Log.d(tag, "current User " + mUser);
        }
    }

    class EditTextListener implements View.OnFocusChangeListener{
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText et = (EditText)v;
            if(hasFocus == true)
            {
                return;
            }
            if(v.getId() == R.id.bs_basetime)
            {
                try{
                    int basetime = Integer.parseInt(et.getText().toString());
                    Log.d(tag, "set basetime " + basetime);
                    if(basetime < 0) basetime = 0;
                    if(basetime > 24*60) basetime = 24*60;
                    //update to content provider
                    updateBasetime(basetime);
                }catch (NumberFormatException e)
                {

                }
            }
        }

    }

    private void updateBasetime(int basetime)
    {
        //Check if base time is changed, else no necessary to preocess it
        Object obj;

        obj = mUser.getProperities().get(UsersContract.TableUsers.Column.BASETIME);
        if(obj != null) {
            int prev = (Integer) obj;
            if(prev == basetime) return;
        }

        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        update.put(UsersContract.TableUsers.Column.BASETIME, basetime);
        mUser.getProperities().put(UsersContract.TableUsers.Column.BASETIME, basetime);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the current user base time");
        }
        else
        {
            mService.updateUserSetting();
        }
    }

    private void updateFlag(int flag)
    {
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        update.put(UsersContract.TableUsers.Column.FLAG, flag);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the current user flag");
        }
        else
        {
            mService.updateUserSetting();
        }
    }

    class CBCheckedChange implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView.getId() == R.id.bs_switch_auto_mute)
            {
                Log.d(tag, "auto mute");
                if(mUser == null) return;
                Object obj = mUser.getProperities().get(UsersContract.TableUsers.Column.FLAG);
                if(obj == null) return;

                int flag = (Integer)obj;
                if(isChecked) {
                    flag = UsersContract.TableUsers.enableAutoMute(flag);
                }
                else
                {
                    flag = UsersContract.TableUsers.disableAutoMute(flag);
                }
                updateFlag(flag);
            }
            else if(buttonView.getId() == R.id.bs_cb_no_limit)
            {
                Log.d(tag, "no limit to base time");
                if(isChecked)
                {
                    mBasetime.setText("");
                    updateBasetime(UsersContract.TableUsers.BASETIME_NOLIMIT);
                }
            }
        }
    }

    class ButtonOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String aa = mExtra.getText().toString().trim();
            int val = 0;

            try{
                val = Integer.parseInt(aa);
            }catch (Exception e)
            {
                return;
            }


            if(v.getId() == R.id.bs_extra_add) {
                Log.d(tag, "click add extra");
                mService.addtime(val);
            }
            else if(v.getId() == R.id.bs_extra_decrease)
            {
                Log.d(tag, "click decrease extra");
                mService.addtime(-val);
            }

        }
    }

    @Override
    protected void onStop() {
        if(mBasetime.getText().length() > 0) {
            mBasetime.clearFocus();
        }
        super.onStop();
    }

    private void setupViews()
    {
        mBasetime = (EditText)findViewById(R.id.bs_basetime);
        mCBBasetime = (CheckBox)findViewById(R.id.bs_cb_no_limit);

        mExtra = (EditText)findViewById(R.id.bs_extra_number);

        mAdd = (Button)findViewById(R.id.bs_extra_add);

        mDecrease = (Button)findViewById(R.id.bs_extra_decrease);

        mSwitch = (Switch)findViewById(R.id.bs_switch_auto_mute);

        setupRuleViews((LinearLayout) findViewById(R.id.bs_wifirule), mWifiRule);
        setupRuleViews((LinearLayout) findViewById(R.id.bs_workdayrule), mWorkdayRule);
        setupRuleViews((LinearLayout) findViewById(R.id.bs_weekendrule), mWeekendRule);
    }

    private void setupRuleViews(LinearLayout layout, CheckBoxs cbs)
    {
        cbs.info = (CheckBox)layout.findViewById(R.id.bs_cb_home);
        cbs.game = (CheckBox)layout.findViewById(R.id.bs_cb_game);

    }

    private void setupViewHandles()
    {
        mBasetime.setOnFocusChangeListener(new EditTextListener());
        mBasetime.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                mCBBasetime.setChecked(false);
                return false;
            }
        });

        mCBBasetime.setOnCheckedChangeListener(new CBCheckedChange());

        mAdd.setOnClickListener(new ButtonOnClick());

        mDecrease.setOnClickListener(new ButtonOnClick());

        mSwitch.setOnCheckedChangeListener(new CBCheckedChange());

        setupRuleViewHandles((LinearLayout) findViewById(R.id.bs_wifirule), mWifiRule);
        setupRuleViewHandles((LinearLayout)findViewById(R.id.bs_workdayrule), mWorkdayRule);
        setupRuleViewHandles((LinearLayout)findViewById(R.id.bs_weekendrule), mWeekendRule);
    }

    class CBRuleCheckedChange implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int layout = (Integer)buttonView.getTag();

            String group;
            if(buttonView.getId() == R.id.bs_cb_home)
            {
                group = UsersContract.TableUsers.Column.HOMERULE;
            }
            else
            {
                group = UsersContract.TableUsers.Column.GAMERULE;
            }

            if(layout == R.id.bs_wifirule)
            {
                ruleChange(UsersContract.TableRule.BIT_WIFI_CONNECT, group, isChecked);
            }
            else if(layout == R.id.bs_workdayrule)
            {
                ruleChange(UsersContract.TableRule.BIT_RANGE1, group, isChecked);
            }
            else if(layout == R.id.bs_weekendrule)
            {
                ruleChange(UsersContract.TableRule.BIT_RANGE2, group, isChecked);
            }
        }
    }

    private void ruleChange(int bit, String group, boolean isChecked)
    {
        if(mUser == null) return;
        Object obj = mUser.getProperities().get(group);
        if(obj == null) return;

        int value = (Integer)obj;
        if(isChecked) {
            value = UsersContract.TableRule.setBit(value, bit);
        }
        else
        {
            value = UsersContract.TableRule.clearBit(value, bit);
        }

        mUser.getProperities().put(group, value);
        //update to content provider
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        update.put(group, value);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the current user activate rule");
        }
    }

    private void setupRuleViewHandles(LinearLayout layout, CheckBoxs cbs) {
        cbs.info.setTag(layout.getId());
        cbs.info.setOnCheckedChangeListener(new CBRuleCheckedChange());
        cbs.game.setTag(layout.getId());
        cbs.game.setOnCheckedChangeListener(new CBRuleCheckedChange());

    }

    void diplayViewBySetting()
    {
        if(mUser == null) return;

        Object obj;

        obj = mUser.getProperities().get(UsersContract.TableUsers.Column.BASETIME);
        if(obj != null)
        {
            int basetime = (Integer)obj;
            if(basetime == UsersContract.TableUsers.BASETIME_NOLIMIT)
            {
                mCBBasetime.setChecked(true);
                mBasetime.setText("");
            }
            else {
                mCBBasetime.setChecked(false);
                mBasetime.setText("" + basetime);
            }
        }

        obj = mUser.getProperities().get(UsersContract.TableUsers.Column.FLAG);
        if(obj != null)
        {
            int flag = (Integer)obj;
            if(UsersContract.TableUsers.autoMuteEnable(flag))
            {
                mSwitch.setChecked(true);
            }
            else
            {
                mSwitch.setChecked(false);
            }
        }

        obj = mUser.getProperities().get(UsersContract.TableUsers.Column.HOMERULE);
        int homeActivate = 0;
        if(obj != null)
        {
            homeActivate = (Integer)obj;
        }

        obj = mUser.getProperities().get(UsersContract.TableUsers.Column.GAMERULE);
        int gameActivate = 0;
        if(obj != null)
        {
            gameActivate = (Integer)obj;
        }

        if(LOGD)
        {
            Log.d(tag, "home activate " + homeActivate + " game activate " + gameActivate);
        }
        displayCBView(UsersContract.TableRule.BIT_WIFI_CONNECT, homeActivate, gameActivate, mWifiRule);
        displayCBView(UsersContract.TableRule.BIT_RANGE1, homeActivate, gameActivate, mWorkdayRule);
        displayCBView(UsersContract.TableRule.BIT_RANGE2, homeActivate, gameActivate, mWeekendRule);
    }

    private void displayCBView(int bit, int homeActivate, int gameActivate, CheckBoxs cbs)
    {
        if(UsersContract.TableRule.isBitSet(homeActivate, bit))
        {
            cbs.info.setChecked(true);
        }
        else
        {
            cbs.info.setChecked(false);
        }

        if(UsersContract.TableRule.isBitSet(gameActivate, bit))
        {
            cbs.game.setChecked(true);
        }
        else
        {
            cbs.game.setChecked(false);
        }
    }

}
