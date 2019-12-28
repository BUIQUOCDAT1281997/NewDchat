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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dchatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResetPasswordFragment extends Fragment {

    private TextView tvEmail;
    private LinearLayout llProgressBar;

    private FirebaseAuth firebaseAuth;

    private NavController navController;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        tvEmail = view.findViewById(R.id.text_reset_email);
        firebaseAuth = FirebaseAuth.getInstance();
        llProgressBar = view.findViewById(R.id.llProgressBar_reset);

        view.findViewById(R.id.button_reset_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = tvEmail.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(getContext(),"All fields are requied",Toast.LENGTH_LONG).show();
                }else {
                    llProgressBar.setVisibility(View.VISIBLE);
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(getContext(),"Please check your email", Toast.LENGTH_LONG).show();
                                navController.navigate(R.id.action_resetPasswordFragment_to_signInFragment);
                            }else {
                                Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                            llProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

    }
}
