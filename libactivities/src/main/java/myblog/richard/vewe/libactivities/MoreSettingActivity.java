package myblog.richard.vewe.libactivities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

public class MoreSettingActivity extends AppCompatActivity {
    private static final String tag = "More";
    private static final boolean LOGD = true;

    private RadioGroup mRG;

    private static final int REQUEST_CHECK_PASSWORD = 1;
    private static final int REQUEST_SET_PASSWORD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_setting);

        showViews();
        setupHandles();
    }

    private void showViews()
    {
        mRG = (RadioGroup)findViewById(R.id.hint_group);
        ContentResolver resolver = getContentResolver();

        try {
            String selection = UsersContract.TableCurrent.Column.KEY + "=" + UsersContract.TableCurrent.KEY_HINT_STRATAGE;
            Cursor cursor = resolver.query(UsersContract.TableCurrent.CONTENT_URI,
                    new String[] {UsersContract.TableCurrent.Column.KEY, UsersContract.TableCurrent.Column.VAL},
                    selection,
                    null,
                    null);
            cursor.moveToFirst();

            int index = cursor.getColumnIndex(UsersContract.TableCurrent.Column.VAL);
            int hint = (Integer) cursor.getInt(index);
            switch(hint)
            {
                case UsersContract.TableCurrent.HINT_ONLY_HINT:
                    mRG.check(R.id.hint_only);
                    break;
                case UsersContract.TableCurrent.HINT_DIRECT_LOCK:
                    mRG.check(R.id.hint_direct_lock);
                    break;
                case UsersContract.TableCurrent.HINT_LOCK_AFTER:
                    mRG.check(R.id.hint_lock_after);
                    break;
            }
        } catch(Exception e)
        {

        }
    }

    private void setupHandles()
    {
        mRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int rid = group.getCheckedRadioButtonId();
                int hint = -1;
                if (rid == R.id.hint_only) {
                    hint = UsersContract.TableCurrent.HINT_ONLY_HINT;
                } else if (rid == R.id.hint_direct_lock) {
                    hint = UsersContract.TableCurrent.HINT_DIRECT_LOCK;
                } else if (rid == R.id.hint_lock_after) {
                    hint = UsersContract.TableCurrent.HINT_LOCK_AFTER;
                }

                if (hint == -1) return;

                ContentResolver resolver = getContentResolver();
                ContentValues update = new ContentValues();

                update.put(UsersContract.TableCurrent.Column.VAL, hint);
                String selection = UsersContract.TableCurrent.Column.KEY + "=" + UsersContract.TableCurrent.KEY_HINT_STRATAGE;

                int rowsUpdated;
                rowsUpdated = resolver.update(UsersContract.TableCurrent.CONTENT_URI,
                        update,
                        selection,
                        null);
                if (rowsUpdated != 1) {
                    Log.e(tag, "failed to update hint strategy");
                } else {
                    Log.d(tag, "update hint strategy");
                }
            }
        });
    }

    public void onClick(View v){
        Intent i;
        if(v.getId() == R.id.restore_default){
            defaultRestore();
        }
        else if(v.getId() == R.id.change_password){
            changePwd(UsersContract.TableUsers.ROOT);
        }
        else if(v.getId() == R.id.password_hint){
            i = new Intent(this,
                    myblog.richard.vewe.libactivities.PasswordQA2.class);
            startActivity(i);
        }
        else if(v.getId() == R.id.about_project){
            i = new Intent(this,
                    myblog.richard.vewe.libactivities.WebviewActivity.class);
            i.putExtra("url", UsersContract.getServerUrl() + UsersContract.ABOUT_URL);
            startActivity(i);
        }
    }

    private void defaultRestore()
    {
        //update user setting of diga
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        int flag = UsersContract.TableUsers.USER_FLAG_APPLIST |
                UsersContract.TableUsers.USER_SHOW_LEFTTIME |
                UsersContract.TableUsers.USER_SHOW_PC_ICON |
                UsersContract.TableUsers.USER_AUTO_MUTE;
        int rule = UsersContract.TableRule.setBit(0, UsersContract.TableRule.BIT_RANGE1);
        rule = UsersContract.TableRule.setBit(rule, UsersContract.TableRule.BIT_RANGE2);
        rule = UsersContract.TableRule.setBit(rule, UsersContract.TableRule.BIT_WIFI_CONNECT);

        update.put(UsersContract.TableUsers.Column.FLAG, flag);
        update.put(UsersContract.TableUsers.Column.HOMERULE, rule);
        rule = UsersContract.TableRule.setBit(rule, UsersContract.TableRule.BIT_TIME_LIMITED);
        update.put(UsersContract.TableUsers.Column.GAMERULE, rule);

        update.put(UsersContract.TableUsers.Column.BASETIME, UsersContract.TableUsers.DEFAULT_BASETIME);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the current user default");
        }

        //delete all applist items
        int deleted = getContentResolver().delete(
                UsersContract.TableApplist.DIGA_URI,
                null,
                null
        );
        if(LOGD){
            Log.d(tag, "delete applist " + deleted);
        }

        doneAction();
    }

    private void doneAction()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        alertDialogBuilder.setMessage("Done!");
        // set dialog message
        alertDialogBuilder.setNegativeButton("OK", null);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void changePwd(String user) {
        Intent i;

        //get old password for user
        String selection = UsersContract.TableUsers.Column.NAME + "=" + user;
        Cursor cursor = getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
                new String[]{UsersContract.TableUsers.Column.PASSWORD},
                selection,
                null,
                null);
        String passwrod;
        if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToFirst();
            User u = new User(cursor);
            cursor.close();
            Log.d(tag, "pwd: " + (String) u.getProperities().get(UsersContract.TableUsers.Column.PASSWORD));
            passwrod = (String) u.getProperities().get(UsersContract.TableUsers.Column.PASSWORD);
        } else {
            Toast.makeText(this, R.string.hint_fail_get_password, Toast.LENGTH_SHORT).show();
            return;
        }

        i = new Intent(this,
                myblog.richard.vewe.libactivities.PasswordCheckActivity.class);
        i.putExtra(PasswordInput.TITLE, getResources().getString(R.string.password_old));
        i.putExtra(PasswordInput.PWD, passwrod);
        startActivityForResult(i, REQUEST_CHECK_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_PASSWORD) {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent(this,
                        myblog.richard.vewe.libactivities.NewPasswordActivity.class);
                startActivityForResult(i, REQUEST_SET_PASSWORD);
            }
        } else if (requestCode == REQUEST_SET_PASSWORD) {
            if (resultCode == RESULT_OK) {
                String newpwd = data.getStringExtra(PasswordInput.PWD);
                Log.d(tag, "set password ok: " + newpwd);
                if (newpwd == null) return;

                //update to content provider
                ContentResolver resolver = getContentResolver();
                ContentValues update = new ContentValues();
                update.put(UsersContract.TableUsers.Column.PASSWORD, newpwd);

                int rowsUpdated;
                String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
                rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                        update,
                        selection,
                        null);
                if (rowsUpdated != 1) {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
