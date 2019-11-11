package com.example.dchatapplication.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.dchatapplication.R;
import com.example.dchatapplication.Activity.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class SignUpFragment extends Fragment {

    private EditText userName, email, password;
    private Button btnRegister;
    private NavController navController;
    private LinearLayout llProgressBar;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private DatabaseReference referenceFromOnline;

    //data online/offline
    private HashMap<String, String> hm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        view.findViewById(R.id.tv_sign_up_number).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_signUpFragment_to_sigUpWithNumberFragment);
            }
        });
        initEditText(view);

        // Button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String strUserName = userName.getText().toString();
                String strEmail = email.getText().toString();
                String strPassword = password.getText().toString();

                if (TextUtils.isEmpty(strUserName) || TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                    Toast.makeText(getActivity(), getResources()
                            .getString(R.string.All_fields_are_required), Toast.LENGTH_LONG)
                            .show();
                } else
                    register(userName.getText().toString(), email.getText().toString(), password.getText().toString());
            }
        });

        //hide soft keyboard on android after clicking outside EditText
        hideKeyboard(userName);
        hideKeyboard(email);
        hideKeyboard(password);
    }

    private void initEditText(View view) {
        llProgressBar = view.findViewById(R.id.llProgressBar_sign_up);
        userName = view.findViewById(R.id.sig_up_user_name);
        email = view.findViewById(R.id.sign_up_email);
        password = view.findViewById(R.id.sign_up_password);
        btnRegister = view.findViewById(R.id.sign_up_button_create);
        auth = FirebaseAuth.getInstance();
    }

    private void register(final String userName, String email, final String password) {

        llProgressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userID = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userID);
                    hashMap.put("userName", userName);
                    hashMap.put("password", password);
                    hashMap.put("avatarURL", "default");
                    hashMap.put("status", "Today is good day");
                    //hashMap.put("onoroff","online");

                    referenceFromOnline = FirebaseDatabase.getInstance().getReference("Status").child(userID);

                    hm = new HashMap<>();
                    hm.put("online", "online");
                    hm.put("id",userID);

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                referenceFromOnline.setValue(hm).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            ((StartActivity) getActivity()).toMainActivity();
                                        }
                                    }
                                });
                            }
                        }
                    });
                    llProgressBar.setVisibility(View.GONE);

                } else {
                    Toast.makeText(getActivity(), "You can't register wrong this email or password", Toast.LENGTH_LONG).show();
                    llProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void hideKeyboard(EditText editText){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    ((StartActivity)getActivity()).hideKeyboard(view);
                }
            }
        });
    }

    /**
     * 1. Kiem tra phan text dua vao kix hown nua
     * 2. Them acion cho phan nho mk
     */

}
