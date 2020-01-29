package bui.quocdat.dchat.Fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.R;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewPostFragment extends Fragment implements View.OnClickListener {

    //storage
    private StorageReference mStorageRef;
    private StorageTask uploadTask;
    private static final int READ_STORAGE = 1000;
    private static final int IMAGE_REQUEST_CODE = 100;

    private EditText contentPost;
    private ImageView picturePost;
    private LoadingDialog loadingDialog;

    private Uri imageUri;

    private NavController navController;

    private String currentTime;

    //FireBase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        String data = getArguments().getString("data");

        assert data != null;
        if (data.equals("here_we_go")){
            requestPermission();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_new_post, container, false);


        initView(rootView);
        initFireBase();


        return rootView;
    }

    private void initView(View view){
        //Toolbar
        Toolbar toolbar = view.findViewById(R.id.chat_toolbar);
        AppCompatActivity activity = (AppCompatActivity)getActivity();

        assert activity != null;
        assert activity.getSupportActionBar() != null;
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("");
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        picturePost = view.findViewById(R.id.new_post_picture);
        loadingDialog = new LoadingDialog(getActivity());
        contentPost = view.findViewById(R.id.new_post_content);
        TextView tvPost = view.findViewById(R.id.new_post_create);
        tvPost.setOnClickListener(this);
        picturePost.setOnClickListener(this);
    }

    private void initFireBase(){
        currentTime = String.valueOf(System.currentTimeMillis());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Posts").child(currentTime+firebaseUser.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child(firebaseUser.getUid());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_post_picture:
                requestPermission();
                break;
            case R.id.new_post_create:
                if (!contentPost.getText().toString().isEmpty()){
                    uploadPost();
                }
                break;
            default:
                break;
        }
    }

    private void requestPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
            } else {
                readDataExternal();
            }
        } else {
            readDataExternal();
        }

    }

    private void readDataExternal(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data !=null && data.getData() != null){

            if (requestCode == IMAGE_REQUEST_CODE){
                imageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),imageUri);
                    picturePost.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.e("Error",e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadPost() {

        final HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("nameReference",currentTime+firebaseUser.getUid());
        hashMap.put("senderID",firebaseUser.getUid());
        hashMap.put("picturePost","default");
        hashMap.put("currentTime", getCurrentTime());
        hashMap.put("caption",contentPost.getText().toString().trim());

        loadingDialog.startLoadingDialog();

        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    if (imageUri!=null){
                        if (uploadTask != null && uploadTask.isInProgress()) {
                            Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_LONG).show();
                        } else
                            fileUploader();
                    }

                    navController.navigate(R.id.action_createNewPostFragment_to_newsFragment);

                }else {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismissDialog();
                }
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

    private void fileUploader() {
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
                    hashMap.put("picturePost", mUri);
                    reference.updateChildren(hashMap);

                    DatabaseReference toAllPicture = FirebaseDatabase
                            .getInstance()
                            .getReference("Pictures");
                    DatabaseReference refAvatar = toAllPicture.child("PostPicture").child(firebaseUser.getUid());
                    HashMap<String, Object> hmAvatar = new HashMap<>();
                    hmAvatar.put("url", mUri);
                    hmAvatar.put("timeUpload",getCurrentTime());
                    refAvatar.push().setValue(hmAvatar);

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        contentPost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }
}
