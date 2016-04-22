package myblog.richard.vewe.libactivities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

public class PasswordQA extends AppCompatActivity {
    private static final String tag = "pwdQA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_qa);
        //get stored answer
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
        Cursor cursor = getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
                new String[]{UsersContract.TableUsers.Column.ANSWER},
                selection,
                null,
                null);
        if(cursor != null && cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            User user = new User(cursor);
            cursor.close();
            //Log.d(tag, "cur: " + user.getProperities().get(UsersContract.TableUsers.Column.ANSWER));
            String answer = (String)user.getProperities().get(UsersContract.TableUsers.Column.ANSWER);
            EditText et = (EditText)findViewById(R.id.editText);
            et.setText(answer);
        }
        else
        {
            Log.e(tag, "failed to get root password question");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EditText et = (EditText)findViewById(R.id.editText);
        //Log.d(tag, et.getText().toString());
        String input = et.getText().toString();
        if(input.length() != 8)
        {
            return;
        }
        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        update.put(UsersContract.TableUsers.Column.QUESTION, getString(R.string.password_question));
        update.put(UsersContract.TableUsers.Column.ANSWER, input);

        int rowsUpdated;
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
        rowsUpdated = resolver.update(UsersContract.TableUsers.CONTENT_URI,
                update,
                selection,
                null);
        if(rowsUpdated != 1)
        {
            Log.e(tag, "failed to update the root user password QA");
        }
    }
}
