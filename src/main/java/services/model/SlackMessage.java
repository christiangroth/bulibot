package services.model;

public class SlackMessage {
    private String username;
    private String text;

    public SlackMessage() {
        this(null, null);
    }

    public SlackMessage(String username, String text) {
        super();
        this.username = username;
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "SlackMessage [username=" + username + ", text=" + text + "]";
    }
}
