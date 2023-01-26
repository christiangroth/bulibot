package model.user;

public class UserNamesData {
    private final long id;
    private final String name;
    private final String bulibotName;

    public UserNamesData(User user) {
        id = user.getId();
        name = user.getName();
        bulibotName = user.getBulibotName();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBulibotName() {
        return bulibotName;
    }
}
