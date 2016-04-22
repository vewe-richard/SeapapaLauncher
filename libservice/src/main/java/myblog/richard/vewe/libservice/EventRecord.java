package myblog.richard.vewe.libservice;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by richard on 15-9-29.
 */
public class EventRecord implements Serializable {
    private String name;
    private long  time;
    private int duration;
    private String label;
    private byte type;

    private static final long serialVersionUID = 1L;
    public static final String QUIT_EVENT = "Quit";
    public static final String START_EVENT = "Start";
    public static final String CREATE_EVENT = "Create";
    public static final String SCREEN_OFF_EVENT = "ScreenOff";
    public static final String UNKNOWN_STATUS_EVENT = "Unknown Status";

    public EventRecord(String name, long time, int duration, String label, int type)
    {
        this.name = name;
        this.time = time;
        this.duration = duration;
        this.label = label;
        this.type = (byte)type;
    }

    public EventRecord(EventRecord er)
    {
        name = new String(er.getName());
        time = er.getTime();
        duration = er.getDuration();
        label = new String(er.getLabel());
        type = er.getType();
    }

    @Override
    public String toString() {
        return name + ":" + time + ":" + duration;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getLabel() {
        return label;
    }

    public byte getType() {
        return type;
    }
}
