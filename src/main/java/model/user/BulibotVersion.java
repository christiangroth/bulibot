package model.user;

public class BulibotVersion {
    private String name;
    private String source;
    private boolean live;
    private boolean systemTag;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public boolean isSystemTag() {
        return systemTag;
    }

    public void setSystemTag(boolean systemTag) {
        this.systemTag = systemTag;
    }

    @Override
    public String toString() {
        return "BulibotVersion [name=" + name + ", source=" + source + ", live=" + live + ", systemTag=" + systemTag + "]";
    }
}
