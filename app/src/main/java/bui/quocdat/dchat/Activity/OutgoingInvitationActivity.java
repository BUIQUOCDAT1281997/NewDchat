package bui.quocdat.dchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import bui.quocdat.dchat.Other.APIService;
import bui.quocdat.dchat.Other.ApiClient;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private ImageView stopCall;
    private TextView nameUser;
    private String inviterToken = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        initView();
        String meetingType = getIntent().getStringExtra("type");
        User user = (User) getIntent().getSerializableExtra("user");

        // inviterToken = tokenServer
        stopCall.setOnClickListener(view -> onBackPressed());

        if (meetingType != null ) {
            if (meetingType.equals("video")) {
                initiateMeeting(meetingType, "hhhh");
            }
        }
    }

    private void initView() {
        stopCall = findViewById(R.id.outgoing_stop);
        nameUser = findViewById(R.id.outgoing_user_name);
    }

    private void initiateMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Strings.REMOTE_MSG_TYPE, Strings.REMOTE_MSG_INVITATION);
            data.put(Strings.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Strings.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            body.put(Strings.REMOTE_MSG_DATA, data);
            body.put(Strings.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Strings.REMOTE_MSG_INVITATION);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(APIService.class).sendRemoteMessage(
                Strings.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Strings.REMOTE_MSG_INVITATION)){
                        Toast.makeText(OutgoingInvitationActivity.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OutgoingInvitationActivity.this, "response.message()", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, "t.getMessage()", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}