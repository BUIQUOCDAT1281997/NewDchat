package com.example.dchatapplication.Fragment;


import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
public class SignInFragment extends Fragment implements View.OnClickListener {

    private EditText textEmail, textPassword;
    private NavController navController;

    private LinearLayout llProgressBar;


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

        initView(view);

        view.findViewById(R.id.sign_in_with_number_phone).setOnClickListener(this);
        view.findViewById(R.id.sign_in_forgot).setOnClickListener(this);
        view.findViewById(R.id.sign_in_button).setOnClickListener(this);

        //hide soft keyboard on android after clicking outside EditText
        textEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    ((StartActivity)getActivity()).hideKeyboard(view);
                }
            }
        });
        textPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b){
                    ((StartActivity)getActivity()).hideKeyboard(view);
                }
            }
        });

    }

    private void initView(View view) {

        navController = Navigation.findNavController(view);
        llProgressBar = view.findViewById(R.id.llProgressBar_sign_in);
        textEmail = view.findViewById(R.id.sign_in_email);
        textPassword = view.findViewById(R.id.sign_in_password);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        int idView = view.getId();

        switch (idView) {
            case R.id.sign_in_with_number_phone: {
                navController.navigate(R.id.action_signInFragment_to_signInWithNumberFragment);
                break;
            }
            case R.id.sign_in_forgot: {
                navController.navigate(R.id.action_signInFragment_to_resetPasswordFragment);
                break;
            }
            case R.id.sign_in_button: {
                String strEmail = textEmail.getText().toString();
                String strPassword = textPassword.getText().toString();

                if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_LONG).show();
                } else {
                    loginToAccount(strEmail, strPassword);
                }

                break;
            }
        }
    }

    private void loginToAccount(String email, String Password) {

        llProgressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    ((StartActivity) getActivity()).toMainActivity();
                } else {
                    Toast
                            .makeText(getActivity(), getResources().getString(R.string.Authentication_failed), Toast.LENGTH_LONG)
                            .show();
                }
                llProgressBar.setVisibility(View.GONE);
            }
        });
    }

}
