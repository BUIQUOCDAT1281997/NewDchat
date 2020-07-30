package bui.quocdat.dchat.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bui.quocdat.dchat.Adapter.MessageAdapter;
import bui.quocdat.dchat.Other.Message;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView imgUser;
    private TextView useName;

    //FireBase
    private FirebaseUser firebaseUser;

    private RecyclerView recyclerView;
    private List<Message> listData;
    private MessageAdapter messageAdapter;

    private ImageView btn_send;
    private EditText texSend;
    private TextView tvStatus;

    private String userIDFriend;


    private String id;

    // my server
    private Socket socket;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get data
        Intent intent = getIntent();
        userIDFriend = String.valueOf(intent.getIntExtra("userID", 1));

        SharedPreferences sharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        id = sharedPreferences.getString(Strings.USER_ID, "");

        //initView
        initView();

        //Toolbar
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recyclerview
        recyclerView = findViewById(R.id.recycler_view_message);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);


        socket.emit("getInfOfUser", userIDFriend).on("resultInfOfUser", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject infUser = (JSONObject) args[0];
                        try {
                            String url = infUser.getString("url");
                            if (!url.isEmpty()) {
                                Glide.with(getApplicationContext())
                                        .load(url)
                                        .into(imgUser);
                            }
                            useName.setText(infUser.getString("last_name"));
                            if (infUser.getBoolean("status")) tvStatus.setText("Online");
                            else tvStatus.setText("Offline");
                            imageUrl = infUser.getString("url");
                            readMessage(id , userIDFriend, infUser.getString("url"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


        // set Even click
        btn_send.setOnClickListener(this);

//        seenMessage(userIDFriend);

        imgUser.setOnClickListener(this);
        findViewById(R.id.img_attach).setOnClickListener(this);

    }

    private void initView() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tvStatus = findViewById(R.id.tv_status_online_offline);
        btn_send = findViewById(R.id.button_send);
        texSend = findViewById(R.id.text_send);
        imgUser = findViewById(R.id.circle_view_chat);
        useName = findViewById(R.id.chat_user_name);

        socket = SocketManager.getInstance().getSocket();

    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen", "false");
        reference.child("Chats").push().setValue(hashMap);

        JSONObject object = new JSONObject();
        try {
            object.put("message", message);
            object.put("receiver_id", receiver);
            object.put("sender_id", sender);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("messagedectectionfromsingle", 200, object);


        //received the message
        socket.on("messagefromsingle", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        Message message;
                        try {
                            message = new Message(Integer.parseInt(data.getString("id"))
                                    , Integer.parseInt(data.getString("sender_id"))
                                    , Integer.parseInt(data.getString("receiver_id"))
                                    , data.getString("text")
                                    , data.getString("created_at")
                                    , data.getString("type"));
                            listData.add(message);
                messageAdapter = new MessageAdapter(listData, imageUrl, getApplicationContext());
                recyclerView.setAdapter(messageAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    private void readMessage(String myId, String userID, final String imageUrl) {
        listData = new ArrayList<>();

        socket.emit("getMessages", myId, userID).on("resultGetMessages", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray array = (JSONArray) args[0];
                        try {
                            Message message;
                            JSONObject current;
                            for (int i = 0; i < array.length(); i++) {
                                current = array.getJSONObject(i);
                                message = new Message(Integer.parseInt(current.getString("id"))
                                        , Integer.parseInt(current.getString("sender_id"))
                                        , Integer.parseInt(current.getString("receiver_id"))
                                        , current.getString("text")
                                        , current.getString("created_at")
                                        , current.getString("type"));
                                listData.add(message);
                            }
                messageAdapter = new MessageAdapter(listData, imageUrl, getApplicationContext());
                recyclerView.setAdapter(messageAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void setStatus(Boolean isOnline) {
        if (isOnline) {
            socket.emit("setOnline", id);
        } else socket.emit("setOffline", id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus(true);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.circle_view_chat:
                Intent intent = new Intent(ChatActivity.this, ProfileFriendActivity.class);
                intent.putExtra("userID", userIDFriend);
                startActivity(intent);
                break;
            case R.id.img_attach:
                showBottomSheet();
                break;
            case R.id.button_send: {
                //notification

                String currentText = texSend.getText().toString();
                if (!TextUtils.isEmpty(currentText)) {
                    sendMessage(id, userIDFriend, currentText);
                }
                texSend.setText("");
                break;
            }
            case R.id.tv_file_pdf:
                Toast.makeText(this, "pdf", Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_photo_video:
                Toast.makeText(this, "photo video", Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_music:
                Toast.makeText(this, "music", Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_location:
                Toast.makeText(this, "location", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    private void showBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
        view.findViewById(R.id.tv_file_pdf).setOnClickListener(this);
        view.findViewById(R.id.tv_photo_video).setOnClickListener(this);
        view.findViewById(R.id.tv_music).setOnClickListener(this);
        view.findViewById(R.id.tv_location).setOnClickListener(this);
    }

}
