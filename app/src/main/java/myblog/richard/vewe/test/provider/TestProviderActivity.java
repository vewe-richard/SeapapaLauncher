package myblog.richard.vewe.test.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import myblog.richard.vewe.libusersprovider.App;
import myblog.richard.vewe.libusersprovider.Rule;
import myblog.richard.vewe.libusersprovider.Rules;
import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;
import myblog.richard.vewe.test.R;

public class TestProviderActivity extends myblog.richard.vewe.libcommon.test.TestActivity {
    private static final String tag = "test";

    static String[] sButtonNames = {
            "query users all","query users basic","~ current",  //row0
            "set current root","~ diga", "update pwd",
            "applist insert pkg0","~ pkg1","~ pkg2",
            "applist delete pkg0","~ pkg1","~ pkg2",
            "applist update pkg0","query *","insert lots",
            "rule insert", "rule query", "applist del*",        //row5
            "lefttime","wifi","timerange"
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
        ContentResolver resolver = getContentResolver();
        if(button == 0)
        {
            Cursor cursor = resolver.query(UsersContract.TableUsers.CONTENT_URI,
                    UsersContract.TableUsers.ALL_COLUMNS,
                    null,
                    null,
                    null);
            Log.d(tag, "query from " + UsersContract.TableUsers.CONTENT_URI);
            if(cursor == null)
            {
                Log.e(tag, "could not query all from table users");
            }
            else if(cursor.getCount() < 1)
            {
                Log.e(tag, "failed to get any user from table users");
            }
            else {
                while(cursor.moveToNext()){
                    User user = new User(cursor);
                    Log.d(tag, "user: " + user);
                }
                cursor.close();
            }
        }
        else
        {
            if(button == 1) //query all user
            {
                Cursor cursor = resolver.query(UsersContract.TableUsers.CONTENT_URI,
                        UsersContract.TableUsers.BASIC_COLUMNS,
                        null,
                        null,
                        null);
                Log.d(tag, "query from " + UsersContract.TableUsers.CONTENT_URI);
                if(cursor == null)
                {
                    Log.e(tag, "could not query all from table users");
                }
                else if(cursor.getCount() < 1)
                {
                    Log.e(tag, "failed to get any user from table users");
                }
                else {
                    while(cursor.moveToNext()){
                        User user = new User(cursor);
                        Log.d(tag, "user: " + user);
                    }
                    cursor.close();
                }
            }
            else //query current
            {
                String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
                Cursor cursor = resolver.query(UsersContract.TableUsers.CONTENT_URI,
                        UsersContract.TableUsers.BASIC_COLUMNS,
                        selection,
                        null,
                        null);
                if(cursor == null)
                {
                    Log.e(tag, "could not query all from table users");
                }
                else if(cursor.getCount() < 1)
                {
                    Log.e(tag, "failed to get any user from table users");
                }
                else {
                    cursor.moveToFirst();
                    User user = new User(cursor);
                    cursor.close();
                    Log.d(tag, "cur: " + user);
                    //TODO
                    //display the user name in information field
                }
            }

        }
    }

    private void updateCurrentPwd()
    {
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        update.put(UsersContract.TableUsers.Column.PASSWORD, "qianjiang");

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the current user password");
        }
        else
        {
            Log.d(tag, "update current user password");
        }
    }

    @Override
    protected void onClickRow1(int button) {
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();


        if(button == 0)
        {
            update.put(UsersContract.TableCurrent.Column.VAL, UsersContract.TableUsers.ROOT);
        }
        else if(button == 1)
        {
            update.put(UsersContract.TableCurrent.Column.VAL, UsersContract.TableUsers.DIGA);
        }
        else //update password of current user
        {
            updateCurrentPwd();
            return;
        }
        int rowsUpdated;
        String selection = UsersContract.TableCurrent.Column.KEY + "=" + UsersContract.TableCurrent.KEY_CURRENT;
        rowsUpdated = resolver.update(UsersContract.TableCurrent.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the current user");
        }
        else
        {
            Log.d(tag, "update current user");
        }
    }

    //test insert
    @Override
    protected void onClickRow2(int button) {
        String name;
        name = "pkg" + button;
        int type;
        String prefix;

        if(button == 0){
            type = UsersContract.TableApplist.FORBIDDEN;
            prefix = "F";
        }else if(button == 1)
        {
            type = UsersContract.TableApplist.HOME;
            prefix = "H";
        }
        else
        {
            type = UsersContract.TableApplist.GAME;
            prefix = "G";
        }

        ContentValues values = new ContentValues();
        values.put(UsersContract.TableApplist.Column.PKG, name);
        values.put(UsersContract.TableApplist.Column.TYPE, type);
        values.put(UsersContract.TableApplist.Column.PREFIX, prefix);
        ContentResolver resolver = getContentResolver();

        Uri newUri;
        newUri = resolver.insert(UsersContract.TableApplist.DIGA_URI,
                values
                );
        Log.d(tag, "insert uri " + newUri);
    }

    //delete rows
    @Override
    protected void onClickRow3(int button) {
        String name;
        name = "pkg" + button;
        String selection = UsersContract.TableApplist.Column.PKG + "=" + name;
        int deleted = getContentResolver().delete(
                UsersContract.TableApplist.DIGA_URI,
                selection,
                null
        );
        Log.d(tag, "deleted " + deleted);
    }

    //update pkg0, query *, insert lots
    @Override
    protected void onClickRow4(int button) {
        if(button == 0)
        {
            updateApplistPkg0();
        }
        else if(button == 1)
        {
            queryApplistAll();
        }
        else
        {
            insertApplistLots();
        }
    }


    private void updateApplistPkg0()
    {
        ContentResolver resolver = getContentResolver();
        ContentValues update = new ContentValues();
        update.put(UsersContract.TableApplist.Column.TYPE, UsersContract.TableApplist.HOME);

        int rowsUpdated;
        String selection = UsersContract.TableApplist.Column.PKG + "=" + "pkg0";
        rowsUpdated = resolver.update(UsersContract.TableApplist.DIGA_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update pkg0");
        }
        else
        {
            Log.d(tag, "update pkg0");
        }
    }

    private void queryApplistAll()
    {
        Cursor cursor = getContentResolver().query(
                UsersContract.TableApplist.DIGA_URI,
                UsersContract.TableApplist.BASIC_COLUMNS,
                null,
                null,
                null);
        if(cursor == null)
        {
            Log.e(tag, "could not query all from table applist diga");
        }
        else if(cursor.getCount() < 1)
        {
            Log.e(tag, "failed to get any entries from table applist diga");
        }
        else {
            while(cursor.moveToNext()){
                App app = new App(cursor);
                Log.d(tag, "app: " + app);
            }
            cursor.close();
        }
    }

    private void insertApplistLots()
    {
        ContentResolver resolver = getContentResolver();

        ContentValues[] lists = new ContentValues[20];

        for(int i = 0; i < 20; i ++) {
            ContentValues values = new ContentValues();

            values.put(UsersContract.TableApplist.Column.PKG, "pkgg" + i);
            values.put(UsersContract.TableApplist.Column.TYPE, 2);
            values.put(UsersContract.TableApplist.Column.PREFIX, "d");
            lists[i] = values;
        }
        int inserted = resolver.bulkInsert(UsersContract.TableApplist.DIGA_URI,
                lists);
        Log.d(tag, "insert " + inserted);
    }

    //rule insert, rule query
    @Override
    protected void onClickRow5(int button) {
        ContentResolver resolver = getContentResolver();
        if(button == 0) //inserted
        {
            ContentValues values = new ContentValues();

            values.put(UsersContract.TableRule.Column.BIT, 31);
            values.put(UsersContract.TableRule.Column.REASON, "test reason");

            Uri inserted = resolver.insert(UsersContract.TableRule.RULE_URI,
                    values);
            Log.d(tag, "insert " + inserted);
        }
        else if(button == 1)
        {
            Cursor cursor = getContentResolver().query(
                    UsersContract.TableRule.RULE_URI,
                    UsersContract.TableRule.COLUMNS,
                    null,
                    null,
                    null);
            if(cursor == null)
            {
                Log.e(tag, "could not query all from table rule game");
            }
            else if(cursor.getCount() < 1)
            {
                Log.e(tag, "failed to get any entries from table rule game");
            }
            else {
                while(cursor.moveToNext()){
                    Rule rule = new Rule(cursor);
                    Log.d(tag, "rule game: " + rule);
                }
                cursor.close();
            }
        }
        else if(button == 2)
        {
            String selection = null;
            int deleted = getContentResolver().delete(
                    UsersContract.TableApplist.DIGA_URI,
                    selection,
                    null
            );
            Log.d(tag, "deleted " + deleted);
        }
    }

    @Override
    protected void onClickRow6(int button) {
        ContentResolver resolver = getContentResolver();

        if(button == 0)     //test left time
        {
            //add timelimited to game rule
            //update lefttime to user diga
            //activate timelimited flag
            ContentValues update = new ContentValues();
            update.put(UsersContract.TableUsers.Column.LEFTTIME, -100);
            int gamerule = UsersContract.TableRule.setBit(0, UsersContract.TableRule.BIT_TIME_LIMITED);
            update.put(UsersContract.TableUsers.Column.GAMERULE, gamerule);

            int rowsUpdated;
            String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.DIGA;
            rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                    update,
                    selection,
                    null);
            if(rowsUpdated != 1)
            {
                Log.e(tag, "failed to update lefttime");
            }
            else
            {
                Log.d(tag, "update lefttime");
            }

        }
        else if(button == 1)
        {
            ContentValues update = new ContentValues();
            int homerule = UsersContract.TableRule.setBit(0, UsersContract.TableRule.BIT_WIFI_CONNECT);
            update.put(UsersContract.TableUsers.Column.HOMERULE, homerule);

            int rowsUpdated;
            String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.DIGA;
            rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                    update,
                    selection,
                    null);
            if(rowsUpdated != 1)
            {
                Log.e(tag, "failed to update wifi");
            }
            else
            {
                Log.d(tag, "update wifi");
            }
        }
        else    //timerange
        {
            ContentValues update = new ContentValues();

            int timerange = UsersContract.TableUsers.setDayType(0, UsersContract.TableUsers.WORKDAY);
            timerange = UsersContract.TableUsers.setFromHour(timerange, 10);
            timerange = UsersContract.TableUsers.setFromMinute(timerange, 32);
            timerange = UsersContract.TableUsers.setToHour(timerange, 22);
            timerange = UsersContract.TableUsers.setToMinute(timerange, 28);

            update.put(UsersContract.TableUsers.Column.RANGE2, timerange);
            int gamerule = UsersContract.TableRule.setBit(0, UsersContract.TableRule.BIT_RANGE2);
            update.put(UsersContract.TableUsers.Column.GAMERULE, gamerule);


            int rowsUpdated;
            String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.DIGA;
            rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                    update,
                    selection,
                    null);
            if(rowsUpdated != 1)
            {
                Log.e(tag, "failed to update timerange");
            }
            else
            {
                Log.d(tag, "update timerange");
            }
        }

    }
}
