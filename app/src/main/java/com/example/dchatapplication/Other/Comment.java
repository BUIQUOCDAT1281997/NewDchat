package com.example.dchatapplication.Other;

public class Comment {

    private String senderID;
    private String textComment;
    private String currentTime;

    public Comment(String senderID, String textComment, String currentTime) {
        this.senderID = senderID;
        this.textComment = textComment;
        this.currentTime = currentTime;
    }

    public Comment() {
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getTextComment() {
        return textComment;
    }

    public void setTextComment(String textComment) {
        this.textComment = textComment;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}
