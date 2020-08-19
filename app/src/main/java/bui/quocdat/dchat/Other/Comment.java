package bui.quocdat.dchat.Other;

public class Comment {
    private int id;
    private int post_id;
    private int user_id;
    private String text;
    private int media_id;
    private String url;
    private String created_at;
    private String urlUser;
    private String userName;

    public Comment(int id, int post_id, int user_id, String text, String url, String created_at) {
        this.id = id;
        this.post_id = post_id;
        this.user_id = user_id;
        this.text = text;
        this.url = url;
        this.created_at = created_at;
    }

    public Comment(int id, int post_id, int user_id, String text, String url, String created_at, String urlUser, String userName) {
        this.id = id;
        this.post_id = post_id;
        this.user_id = user_id;
        this.text = text;
        this.url = url;
        this.created_at = created_at;
        this.urlUser = urlUser;
        this.userName = userName;
    }

    public String getUrlUser() {
        return urlUser;
    }

    public void setUrlUser(String urlUser) {
        this.urlUser = urlUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMedia_id() {
        return media_id;
    }

    public void setMedia_id(int media_id) {
        this.media_id = media_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
