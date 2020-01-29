package bui.quocdat.dchat.Other;

public class User {
    private String id;
    private String userName;
    private String avatarURL;
    private String password;
    private String status;

    public User(String id, String userName, String avatarURL, String password, String status) {
        this.id = id;
        this.userName = userName;
        this.avatarURL = avatarURL;
        this.password = password;
        this.status = status;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
