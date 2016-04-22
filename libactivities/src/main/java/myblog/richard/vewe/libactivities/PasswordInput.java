package myblog.richard.vewe.libactivities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PasswordInput extends AppCompatActivity {
    protected static final String tag = "pwd";
    public static final int MAX = 8;        //max length of password
    public static final int MIN = 4;        //min length of password
    public static final String PWD = "pwd";
    public static final String TITLE = "title";
    public static final String INITIAL = "initial";
    public static final String INFO = "info";
    public static final String PWD2 = "pwd2";
    public static final String PKG = "pkg";

    public String getHint()
    {
        return "(" + MIN + "~" + MAX + getString(R.string.password_hint) + ")";
    }

    private Button[] mButtons = new Button[10];
    private TextView m_pwdTitle;
    private TextView m_pwdContent;
    private Button mConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);

        //create the content
        m_pwdTitle = (TextView)findViewById(R.id.password_input_hint);
        m_pwdContent = (TextView)findViewById(R.id.password_content);

        mButtons[0] = (Button)findViewById(R.id.number0Btn);
        mButtons[1] = (Button)findViewById(R.id.number1Btn);
        mButtons[2] = (Button)findViewById(R.id.number2Btn);
        mButtons[3] = (Button)findViewById(R.id.number3Btn);
        mButtons[4] = (Button)findViewById(R.id.number4Btn);
        mButtons[5] = (Button)findViewById(R.id.number5Btn);
        mButtons[6] = (Button)findViewById(R.id.number6Btn);
        mButtons[7] = (Button)findViewById(R.id.number7Btn);
        mButtons[8] = (Button)findViewById(R.id.number8Btn);
        mButtons[9] = (Button)findViewById(R.id.number9Btn);

        for(int i = 0; i < 10; i ++) {
            mButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String sNumber = ((Button) v).getText().toString().trim();
                    updatePwdAfterNumberClicked(sNumber);
                }
            });
        }

        mConfirm = (Button)findViewById(R.id.confirmButton);
        mConfirm.setClickable(false);
        mConfirm.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void setTitle(String title)
    {
        m_pwdTitle.setText(title);
    }

    public void reset(View v)
    {
        m_pwdContent.setText("");
        m_InputedStr = "";
        mConfirm.setClickable(false);
        mConfirm.setTextColor(getResources().getColor(R.color.white));
    }

    public void confirm(View v)
    {
        confirm(m_InputedStr);
    }

    //process the password
    private String m_InputedStr = new String();

    private void updatePwdAfterNumberClicked(String sNumber) {
        int len = m_InputedStr.length();
        if(len >= (MIN-1))
        {
            mConfirm.setClickable(true);
            mConfirm.setTextColor(getResources().getColor(R.color.black));
        }
        if (len >= MAX)
            return ;

        m_InputedStr = m_InputedStr.concat(sNumber);
        String pwdStr = m_InputedStr.replaceAll("\\d", " *");
        m_pwdContent.setText(pwdStr);
        verifyingDuringInput(m_InputedStr);
    }

    protected void verifyingDuringInput(String pwdstr)
    {

    }

    protected void confirm(String pwdstr)
    {

    }
}
