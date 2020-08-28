package bui.quocdat.dchat.Other;

import java.util.HashMap;

public class Strings {
    public static final String SHARED_PREFERENCES_NAME = "login";
    public static final String STATUS = "login_status";
    public static final String USER_ID = "user_id";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_EMAIL = "password";

    public static HashMap<String, String> getRemoteMessageHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Strings.REMOTE_MSG_AUTHORIZATION,
                "AAAAZq4CwEc:APA91bH_iit2a0xmyvxo8zfLUY48OiNK4alKkzfdz-89cgZSj8opvOXBLhV2Cu8mwpGiJQ7NAip7-y464JWHomy0e0JMHHNCteaQQ_PLfxqe6Iyp8iMgFTsqMeSC2lzq4PY9OYR-qLvk"
        );
        headers.put(Strings.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;
    }
}
