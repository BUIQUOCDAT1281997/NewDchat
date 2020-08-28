package bui.quocdat.dchat.Fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import bui.quocdat.dchat.Activity.ProfileActivity;
import bui.quocdat.dchat.Activity.StartActivity;
import bui.quocdat.dchat.Other.PreferenceManager;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {

    private TextView tvUserName;
    private ImageView imgUser;


    private PreferenceManager preferenceManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_setting_new, container, false);

        preferenceManager = new PreferenceManager(getContext());
        String id = preferenceManager.getString(Strings.USER_ID);

        tvUserName = rootView.findViewById(R.id.tv_user_name);
        imgUser = rootView.findViewById(R.id.image_view_user);

        // my server
        Socket socket = SocketManager.getInstance().getSocket();

        socket.emit("getInfOfUser", id).on("resultInfOfUser", args -> {
            if (getActivity()!=null) {
                getActivity().runOnUiThread(() -> {
                    JSONObject infUser = (JSONObject) args[0];
                    try {
                        String url = infUser.getString("url");
                        if (!url.isEmpty()) {
                            Glide.with(getActivity())
                                    .load(url)
                                    .into(imgUser);
                        }
                        tvUserName.setText(infUser.getString("last_name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
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
                Objects.requireNonNull(getActivity()).startActivity(intent);
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
        builder.setPositiveButton("Cancel", (dialog, which) -> {

        });
        builder.setNegativeButton("OK", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            preferenceManager.clearPreferences();
            startActivity(new Intent(getActivity(), StartActivity.class));
            Objects.requireNonNull(getActivity()).finish();
        });
        builder.show();
    }
}
