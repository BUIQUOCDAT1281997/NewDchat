package bui.quocdat.dchat.Other;

public class Post {
    private int id;
    private int user_id;
    private String caption;
    private int media_id;
    private int sum_likes;
    private int sum_comments;
    private String created_at;
    private String urlUser;
    private String urlPost;
    private String fullName;

    public Post(int id, int user_id, String caption, int media_id, int sum_likes, int sum_comments, String created_at) {
        this.id = id;
        this.user_id = user_id;
        this.caption = caption;
        this.media_id = media_id;
        this.sum_likes = sum_likes;
        this.sum_comments = sum_comments;
        this.created_at = created_at;
    }

    public Post(int id, int user_id, String caption, int media_id, int sum_likes, int sum_comments, String created_at, String urlUser, String urlPost) {
        this.id = id;
        this.user_id = user_id;
        this.caption = caption;
        this.media_id = media_id;
        this.sum_likes = sum_likes;
        this.sum_comments = sum_comments;
        this.created_at = created_at;
        this.urlUser = urlUser;
        this.urlPost = urlPost;
    }

    public Post(int id, int user_id, String caption, int media_id, int sum_likes, int sum_comments, String created_at, String urlUser, String urlPost, String fullName) {
        this.id = id;
        this.user_id = user_id;
        this.caption = caption;
        this.media_id = media_id;
        this.sum_likes = sum_likes;
        this.sum_comments = sum_comments;
        this.created_at = created_at;
        this.urlUser = urlUser;
        this.urlPost = urlPost;
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUrlUser() {
        return urlUser;
    }

    public void setUrlUser(String urlUser) {
        this.urlUser = urlUser;
    }

    public String getUrlPost() {
        return urlPost;
    }

    public void setUrlPost(String urlPost) {
        this.urlPost = urlPost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getMedia_id() {
        return media_id;
    }

    public void setMedia_id(int media_id) {
        this.media_id = media_id;
    }

    public int getSum_likes() {
        return sum_likes;
    }

    public void setSum_likes(int sum_likes) {
        this.sum_likes = sum_likes;
    }

    public int getSum_comments() {
        return sum_comments;
    }

    public void setSum_comments(int sum_comments) {
        this.sum_comments = sum_comments;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
