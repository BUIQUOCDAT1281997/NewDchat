package bui.quocdat.dchat.Socketconnetion;

public interface OnSocketConnectionListener {
    void onSocketEventFailed();
    void onSocketConnectionStateChange(int socketState);
    void onInternetConnectionStateChange(int socketState);
}