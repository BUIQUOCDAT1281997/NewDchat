package com.example.dchatapplication.Other;

public class Status {
    private String online;
    private String id;

    public Status(String online, String id) {
        this.online = online;
        this.id= id;
    }

    public Status() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

}
