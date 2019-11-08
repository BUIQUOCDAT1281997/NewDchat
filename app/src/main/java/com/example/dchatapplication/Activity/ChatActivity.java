package com.example.dchatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dchatapplication.Adapter.MessageAdapter;
import com.example.dchatapplication.Chat;
import com.example.dchatapplication.R;
import com.example.dchatapplication.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private CircleImageView imgUser;
    private TextView useName;

    //evenLister
    private ValueEventListener eventListener;

    //FireBase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private DatabaseReference referenceFromChats;


    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Chat> listData;
    private MessageAdapter messageAdapter;

    private ImageView btn_send;
    private EditText texSend;

    private String userIDFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //initView
        initView();

        //Toolbar
        Toolbar toolbar =findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recyclerview
        recyclerView = findViewById(R.id.recycler_view_message);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //get data
        Intent intent = getIntent();
        userIDFriend = intent.getStringExtra("userID");

        // set Even click
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String currentText = texSend.getText().toString();
                if (!TextUtils.isEmpty(currentText)){
                    sendMessage(firebaseUser.getUid(),userIDFriend,currentText);
                }else {
                    Toast.makeText(ChatActivity.this,"You can't send empty message",Toast.LENGTH_LONG).show();
                }
                texSend.setText("");
            }
        });

        // work with toolbar and read message
        DatabaseReference referenceForReadMessage = reference.child(userIDFriend);
        referenceForReadMessage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                useName.setText(user.getUserName());
                if (!user.getAvatarURL().equals("default")){
                    Glide.with(getApplicationContext()).load(user.getAvatarURL()).into(imgUser);
                }

                readMessage(firebaseUser.getUid(),userIDFriend,user.getAvatarURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userIDFriend);

    }



    private void initView() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        referenceFromChats = FirebaseDatabase.getInstance().getReference("Chats");
        btn_send = findViewById(R.id.button_send);
        texSend = findViewById(R.id.text_send);
        imgUser = findViewById(R.id.circle_view_chat);
        useName = findViewById(R.id.chat_user_name);

    }


    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isSeen",false);
        reference.child("Chats").push().setValue(hashMap);

        // gui neu khong co mang addOnComplete
    }


    private void readMessage(final String myId, final String userID, final String imageUrl){
        listData = new ArrayList<>();

        DatabaseReference referenceFromChats = FirebaseDatabase.getInstance().getReference("Chats");
        referenceFromChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listData.clear();
                for (DataSnapshot snapshot :dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if ((chat.getSender().equals(myId)&&chat.getReceiver().equals(userID))
                            ||(chat.getSender().equals(userID)&&chat.getReceiver().equals(myId))){
                        listData.add(chat);
                    }
                }
                messageAdapter= new MessageAdapter(listData,imageUrl,ChatActivity.this);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setOnOff(String onOff){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("onoroff",onOff);

        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());

        databaseReference.updateChildren(hashMap);
    }

    private void seenMessage(final String userIDFriends){
        eventListener = referenceFromChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid())&&chat.getSender().equals(userIDFriends)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen",true);
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
        setOnOff("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        referenceFromChats.removeEventListener(eventListener);
        setOnOff("offline");
    }
}
