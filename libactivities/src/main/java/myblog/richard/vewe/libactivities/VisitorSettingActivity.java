package myblog.richard.vewe.libactivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

public class VisitorSettingActivity extends AppCompatActivity {
    private static final String tag = "visitorSetting";
    private static final boolean LOGD = true;

    private static final int NIGHT_SETTING = 1;
    private static final int VISITOR_APP_SELECTION = 2;
    private static final int CHANGEPWD = 3;
    private static final int PWDQA = 4;
    private static final int ABOUT = 5;

    private static final int REQUEST_CHECK_PASSWORD = 1;
    private static final int REQUEST_SET_PASSWORD = 2;

    private List<MenuItemDetail> mMenuItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_item_list);

        buildSettingList();
        loadListView();
    }


    private void buildSettingList(){
        mMenuItemList = new ArrayList<MenuItemDetail>();

        MenuItemDetail mid;

        mid = new MenuItemDetail(this, R.string.item_app_selection,
                R.drawable.ic_select_app, VISITOR_APP_SELECTION);
        mMenuItemList.add(mid);
        mid = new MenuItemDetail(this, R.string.item_night_setting,
                R.drawable.ic_night, NIGHT_SETTING);
        mMenuItemList.add(mid);
        mid = new MenuItemDetail(this, R.string.item_change_pwd,
                R.drawable.ic_lock, CHANGEPWD);
        mMenuItemList.add(mid);
        mid = new MenuItemDetail(this, R.string.item_password_hint,
                R.drawable.ic_forget_pwd, PWDQA);
        mMenuItemList.add(mid);
        mid = new MenuItemDetail(this, R.string.item_about,
                R.drawable.ic_about, ABOUT);
        mMenuItemList.add(mid);

    }

    private void loadListView(){
        ListView list;

        list = (ListView)findViewById(R.id.menu_item_list);

        ArrayAdapter<MenuItemDetail> adapter = new ArrayAdapter<MenuItemDetail>(this,
                R.layout.menu_item,
                mMenuItemList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.menu_item, null);
                }
                final MenuItemDetail mid = mMenuItemList.get(position);

                if(mid.getmIcon() != null) {
                    ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_icon);
                    Drawable d = mid.getmIcon();
                    d.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
                    appIcon.setImageDrawable(d);
                }

                TextView appdesc = (TextView) convertView.findViewById(R.id.item_label);
                appdesc.setText(mid.getmName());

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = null;
                        switch (mid.getmId()) {
                            case VISITOR_APP_SELECTION:
                                i = new Intent(VisitorSettingActivity.this,
                                        myblog.richard.vewe.libactivities.SelectProtectAppsActivity.class);
                                startActivity(i);
                                break;
                            case CHANGEPWD:
                                changePwd(UsersContract.TableUsers.ROOT);
                                break;
                            case ABOUT:
                                about();
                                break;
                            case PWDQA:
                                i = new Intent(VisitorSettingActivity.this,
                                        myblog.richard.vewe.libactivities.PasswordQA.class);
                                startActivity(i);
                                break;
                        }
                    }
                });

                return convertView;
            }
        };

        list.setAdapter(adapter);
    }

    private void changePwd(String user)
    {
        Intent i;

        //get old password for user
        String selection = UsersContract.TableUsers.Column.NAME + "=" + user;
        Cursor cursor = getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
                new String[]{UsersContract.TableUsers.Column.PASSWORD},
                selection,
                null,
                null);
        String passwrod;
        if(cursor != null && cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            User u = new User(cursor);
            Log.d(tag, "pwd: " + (String) u.getProperities().get(UsersContract.TableUsers.Column.PASSWORD));
            passwrod = (String) u.getProperities().get(UsersContract.TableUsers.Column.PASSWORD);
            cursor.close();
        }
        else {
            Toast.makeText(this, R.string.hint_fail_get_password, Toast.LENGTH_SHORT).show();
            return;
        }

        i = new Intent(VisitorSettingActivity.this,
                myblog.richard.vewe.libactivities.PasswordCheckActivity.class);
        i.putExtra(PasswordInput.TITLE, getResources().getString(R.string.password_old));
        i.putExtra(PasswordInput.PWD, passwrod);
        startActivityForResult(i, REQUEST_CHECK_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CHECK_PASSWORD)
        {
            if(resultCode == RESULT_OK) {
                Intent i = new Intent(this,
                        myblog.richard.vewe.libactivities.NewPasswordActivity.class);
                startActivityForResult(i, REQUEST_SET_PASSWORD);
            }
        }
        else if(requestCode == REQUEST_SET_PASSWORD)
        {
            if(resultCode == RESULT_OK){
                String newpwd = data.getStringExtra(PasswordInput.PWD);
                Log.d(tag, "set password ok: " + newpwd);
                if(newpwd == null) return;

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
                if(rowsUpdated != 1)
                {
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void about()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(R.string.hint_about);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.hint_about_content)
                .setCancelable(false)
                .setNegativeButton("OK", null);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}




















