package myblog.richard.vewe.libactivities;

/**
 * Created by richard on 15-11-13.
 */
public class AppItem {
    private String label;
    private String pkg;
    private int type;

    public AppItem(String label, String pkg, int type) {
        this.label = label;
        this.pkg = pkg;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public String getPkg() {
        return pkg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
