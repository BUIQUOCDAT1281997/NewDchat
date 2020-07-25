package bui.quocdat.dchat.Other;

public class Message {
    private int id;
    private int sender_id;
    private int receiver_id;
    private String text;
    private int mediaID;
    private String created_at;
    private String type;

    public Message(int id, int sender_id, int receiver_id, String text, String created_at, String type) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.text = text;
        this.created_at = created_at;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}