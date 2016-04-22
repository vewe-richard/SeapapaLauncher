package myblog.richard.vewe.libactivities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import myblog.richard.vewe.libusersprovider.UsersContract;

public class SelectProtectAppsActivity extends AppCompatActivity {
    private static final String tag = "selectapps";
    private static final boolean LOGD = true;
    private AppItems mAppItems;
    private ListView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_protect_apps);
        mAppItems = new AppItems(this);
        loadListView();
    }

    private void loadListView(){
        mList = (ListView)findViewById(R.id.apps_list);

        ArrayAdapter<AppItem> adapter = new ArrayAdapter<AppItem>(this,
                R.layout.select_app_row,
                mAppItems.getmItems()) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.select_app_row, null);
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

                CheckBox cb = (CheckBox)convertView.findViewById(R.id.checkBox);
                cb.setOnCheckedChangeListener(new CheckedChangeListener(ai));
                cb.setChecked(ai.getType() == UsersContract.TableApplist.FORBIDDEN);
                return convertView;
            }
        };

        mList.setAdapter(adapter);
    }

    class CheckedChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        private AppItem mAI;
        CheckedChangeListener(AppItem ai)
        {
            mAI = ai;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked)
                mAppItems.changeItemType(mAI, UsersContract.TableApplist.FORBIDDEN);
            else
                mAppItems.changeItemType(mAI, UsersContract.TableApplist.FREE);
        }
    }
}
