package com.example.dchatapplication.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dchatapplication.Other.User;
import com.example.dchatapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFriendActivity extends AppCompatActivity {

    private ImageView imgUser;
    private TextView tvUserName;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_friend);

        //Transparent Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager
                    .LayoutParams
                    .FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        initView();

        final String userId = getIntent().getStringExtra("userID");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if (user.getId().equals(userId)){
                        //imgUser
                        if (!user.getAvatarURL().equals("default")){
                            try {
                                Glide.with(getApplicationContext())
                                        .load(user.getAvatarURL())
                                        .placeholder(R.drawable.ic_user)
                                        .into(imgUser);
                            }catch (NullPointerException e){
                                Toast.makeText(getApplicationContext(),"failed", Toast.LENGTH_LONG).show();
                            }
                        }
                        //tvUserName
                        tvUserName.setText(user.getUserName());
                        tvStatus.setText(user.getStatus());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        imgUser = findViewById(R.id.profile_friend_img);
        tvUserName = findViewById(R.id.profile_friend_user_name);
        tvStatus = findViewById(R.id.profile_friend_status);
    }
}
