package bui.quocdat.dchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.bumptech.glide.Glide;

import bui.quocdat.dchat.Adapter.MessageAdapter;
import bui.quocdat.dchat.Adapter.PostAdapter;
import bui.quocdat.dchat.Notification.Client;
import bui.quocdat.dchat.Notification.Data;
import bui.quocdat.dchat.Notification.MyResponse;
import bui.quocdat.dchat.Notification.Sender;
import bui.quocdat.dchat.Notification.Token;
import bui.quocdat.dchat.Other.APIService;
import bui.quocdat.dchat.Other.Chat;
import bui.quocdat.dchat.Other.Message;
import bui.quocdat.dchat.Other.Post;
import bui.quocdat.dchat.Other.Status;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.Other.User;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView imgUser;
    private TextView useName;

    //evenLister
    private ValueEventListener eventListener;

    //FireBase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private DatabaseReference referenceFromChats;

    private RecyclerView recyclerView;
    private List<Message> listData;
    private MessageAdapter messageAdapter;

    private ImageView btn_send;
    private EditText texSend;
    private TextView tvStatus;

    private String userIDFriend;

    private APIService apiService;

    private boolean notify = false;

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
        userIDFriend = intent.getStringExtra("userID");

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

        // work with toolbar and read message
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//                useName.setText(user.getUserName());
//                if (!user.getAvatarURL().equals("default")) {
//                    Glide.with(getApplicationContext()).load(user.getAvatarURL()).into(imgUser);
//                }
//
//                readMessage(firebaseUser.getUid(), userIDFriend, user.getAvatarURL());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

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
                            readMessage(id , userIDFriend, infUser.getString("url"), infUser.getString("last_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });



//        setStatusUser(userIDFriend);

        // set Even click
        btn_send.setOnClickListener(this);

//        seenMessage(userIDFriend);

        imgUser.setOnClickListener(this);
        findViewById(R.id.img_attach).setOnClickListener(this);

    }

    private void initView() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userIDFriend);
        referenceFromChats = FirebaseDatabase.getInstance().getReference("Chats");
        tvStatus = findViewById(R.id.tv_status_online_offline);
        btn_send = findViewById(R.id.button_send);
        texSend = findViewById(R.id.text_send);
        imgUser = findViewById(R.id.circle_view_chat);
        useName = findViewById(R.id.chat_user_name);

        socket = SocketManager.getInstance().getSocket();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isSeen", "false");
        reference.child("Chats").push().setValue(hashMap);

        // gui neu khong co mang addOnComplete

//        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
//                .child(firebaseUser.getUid())
//                .child(userIDFriend);
//        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
//                    chatRef.child("id").setValue(userIDFriend);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//
//        //notification
//        final String msg = message;
//
//        DatabaseReference drf = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//        drf.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//                if (notify)
//                    sendNotification(receiver, user.getUserName(), msg);
//                notify = false;
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

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

    //notification
//    private void sendNotification(final String receiver, final String userName, final String message) {
//        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
//        Query query = tokens.orderByKey().equalTo(receiver);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Token token = snapshot.getValue(Token.class);
//                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, userName + " : " + message, "New message", userIDFriend);
//                    Sender sender = new Sender(data, token.getToken());
//
//                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
//                        @Override
//                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
//                            if (response.code() == 200) {
//                                if (response.body().success != 1) {
//                                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<MyResponse> call, Throwable t) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    private void readMessage(String myId, String userID, final String imageUrl, String friend_name) {
        listData = new ArrayList<>();

//        referenceFromChats = FirebaseDatabase.getInstance().getReference("Chats");
//        referenceFromChats.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listData.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Chat chat = snapshot.getValue(Chat.class);
//                    if ((chat.getSender().equals(myId) && chat.getReceiver().equals(userID))
//                            || (chat.getSender().equals(userID) && chat.getReceiver().equals(myId))) {
//                        listData.add(chat);
//                    }
//                }
//                messageAdapter = new MessageAdapter(listData, imageUrl, getApplicationContext());
//                recyclerView.setAdapter(messageAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

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

    private void setOnOff(String onOff) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", onOff);

        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("Status")
                .child(firebaseUser.getUid());

        databaseReference.updateChildren(hashMap);
    }

    private void setStatus(Boolean isOnline) {
        if (isOnline) {
            socket.emit("setOnline", id);
        } else socket.emit("setOffline", id);
    }

    private void seenMessage(final String userIDFriends) {
        eventListener = referenceFromChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userIDFriends)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", "true");
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
//        setOnOff("online");
        setStatus(true);
        //notification
//        setCurrentUser(userIDFriend);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        seenMessage(userIDFriend);
    }

    @Override
    protected void onPause() {
        super.onPause();
        referenceFromChats.removeEventListener(eventListener);
//        setOnOff("offline");

        setStatus(false);
        //notification
//        setCurrentUser("default");
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
                notify = true;

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

    //notification
//    private void setCurrentUser(String user) {
//        SharedPreferences.Editor editor = getSharedPreferences("Preferences_Shared", MODE_PRIVATE).edit();
//        editor.putString("currentUser", user);
//        editor.apply();
//    }
//
//    private void setStatusUser(final String userID) {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Status");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Status status = snapshot.getValue(Status.class);
//                    if (status.getId().equals(userID)) {
//                        if (status.getOnline().equals("online")) {
//                            tvStatus.setText("Online");
//                        } else {
//                            tvStatus.setText("Offline");
//                        }
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

}
