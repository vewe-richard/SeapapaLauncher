package myblog.richard.vewe.libusersprovider;

import android.os.Build;

/**
 * Created by richard on 15-12-20.
 */
public class DeviceSpecific {
    private static DeviceSpecific mDeviceSpecific = new DeviceSpecific();

    public static final int UNKNOWN_TYPE = 0;
    public static final int GOOGLE_TYPE = 1;
    public static final int XIAOMI_TYPE = 2;

    private String mManufacture;
    private String mModel;

    private int mType;
    private DeviceSpecific()
    {
        mManufacture = Build.MANUFACTURER;
        mModel = Build.MODEL;
    }

    public static DeviceSpecific getInstance(){
        return mDeviceSpecific;
    }

    public int getType()
    {
        return mType;
    }

    public void setType(int type){
        mType = type;
    }

}
