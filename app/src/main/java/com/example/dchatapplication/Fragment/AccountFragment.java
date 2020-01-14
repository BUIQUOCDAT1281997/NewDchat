package com.example.dchatapplication.Fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dchatapplication.Other.LoadingDialog;
import com.example.dchatapplication.R;
import com.example.dchatapplication.Other.User;
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

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment implements View.OnClickListener {

    private TextView tvUserName, tvStatus;
    private ImageView imgUser;
    private LoadingDialog loadingDialog;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //views
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        initView(rootView);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                tvUserName.setText(user.getUserName());
                tvStatus.setText(user.getStatus());
                if (!user.getAvatarURL().equals("default")) {

                    try {
                        Glide.with(getActivity().getApplicationContext())
                                .load(user.getAvatarURL())
                                .placeholder(R.drawable.ic_user)
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

        return rootView;
    }


    private void initView(View view) {
        //view
        tvUserName = view.findViewById(R.id.account_user_name);
        tvStatus = view.findViewById(R.id.account_status);
        imgUser = view.findViewById(R.id.profile_image);
        loadingDialog = new LoadingDialog(getActivity());

        //Firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child(firebaseUser.getUid());
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgUser.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.profile_image) {
            showPictureDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data !=null && data.getData() != null){

            switch (requestCode){
                case IMAGE_REQUEST_CODE :{
                    imageUri = data.getData();

                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_LONG).show();
                    } else
                        fileUploader();

                    break;
                }
                case CAMERA : {

                    // TODO
                    break;
                }

                default: break;
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

                } else {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismissDialog();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void showPictureDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
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
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
                }else {
                    readDataExternal();
                }
            }else {
                readDataExternal();
            }
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
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

}
