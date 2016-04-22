package myblog.richard.vewe.libactivities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import myblog.richard.vewe.libusersprovider.UsersContract;

public class SelectAppsActivity extends AppCompatActivity {
    private static final String tag = "selectapps";
    private static final boolean LOGD = true;
    private AppItems mAppItems;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_apps);
        mAppItems = new AppItems(this);
        loadListView();
    }

    private void loadListView(){
        mList = (ListView)findViewById(R.id.apps_list);

        ArrayAdapter<AppItem> adapter = new ArrayAdapter<AppItem>(this,
                R.layout.app_checkbox,
                mAppItems.getmItems()) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.app_checkbox, null);
                }

                AppItem ai = mAppItems.getmItems().get(position);

                ImageView appIcon = (ImageView)convertView.findViewById(R.id.imageView);
                try {
                    Drawable drawable = mAppItems.getmManager().getApplicationIcon(ai.getPkg());
                    appIcon.setImageDrawable(drawable);

                }catch(Exception e)
                {

                }

                TextView appLabel = (TextView)convertView.findViewById(R.id.textView);
                appLabel.setText(ai.getLabel());

                RadioGroup group = (RadioGroup)convertView.findViewById(R.id.radioGroup);
//                group.setOnCheckedChangeListener(null);
                group.setOnCheckedChangeListener(new CheckedChangeListener(ai));
                switch(ai.getType())
                {
                    case UsersContract.TableApplist.FREE:
                        group.check(R.id.radioButton);
                        break;
                    case UsersContract.TableApplist.HOME:
                        group.check(R.id.radioButton1);
                        break;
                    case UsersContract.TableApplist.GAME:
                        group.check(R.id.radioButton2);
                        break;
                    case UsersContract.TableApplist.FORBIDDEN:
                        group.check(R.id.radioButton3);
                        break;
                }

                return convertView;
            }
        };

        mList.setAdapter(adapter);
    }

    class CheckedChangeListener implements RadioGroup.OnCheckedChangeListener{
        private AppItem mAI;
        CheckedChangeListener(AppItem ai)
        {
            mAI = ai;
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int rbId = group.getCheckedRadioButtonId();
            if(rbId == R.id.radioButton)
                mAppItems.changeItemType(mAI, UsersContract.TableApplist.FREE);
            else if(rbId == R.id.radioButton1)
                mAppItems.changeItemType(mAI, UsersContract.TableApplist.HOME);
            else if(rbId == R.id.radioButton2)
                mAppItems.changeItemType(mAI, UsersContract.TableApplist.GAME);
            else if(rbId == R.id.radioButton3)
                mAppItems.changeItemType(mAI, UsersContract.TableApplist.FORBIDDEN);
        }
    }

}

