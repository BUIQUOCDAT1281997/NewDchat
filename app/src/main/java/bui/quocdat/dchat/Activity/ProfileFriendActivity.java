package bui.quocdat.dchat.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import bui.quocdat.dchat.Other.PreferenceManager;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

public class ProfileFriendActivity extends AppCompatActivity {

    private ImageView imgUser;
    private TextView tvUserName;
    private TextView tvStatus;


    private Socket socket;

    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_friend);

        id = new PreferenceManager(this).getString(Strings.USER_ID);


       /*
        //Transparent Status Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager
                    .LayoutParams
                    .FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        */

        changeStatusBarColor();

        initView();

        socket.emit("getInfOfUser", id).on("resultInfOfUser", new Emitter.Listener() {
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
                            tvUserName.setText(infUser.getString("last_name"));
                            tvStatus.setText(infUser.getString("preferences"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }

//    private void addPictureFromPost() {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pictures").child("PostPicture").child(userId);
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Picture picture = snapshot.getValue(Picture.class);
//                    listPicture.add(picture.getUrl());
//                }
//                mAdapter = new PictureAdapter(listPicture, getApplicationContext());
//                recyclerView.setAdapter(mAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void initView() {
        imgUser = findViewById(R.id.profile_friend_img);
        tvUserName = findViewById(R.id.tv_user_name);
        tvStatus = findViewById(R.id.tv_status_user_name);

        socket = SocketManager.getInstance().getSocket();

        //recyclerview
        RecyclerView recyclerView = findViewById(R.id.recycler_view_picture);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);


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
    protected void onPause() {
        super.onPause();
        setStatus(false);
    }

    private void changeStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
    }

}
