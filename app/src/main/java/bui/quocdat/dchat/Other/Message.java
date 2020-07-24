package bui.quocdat.dchat.Other;

public class Message {
    private int id;
    private int groupID;
    private int sender_id;
    private String text;
    private int mediaID;
    private String created_at;
    private String full_name;
    private String urlMess;
    private String urlUser;
    private String type;

    public Message(int id, int groupID, int sender_id, String text, int mediaID, String created_at, String full_name) {
        this.id = id;
        this.groupID = groupID;
        this.sender_id = sender_id;
        this.text = text;
        this.mediaID = mediaID;
        this.created_at = created_at;
        this.full_name = full_name;
    }

    public Message(int id, int groupID, int sender_id, String text, int mediaID, String created_at, String full_name, String urlMess, String urlUser) {
        this.id = id;
        this.groupID = groupID;
        this.sender_id = sender_id;
        this.text = text;
        this.mediaID = mediaID;
        this.created_at = created_at;
        this.full_name = full_name;
        this.urlMess = urlMess;
        this.urlUser = urlUser;
    }

    public Message(int id, int groupID, int sender_id, String text, int mediaID, String created_at, String full_name, String urlMess, String urlUser, String type) {
        this.id = id;
        this.groupID = groupID;
        this.sender_id = sender_id;
        this.text = text;
        this.mediaID = mediaID;
        this.created_at = created_at;
        this.full_name = full_name;
        this.urlMess = urlMess;
        this.urlUser = urlUser;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrlMess() {
        return urlMess;
    }

    public void setUrlMess(String urlMess) {
        this.urlMess = urlMess;
    }

    public String getUrlUser() {
        return urlUser;
    }

    public void setUrlUser(String urlUser) {
        this.urlUser = urlUser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMediaID() {
        return mediaID;
    }

    public void setMediaID(int mediaID) {
        this.mediaID = mediaID;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }
}