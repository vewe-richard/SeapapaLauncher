package myblog.richard.vewe.libactivities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

public class PasswordQA2 extends AppCompatActivity {
    private static final String tag = "pwdQA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_qa2);
        String selection = UsersContract.TableUsers.Column.NAME + "=" + UsersContract.TableUsers.ROOT;
        Cursor cursor = getContentResolver().query(UsersContract.TableUsers.CONTENT_URI,
                new String[]{UsersContract.TableUsers.Column.QUESTION, UsersContract.TableUsers.Column.ANSWER},
                selection,
                null,
                null);
        if(cursor != null && cursor.getCount() == 1)
        {
            cursor.moveToFirst();
            User user = new User(cursor);
            cursor.close();
            String question = (String)user.getProperities().get(UsersContract.TableUsers.Column.QUESTION);
            if(question != null) {
                EditText et = (EditText) findViewById(R.id.editTexta);
                et.setText(question);
            }
            String answer = (String)user.getProperities().get(UsersContract.TableUsers.Column.ANSWER);
            if(answer != null) {
                EditText et = (EditText) findViewById(R.id.editTextb);
                et.setText(answer);
            }
        }
        else
        {
            Log.e(tag, "failed to get root password question");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EditText qet = (EditText)findViewById(R.id.editTexta);
        String question = qet.getText().toString();

        EditText aet = (EditText)findViewById(R.id.editTextb);
        String answer = aet.getText().toString().trim();

        ContentResolver resolver = getContentResolver();

        ContentValues update = new ContentValues();

        update.put(UsersContract.TableUsers.Column.QUESTION, question);
        update.put(UsersContract.TableUsers.Column.ANSWER, answer);

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
