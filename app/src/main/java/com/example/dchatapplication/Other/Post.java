package com.example.dchatapplication.Other;

public class Post {

    private String senderID;
    private String caption;
    private String picturePost;
    private String nameReference;
    private String currentTime;
    private String counterLikes;

    public Post(String senderID, String caption, String picturePost, String nameReference, String currentTime, String counterLikes) {
        this.senderID = senderID;
        this.caption = caption;
        this.picturePost = picturePost;
        this.nameReference = nameReference;
        this.currentTime = currentTime;
        this.counterLikes = counterLikes;
    }

    public Post() {
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPicturePost() {
        return picturePost;
    }

    public void setPicturePost(String picturePost) {
        this.picturePost = picturePost;
    }

    public String getNameReference() {
        return nameReference;
    }

    public void setNameReference(String nameReference) {
        this.nameReference = nameReference;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getCounterLikes() {
        return counterLikes;
    }

    public void setCounterLikes(String counterLikes) {
        this.counterLikes = counterLikes;
    }
}
