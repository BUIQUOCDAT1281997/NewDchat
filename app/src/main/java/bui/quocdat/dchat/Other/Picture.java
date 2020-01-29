package bui.quocdat.dchat.Other;

public class Picture {

    private String url;
    private String timeUpload;

    public Picture(String url, String timeUpload) {
        this.url = url;
        this.timeUpload = timeUpload;
    }

    public Picture() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimeUpload() {
        return timeUpload;
    }

    public void setTimeUpload(String timeUpload) {
        this.timeUpload = timeUpload;
    }
}
