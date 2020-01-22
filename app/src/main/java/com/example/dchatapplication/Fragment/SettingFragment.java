package com.example.dchatapplication.Fragment;


import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.dchatapplication.Activity.ProfileActivity;

import com.example.dchatapplication.R;
import com.example.dchatapplication.Activity.StartActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {


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
        switch (v.getId()) {
            case R.id.setting_tv_account:
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.setting_tv_log_out:
                showDialog();
                break;

            default:
                break;
        }
    }


    private void showDialog(){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setMessage(getResources().getString(R.string.you_want_to_log_out));
        builder.setCancelable(false);
        builder.setBackground(getResources().getDrawable(R.drawable.alert_dialog_bg));
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), StartActivity.class));
                getActivity().finish();
            }
        });
        builder.show();
    }
}
