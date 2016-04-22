package myblog.richard.vewe.libactivities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import myblog.richard.vewe.libusersprovider.User;
import myblog.richard.vewe.libusersprovider.UsersContract;

/**
 * Created by richard on 15-10-22.
 */
public class PasswordCheckActivity extends PasswordInput {
    String mPwd;
    String mPwd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        mPwd = i.getStringExtra(PasswordInput.PWD);
        mPwd2 = i.getStringExtra(PasswordInput.PWD2);
        Log.d(tag, "to verify pwd " + mPwd);
        String title = i.getStringExtra(PasswordInput.TITLE);
        if(title != null)
        {
            setTitle(title);
        }
        String initial = i.getStringExtra(PasswordInput.INITIAL);
        if(initial != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setTitle("title");
            builder.setMessage(getString(R.string.first_time));
            builder.setNeutralButton(getString(R.string.first_help), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(PasswordCheckActivity.this,
                            myblog.richard.vewe.libactivities.WebviewActivity.class);
                    i.putExtra("url", UsersContract.getServerUrl() + UsersContract.USERGUIDE_URL);
                    startActivity(i);
                }
            });
            builder.setNegativeButton("OK", null);
            AlertDialog ad = builder.create();
            ad.show();
        }
        String reason = i.getStringExtra(PasswordInput.INFO);
        if(title == null && reason != null)
        {
            setTitle(reason);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String question = null, answer = null;
        //Read question and answer from root user
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
            question = (String)user.getProperities().get(UsersContract.TableUsers.Column.QUESTION);
            answer = (String)user.getProperities().get(UsersContract.TableUsers.Column.ANSWER);
        }
        else
        {
            Log.e(tag, "failed to get root password question");
        }


        //noinspection SimplifiableIfStatement
        if (id != R.id.action_settings) {
            return super.onOptionsItemSelected(item);
        }
        if(answer == null || answer.trim().length() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.findpwd_noqa));
            builder.setNegativeButton("OK", null);
            AlertDialog ad = builder.create();
            ad.show();
            return true;
        }

        Log.d(tag, "question " + question);
        Log.d(tag, "answer " + answer);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.hint_question) + question);

        final EditText edittext= new EditText(getApplicationContext());
        edittext.setTextColor(getResources().getColor(R.color.black));
        builder.setView(edittext);

        builder.setNeutralButton(getString(R.string.cancel_it), null);

        final String thisAnswer = answer;
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pwd = edittext.getText().toString().trim();
                if(pwd == null) return;
                if (!pwd.contentEquals(thisAnswer)) {
                    return;
                }
                Log.d(tag, "Question Answer match");

                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordCheckActivity.this);
                builder.setMessage(mPwd);
                builder.setNegativeButton("OK", null);
                AlertDialog ad = builder.create();
                ad.show();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();


        return true;
    }

    @Override
    protected void confirm(String pwdstr) {
        if(mPwd == null){
            //just thinks the verification is OK
            setResult(RESULT_OK);
            notifyService();
            finish();
            return;
        }

        if(pwdstr.contentEquals(mPwd) || (mPwd2 != null && pwdstr.contentEquals(mPwd2)))
        {
            Log.d(tag, "verify OK");
            setResult(RESULT_OK);
            notifyService();
            finish();
        }
        else
        {
            reset(null);
            setTitle(getString(R.string.password_wrong) + getHint());
        }
    }

    protected void notifyService(){

    }

    @Override
    protected void verifyingDuringInput(String pwdstr) {
        if(mPwd == null) return;
        if(pwdstr.contentEquals(mPwd) || (mPwd2 != null && pwdstr.contentEquals(mPwd2)))
        {
            Log.d(tag, "verify ok continue");
            setResult(RESULT_OK);
            notifyService();
            finish();
//            moveTaskToBack(true);
//            finishAffinity();
        }
    }
}
