package com.example.dchatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dchatapplication.Adapter.CommentAdapter;
import com.example.dchatapplication.Other.Comment;
import com.example.dchatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private ImageView imgSend;
    private EditText etContent;

    private RecyclerView recyclerView;
    private List<Comment> listData;
    private CommentAdapter commentAdapter;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private DatabaseReference referenceFromComment;

    private String namePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        namePost = getIntent().getStringExtra("namePost");
        int countLikes = getIntent().getIntExtra("likes", 0);
        TextView textView = findViewById(R.id.tv_comment_likes);
        textView.setText(String.format("%s likes", String.valueOf(countLikes)));

        initView();

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentText = etContent.getText().toString();
                if (!TextUtils.isEmpty(currentText)) {
                    sendComment(firebaseUser.getUid(), namePost, currentText, getCurrentTime());
                } else {
                    Toast.makeText(CommentActivity.this, "You can't send empty message", Toast.LENGTH_LONG).show();
                }
                etContent.setText("");
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                readComment();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getCurrentTime() {

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatDay = new SimpleDateFormat("dd/MM");
        String day = formatDay.format(calendar.getTime());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatHours = new SimpleDateFormat("hh:mm");
        String hours = formatHours.format(calendar.getTime());

        return hours+" "+day;
    }

    private void sendComment(String uid, String namePost, String currentText, String currentTime) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(namePost);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderID", uid);
        hashMap.put("textComment", currentText);
        hashMap.put("currentTime", currentTime);
        reference.push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("PeopleComment")
                .child(namePost)
                .child(firebaseUser.getUid());
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readComment() {
        listData = new ArrayList<>();

        referenceFromComment = FirebaseDatabase.getInstance().getReference("Comments").child(namePost);
        referenceFromComment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listData.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    listData.add(comment);
                }
                commentAdapter = new CommentAdapter(listData,CommentActivity.this);
                recyclerView.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        imgSend = findViewById(R.id.send_from_comment);
        etContent = findViewById(R.id.text_from_comment);

        //recyclerview
        recyclerView = findViewById(R.id.recycler_view_all_comment);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        //FireBase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Comments").child(namePost);
    }
}
