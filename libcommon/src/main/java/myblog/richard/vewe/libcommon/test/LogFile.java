package myblog.richard.vewe.libcommon.test;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by richard on 15-12-5.
 */
public class LogFile {
    private static final String LOGNAME = "logfile";
    private static final String tag = "logfile";
    private static final boolean DISABLE = false;
    private static final String VERSION = "1234";

    private static File mFile;
    private static BufferedWriter mBW;


    public static void setup(String basedir)
    {
        boolean append = false;

        if(DISABLE) return;

        mFile = new File(basedir + "/" + LOGNAME);

        try {
            BufferedReader br = new BufferedReader(new FileReader(mFile));
            String s = br.readLine();
            if(s != null && s.contains(VERSION))
            {
                append = true;
            }
        }catch(Exception e)
        {

        }

        try{
            mBW = new BufferedWriter(new FileWriter(mFile, append));
            if(append == false){
                mBW.write(VERSION + "\n");
            }
        }catch(Exception e)
        {

        }
    }

    public static void writeflush(String message)
    {
        if(DISABLE) return;
        if(mBW == null) return;

        write(message);
        try {
            mBW.flush();
        }catch (Exception e){

        }
    }

    public static void write(String message)
    {
        if(DISABLE) return;
        if(mBW == null) return;

        try {
            Calendar cld = Calendar.getInstance();
            String buf = "" + cld.get(Calendar.DAY_OF_MONTH) + "/" + cld.get(Calendar.HOUR_OF_DAY)
                    + ":" + cld.get(Calendar.MINUTE) + ":" + cld.get(Calendar.SECOND) + " " + message + " \n";

            mBW.write(buf);
        }catch(Exception e)
        {

        }
    }

    public static void dump()
    {
        BufferedReader br;

        if(DISABLE) return;

        try {
            br = new BufferedReader(new FileReader(mFile));
            String s;
            while((s = br.readLine()) != null)
            {
                Log.d(tag, s);
            }
        }catch(Exception e)
        {

        }
    }
}
