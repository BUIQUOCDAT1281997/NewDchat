package bui.quocdat.dchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import bui.quocdat.dchat.Adapter.PictureAdapter;
import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.Other.Picture;
import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.R;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private List<String> listPicture;

    private LoadingDialog loadingDialog;
    private ImageView imgUser;
    private TextView tvUserName;

    //recyclerView
    private RecyclerView.Adapter mAdapter;
    private RecyclerView recyclerView;


    //storage
    private StorageReference mStorageRef;
    private static final int IMAGE_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2019;
    private static final int CAMERA = 99;
    private static final int READ_STORAGE = 1997;
    private Uri imageUri;
    private StorageTask uploadTask;

    //Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();

        setupPictureRecyclerview();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                //activity.getSupportActionBar().setTitle(user.getUserName());
                tvUserName.setText(user.getUserName());

                if (!user.getAvatarURL().equals("default")) {

                    try {
                        Glide.with(getApplicationContext())
                                .load(user.getAvatarURL())
                                .into(imgUser);
                    }catch (NullPointerException ignored){
                        Log.e("Error", ignored.getMessage());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {

        /*
        //Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar_main);
        activity = (AppCompatActivity)getActivity();
        assert activity != null;
        assert activity.getSupportActionBar() != null;
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextAppearance(getContext(), R.style.ToolbarTheme);
         */

        tvUserName = findViewById(R.id.tv_user_name);
        findViewById(R.id.img_change_picture).setOnClickListener(this);

        //picture user
        imgUser = findViewById(R.id.img_user);
        imgUser.setOnClickListener(this);

        loadingDialog = new LoadingDialog(this);

        findViewById(R.id.qr_code_img).setOnClickListener(this);

        //recyclerview
        recyclerView = findViewById(R.id.recyclerView_from_profile);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mGridLayoutManager);

        listPicture= new ArrayList<>();

        //Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child(firebaseUser.getUid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {

            switch (requestCode) {
                case IMAGE_REQUEST_CODE: {
                    imageUri = data.getData();

                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(this, "Upload in progress", Toast.LENGTH_LONG).show();
                    } else
                        fileUploader();

                    break;
                }
                case CAMERA: {

                    // TODO
                    break;
                }

                default:
                    break;
            }
        }
    }

    private void fileUploader() {

        loadingDialog.startLoadingDialog();

        final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                + "." + getFileExtension(imageUri));

        uploadTask = fileReference.putFile(imageUri);
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
                    String mUri = downloadUri.toString();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("avatarURL", mUri);
                    reference.updateChildren(hashMap);

                    DatabaseReference toAllPicture = FirebaseDatabase
                            .getInstance()
                            .getReference("Pictures");
                    DatabaseReference refAvatar = toAllPicture.child("AvatarPicture").child(firebaseUser.getUid());
                    HashMap<String, Object> hmAvatar = new HashMap<>();
                    hmAvatar.put("url", mUri);
                    hmAvatar.put("timeUpload","default");
                    refAvatar.push().setValue(hmAvatar);

                } else {
                    Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismissDialog();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void showPictureDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Select Action");
        builder.setBackground(getResources().getDrawable(R.drawable.alert_dialog_bg));
        String[] pictureDialogItems = {"Select photo from gallery","Capture photo from camera"};
        builder.setItems(pictureDialogItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i){
                    case 0 :{
                        requestPermission(true);
                        break;
                    }
                    case 1 : {
                        requestPermission(false);
                        break;
                    }
                }
            }
        });
        builder.show();
    }

    private void requestPermission(boolean fromGallery){

        if (fromGallery){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
                }else {
                    readDataExternal();
                }
            }else {
                readDataExternal();
            }
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
                }else {
                    takePhotoFromCamera();
                }
            }else {
                takePhotoFromCamera();
            }
        }
    }

    private void readDataExternal(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }


    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
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

    private void setupPictureRecyclerview() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Pictures").child("AvatarPicture").child(firebaseUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pictures").child("PostPicture").child(firebaseUser.getUid());
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

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.img_change_picture){
            showPictureDialog();
        }
        if (view.getId()==R.id.qr_code_img){

            //TODO permission for camera

            Intent intent = new Intent(ProfileActivity.this, QRActivity.class);
            startActivity(intent);
        }
    }
}
