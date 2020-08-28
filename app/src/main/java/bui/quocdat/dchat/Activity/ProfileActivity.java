package bui.quocdat.dchat.Activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.Other.PreferenceManager;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private LoadingDialog loadingDialog;
    private ImageView imgUser;
    private TextView tvUserName;


    //storage
    private StorageReference mStorageRef;
    private static final int IMAGE_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2019;
    private static final int CAMERA = 99;
    private static final int READ_STORAGE = 1997;
    private Uri imageUri;
    private StorageTask uploadTask;

    // my server
    private Socket socket;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = new PreferenceManager(this).getString(Strings.USER_ID);

        initView();

        socket.emit("getInfOfUser", id).on("resultInfOfUser", args -> runOnUiThread(() -> {
            JSONObject infUser = (JSONObject) args[0];
            try {
                String url = infUser.getString("url");
                if (!url.isEmpty()) {
                    Glide.with(getApplicationContext())
                            .load(url)
                            .into(imgUser);
                }
                tvUserName.setText(infUser.getString("last_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));

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

        socket = SocketManager.getInstance().getSocket();

        tvUserName = findViewById(R.id.tv_user_name);
        findViewById(R.id.img_change_picture).setOnClickListener(this);

        //picture user
        imgUser = findViewById(R.id.img_user);
        imgUser.setOnClickListener(this);

        loadingDialog = new LoadingDialog(this);

        findViewById(R.id.qr_code_img).setOnClickListener(this);

        //recyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView_from_profile);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mGridLayoutManager);

        //Firebase
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child("users");
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
        uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return fileReference.getDownloadUrl();
        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                assert downloadUri != null;
                final String mUri = downloadUri.toString();

                socket.emit("updatePictureUser", mUri, id).on("success", args -> runOnUiThread(() -> Glide.with(getApplicationContext())
                        .load(mUri)
                        .into(imgUser)));

            } else {
                Toast.makeText(ProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
            loadingDialog.dismissDialog();

        }).addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
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
        builder.setItems(pictureDialogItems, (dialog, i) -> {
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
