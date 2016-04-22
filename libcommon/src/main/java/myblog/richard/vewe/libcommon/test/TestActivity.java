package myblog.richard.vewe.libcommon.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import myblog.richard.vewe.libcommon.R;

public class TestActivity extends AppCompatActivity {

    private TextView mInfo;
    private EditText mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        nameButtons();
        mInfo = (TextView) findViewById(R.id.information);
        mEdit = (EditText) findViewById(R.id.editText);
    }

    protected String getButtonName(int row, int button)
    {
        return "[" + row + "," + button + "]";
    }

    private int mRows[] = {R.id.row0, R.id.row1, R.id.row2, R.id.row3, R.id.row4,
            R.id.row5, R.id.row6, R.id.row7, R.id.row8};

    private void nameButtons()
    {
        int row = 0;
        for(int i : mRows)
        {
            ViewGroup vg = (ViewGroup)findViewById(i);

            Button b1 = (Button)vg.findViewById(R.id.button0);
            b1.setTransformationMethod(null);
            b1.setText(getButtonName(row, 0));

            Button b2 = (Button)vg.findViewById(R.id.button1);
            b2.setTransformationMethod(null);
            b2.setText(getButtonName(row, 1));

            Button b3 = (Button)vg.findViewById(R.id.button2);
            b3.setTransformationMethod(null);
            b3.setText(getButtonName(row, 2));
            row ++;
        }
    }

    public void onClick(View v)
    {
        int button;
        int rowId, buttonId;

        rowId = ((ViewGroup)v.getParent()).getId();
        buttonId = v.getId();
        if(buttonId == R.id.button0)
            button = 0;
        else if(buttonId == R.id.button1)
            button = 1;
        else
            button = 2;

        if(rowId == R.id.row0)
            onClickRow0(button);
        else if(rowId == R.id.row1)
            onClickRow1(button);
        else if(rowId == R.id.row2)
            onClickRow2(button);
        else if(rowId == R.id.row3)
            onClickRow3(button);
        else if(rowId == R.id.row4)
            onClickRow4(button);
        else if(rowId == R.id.row5)
            onClickRow5(button);
        else if(rowId == R.id.row6)
            onClickRow6(button);
        else if(rowId == R.id.row7)
            onClickRow7(button);
        else if(rowId == R.id.row8)
            onClickRow8(button);
    }

    protected void onClickRow0(int button)
    {

    }

    protected void onClickRow1(int button)
    {

    }

    protected void onClickRow2(int button)
    {

    }

    protected void onClickRow3(int button)
    {

    }

    protected void onClickRow4(int button)
    {

    }

    protected void onClickRow5(int button)
    {

    }

    protected void onClickRow6(int button)
    {

    }

    protected void onClickRow7(int button)
    {

    }

    protected void onClickRow8(int button)
    {

    }

    public void setInfo(String info)
    {
        mInfo.setText(info);
    }

    public String getInput()
    {
        return mEdit.getText().toString().trim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
