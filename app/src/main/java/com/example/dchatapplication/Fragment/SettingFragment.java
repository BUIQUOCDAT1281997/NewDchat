package com.example.dchatapplication.Fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dchatapplication.Activity.ProfileActivity;
import com.example.dchatapplication.R;
import com.example.dchatapplication.Activity.StartActivity;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment implements View.OnClickListener{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.setting_tv_account).setOnClickListener(this);
        view.findViewById(R.id.setting_tv_log_out).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.setting_tv_account){
            Intent intent = new Intent(getContext(), ProfileActivity.class);
            getActivity().startActivity(intent);
        }
        if (id == R.id.setting_tv_log_out){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), StartActivity.class));
            getActivity().finish();
        }
    }
}
