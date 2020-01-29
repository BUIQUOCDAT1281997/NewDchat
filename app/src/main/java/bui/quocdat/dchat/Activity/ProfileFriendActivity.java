package bui.quocdat.dchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import bui.quocdat.dchat.Adapter.PictureAdapter;
import bui.quocdat.dchat.Other.Picture;
import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.R;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFriendActivity extends AppCompatActivity {

    private ImageView imgUser;
    private TextView tvUserName;
    private TextView tvStatus;

    private List<String> listPicture;

    private StorageReference mStorageRef;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;

    private String userId;

    private int count;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_friend);


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

        setupPictureRecyclerview();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getId().equals(userId)) {
                        //imgUser
                        if (!user.getAvatarURL().equals("default")) {
                            try {
                                Glide.with(getApplicationContext())
                                        .load(user.getAvatarURL())
                                        .placeholder(R.drawable.ic_user)
                                        .into(imgUser);
                            } catch (NullPointerException e) {
                                Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG).show();
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

    private void setupPictureRecyclerview() {

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    listPicture.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Picture picture = snapshot.getValue(Picture.class);
                        listPicture.add(picture.getUrl());
                    }

                    addPictureFromPost();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void addPictureFromPost() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pictures").child("PostPicture").child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Picture picture = snapshot.getValue(Picture.class);
                    listPicture.add(picture.getUrl());
                }
                mAdapter = new PictureAdapter(listPicture, getApplicationContext());
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupPictureRecyclerviewWithUrl() {
        mStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        count = 0;
                        for (StorageReference item : listResult.getPrefixes()) {
                            count++;
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    listPicture.add(uri.toString());

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileFriendActivity.this, "Error get Download Url!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        Toast.makeText(ProfileFriendActivity.this, count+"", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileFriendActivity.this, "Download error!", Toast.LENGTH_LONG).show();
            }
        });

        mAdapter = new PictureAdapter(listPicture, this);
        recyclerView.setAdapter(mAdapter);

    }

    private void setupPictureRecyclerviewWithBytes() {

        listPicture.clear();
        mStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        count = 0;

                        for (StorageReference item : listResult.getItems()) {
                            final long ONE_MEGABYTE = 1024 * 1024;
                            item.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    count++;
                                }
                            });
                        }

                        Toast.makeText(ProfileFriendActivity.this, count+"", Toast.LENGTH_LONG).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileFriendActivity.this, "Download error!", Toast.LENGTH_LONG).show();
            }
        });

        mAdapter = new PictureAdapter(listPicture, this);
        recyclerView.setAdapter(mAdapter);

    }

    private void initView() {
        imgUser = findViewById(R.id.profile_friend_img);
        tvUserName = findViewById(R.id.tv_user_name);
        tvStatus = findViewById(R.id.tv_status_user_name);

        //firebase
        userId = getIntent().getStringExtra("userID");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child(userId);

        //recyclerview
        recyclerView = findViewById(R.id.recycler_view_picture);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        //Database referent
        reference = FirebaseDatabase.getInstance().getReference("Pictures").child("AvatarPicture").child(userId);

        listPicture= new ArrayList<>();

    }

    private void setOnOff(String onOff) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", onOff);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("Status")
                .child(firebaseUser.getUid());

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnOff("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setOnOff("offline");
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
    }

}
