package bui.quocdat.dchat.Other;

public class User {
    private int id;
    private String phone;
    private String email;
    private String password;
    private String fullName;
    private Boolean is_active;
    private Boolean is_reported;
    private Boolean is_blocked;
    private String picture_id;
    private String preferences;
    private String created_at;
    private Boolean status;
    private String url;

    public User(int id, String phone, String email, String password, String fullName, String preferences, String created_at, Boolean status, String url) {
        this.id = id;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.preferences = preferences;
        this.created_at = created_at;
        this.status = status;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }

    public Boolean getIs_reported() {
        return is_reported;
    }

    public void setIs_reported(Boolean is_reported) {
        this.is_reported = is_reported;
    }

    public Boolean getIs_blocked() {
        return is_blocked;
    }

    public void setIs_blocked(Boolean is_blocked) {
        this.is_blocked = is_blocked;
    }

    public String getPicture_id() {
        return picture_id;
    }

    public void setPicture_id(String picture_id) {
        this.picture_id = picture_id;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
