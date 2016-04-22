package myblog.richard.vewe.libservice;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by richard on 15-11-25.
 */
public class EventRecordsIO {
    private static final String tag = "eventrecordsIO";
    private static final boolean LOGD = true;

    public static final int MAX_DAYS_EVENT_RECORDS = 7;

    private static final String EVENT_RECORDS_DIR     = "/ers/";
    private String mBaseDir;

    EventRecordsIO(String cacheDir)
    {
        mBaseDir = cacheDir + EVENT_RECORDS_DIR;

        File f = new File(mBaseDir);
        if(!f.exists())
        {
            f.mkdir();
        }
        if(LOGD) {
            Log.d(tag, "EventRecordsIO constructor");
        }
    }

    private String filepath(int date)
    {
        return mBaseDir + date;
    }

    EventRecords read(int date)
    {
        EventRecords ers = null;
        try
        {
            ObjectInputStream in =new ObjectInputStream(
                    new FileInputStream(filepath(date)));
            ers = (EventRecords)in.readObject();
        }
        catch(Exception e){
            Log.e(tag, "failed to read");
        }
        return ers;
    }

    void write(EventRecords er)
    {
        if(er.size() <= 0) return;

        try
        {
            ObjectOutputStream o = new ObjectOutputStream(
                    new FileOutputStream(filepath(er.getmDate())));
            o.writeObject(er);
            o.close();
        }
        catch(Exception e) {
            Log.e(tag, "failed to write file", e);
        }
    }

    void clear()
    {
        File folder = new File(mBaseDir);
        int curDate = EventRecords.getCurDate();

        File[] listOfFiles = folder.listFiles();
        for(File f : listOfFiles)
        {
            if(f.isDirectory()) continue;
            int date;
            try
            {
                date = Integer.parseInt(f.getName());
                if(curDate - date > MAX_DAYS_EVENT_RECORDS)
                {
                    f.delete();
                }

            }catch (Exception e)
            {

            }
        }
    }
}
