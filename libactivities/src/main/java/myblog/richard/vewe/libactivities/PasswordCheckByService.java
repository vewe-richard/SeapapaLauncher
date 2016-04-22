package myblog.richard.vewe.libactivities;

import android.os.Bundle;

import myblog.richard.vewe.libactivities.PasswordInput;
import myblog.richard.vewe.libservice.RemoteMyService;

/**
 * Created by richard on 15-11-25.
 */
public class PasswordCheckByService extends myblog.richard.vewe.libactivities.PasswordCheckActivity {

    private RemoteMyService mService = new RemoteMyService(this);
    private boolean mAuthorised;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mService.bind();
    }

    @Override
    protected void notifyService() {
        String pkg = getIntent().getStringExtra(PasswordInput.PKG);
        if(pkg != null) {
            mAuthorised = true;
            mService.setAuthorised(pkg);
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onDestroy() {
        if(mAuthorised == false){
            mService.clearAuthorised(getIntent().getStringExtra(PasswordInput.PKG));
        }
        mService.unbind();
        super.onDestroy();
    }
}
