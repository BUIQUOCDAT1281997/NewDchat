package bui.quocdat.dchat.Activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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

    private RecyclerView recyclerView;
    private List<Message> listData;
    private MessageAdapter messageAdapter;

    private ImageView btn_send;
    private EditText texSend;
    private TextView tvStatus;

    private String userIDFriend;

    private StorageReference mStorageRef;

    //add file
    private ConstraintLayout add_layout;
    private ImageView add_image_view;
    private ImageView add_close, add_pdf;
    private MaterialCardView cardView;

    private Uri uriMess;

    private boolean isPicture = false;
    private boolean isMusic = false;
    private boolean isFile = false;
    private boolean isLocation = false;

    private String id;

    private BottomSheetDialog bottomSheetDialog;

    // my server
    private Socket socket;

    private String imageUrl;

    private ImageView voiceCall;
    private ImageView videoCall;


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
                            readMessage(id, userIDFriend, infUser.getString("url"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


        // set Even click
        btn_send.setOnClickListener(this);
        videoCall.setOnClickListener(this);
        voiceCall.setOnClickListener(this);

//        seenMessage(userIDFriend);

        imgUser.setOnClickListener(this);
        findViewById(R.id.img_attach).setOnClickListener(this);

        add_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uriMess = null;
                add_layout.setVisibility(View.GONE);
                isPicture = false;
                isFile = false;
                isLocation = false;
                isMusic = false;
            }
        });

        markAsRead();

        socket.on("result_mark", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        readMessage(id, userIDFriend, imageUrl);
                    }
                });
            }
        });

    }

    private void initView() {
        voiceCall = findViewById(R.id.chat_call);
        videoCall = findViewById(R.id.chat_meeting);
        tvStatus = findViewById(R.id.tv_status_online_offline);
        btn_send = findViewById(R.id.button_send);
        texSend = findViewById(R.id.text_send);
        imgUser = findViewById(R.id.circle_view_chat);
        useName = findViewById(R.id.chat_user_name);
        add_layout = findViewById(R.id.chat_clayout);
        add_image_view = findViewById(R.id.chat_add_picture);
        add_close = findViewById(R.id.chat_add_close);
        add_pdf = findViewById(R.id.chat_pdf);
        cardView = findViewById(R.id.materialCardView2);
        String id_storage = (Integer.parseInt(userIDFriend) > Integer.parseInt(id)) ? (userIDFriend + id) : (id + userIDFriend);
        mStorageRef = FirebaseStorage.getInstance().getReference("chats").child(id_storage);

        socket = SocketManager.getInstance().getSocket();

        listData = new ArrayList<>();

    }

    private void readMessage(String myId, String userID, final String imageUrl) {
        socket.emit("getMessages", myId, userID).on("resultGetMessages", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray array = (JSONArray) args[0];
                        listData.clear();
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
                                        , current.getString("type")
                                        , current.getString("url")
                                        , current.getBoolean("is_seen"));
                                listData.add(message);
                            }
                            Collections.reverse(listData);
                            messageAdapter = new MessageAdapter(listData, imageUrl, getApplicationContext());
                            messageAdapter.notifyDataSetChanged();
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
                                    , data.getString("type")
                                    , data.getString("url")
                                    , data.getBoolean("is_seen"));
                            listData.add(message);
                            messageAdapter = new MessageAdapter(listData, imageUrl, getApplicationContext());
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(messageAdapter);
                            Log.d("messages", "go");
                            if (!id.equals(String.valueOf(data.getInt("sender_id")))){
                                markAsRead();
                                Log.d("messages", "mark");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        markAsRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus(false);
        socket.off("messagefromsingle");
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

                String currentText = texSend.getText().toString();
                if (!TextUtils.isEmpty(currentText)) {
                    sendMessage(id, userIDFriend, currentText);
                }
                texSend.setText("");
                break;
            }
            case R.id.tv_file_pdf:
                Intent iPDF = new Intent();
                iPDF.setAction(Intent.ACTION_GET_CONTENT);
                iPDF.setType("application/pdf");
                startActivityForResult(Intent.createChooser(iPDF, "Select PDF File"), 201);
                break;
            case R.id.tv_photo_video:
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(i, 202);
                break;
            case R.id.tv_music:
                Toast.makeText(this, "music", Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_location:
                Toast.makeText(this, "location", Toast.LENGTH_LONG).show();
                break;

            case R.id.chat_call:
                Intent voice_intent = new Intent(ChatActivity.this, OutgoingInvitationActivity.class);
                voice_intent.putExtra("friend_user", userIDFriend);
                voice_intent.putExtra("type", "audio");
                startActivity(voice_intent);

            case R.id.chat_meeting:
                Intent meeting_intent = new Intent(ChatActivity.this, OutgoingInvitationActivity.class);
                meeting_intent.putExtra("friend_user", userIDFriend);
                meeting_intent.putExtra("type", "video");
                startActivity(meeting_intent);

            default:
                break;
        }
    }

    private void sendMessage(String sender, String receiver, String message) {

        JSONObject object = new JSONObject();
        try {
            object.put("message", message);
            object.put("receiver_id", receiver);
            object.put("sender_id", sender);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!isPicture && !isMusic && !isLocation && !isFile) {
            socket.emit("messagedectectionfromsingle", 200, object);
        }
        if (isPicture) {
            add_layout.setVisibility(View.GONE);
            sendMessagesWithFile(false, false, false, object);
        }
        if (isFile) {
            add_layout.setVisibility(View.GONE);
            sendMessagesWithFile(true, false, false, object);
        }
        if (isLocation) {
            sendMessagesWithFile(false, true, false, object);
        }
        isPicture = false;
        isFile = false;
        isLocation = false;
        isMusic = false;
    }

    private void showBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
        view.findViewById(R.id.tv_file_pdf).setOnClickListener(this);
        view.findViewById(R.id.tv_photo_video).setOnClickListener(this);
        view.findViewById(R.id.tv_music).setOnClickListener(this);
        view.findViewById(R.id.tv_location).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            switch (requestCode) {
                case 202: {
                    uriMess = data.getData();
                    add_layout.setVisibility(View.VISIBLE);
                    add_pdf.setVisibility(View.GONE);
                    cardView.setVisibility(View.VISIBLE);
                    add_image_view.setImageURI(uriMess);
                    isPicture = true;
                    isFile = false;
                    isLocation = false;
                    isMusic = false;
                    bottomSheetDialog.cancel();
                    break;
                }
                case 201: {
                    uriMess = data.getData();
                    add_layout.setVisibility(View.VISIBLE);
                    cardView.setVisibility(View.GONE);
                    add_pdf.setVisibility(View.VISIBLE);
                    isPicture = false;
                    isFile = true;
                    isLocation = false;
                    isMusic = false;
                    bottomSheetDialog.cancel();
                }
            }
        }
    }

    private void sendMessagesWithFile(final boolean isFile, boolean isLocation, final boolean isMusic, final JSONObject object) {
        if (isLocation || isMusic) {
            //TODO
        } else {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uriMess));

            StorageTask uploadTask = fileReference.putFile(uriMess);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        final String mUri = downloadUri.toString();

                        int code = (isFile) ? 402 : 400;
                        socket.emit("messagedectectionfromsingle", code, object, mUri);
                        uriMess = null;
                    } else {
                        Toast.makeText(ChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void markAsRead() {
        socket.emit("mark_as_read", Integer.parseInt(id), Integer.parseInt(userIDFriend));
    }

}
