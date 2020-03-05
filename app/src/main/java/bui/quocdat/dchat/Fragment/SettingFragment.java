package bui.quocdat.dchat.Fragment;


import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import bui.quocdat.dchat.Activity.ProfileActivity;

import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Activity.StartActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {

    private TextView tvUserName;
    private ImageView imgUser;

    //Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_setting_new, container, false);

        tvUserName = rootView.findViewById(R.id.tv_user_name);
        imgUser = rootView.findViewById(R.id.image_view_user);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                tvUserName.setText(user.getUserName());

                if (!user.getAvatarURL().equals("default")) {

                    try {
                        Glide.with(getContext())
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


        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.constrain_layout_account).setOnClickListener(this);
        view.findViewById(R.id.setting_tv_log_out).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.constrain_layout_account:
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
