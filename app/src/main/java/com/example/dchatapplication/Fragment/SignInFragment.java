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
import android.widget.EditText;
import android.widget.Toast;

import com.example.dchatapplication.R;
import com.example.dchatapplication.Activity.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment {

    private EditText textEmail , textPassword;
    private Button btnLogin;
    private NavController navController;

    //Firebase
    private FirebaseAuth auth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController= Navigation.findNavController(view);
        view.findViewById(R.id.sign_in_with_number_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_signInFragment_to_signInWithNumberFragment);
            }
        });
        initView(view);

        //Button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = textEmail.getText().toString();
                String strPassword = textPassword.getText().toString();

                if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword))
                    Toast.makeText(getActivity(),"All fileds are required", Toast.LENGTH_LONG).show();
                else
                    loginToAccount(strEmail, strPassword);
            }
        });
    }

    private void initView(View view) {
        textEmail = view.findViewById(R.id.sign_in_email);
        textPassword = view.findViewById(R.id.sign_in_password);
        btnLogin = view.findViewById(R.id.sign_in_button);

        //Firebase
        auth = FirebaseAuth.getInstance();
    }

    private void loginToAccount(String email,String Password){
        auth.signInWithEmailAndPassword(email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    ((StartActivity)getActivity()).toMainActivity();
                }else
                    Toast.makeText(getActivity(),"Authentication failed", Toast.LENGTH_LONG).show();
            }
        });
    }

}
