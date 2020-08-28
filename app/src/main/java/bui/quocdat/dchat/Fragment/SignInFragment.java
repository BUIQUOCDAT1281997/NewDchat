package bui.quocdat.dchat.Fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import bui.quocdat.dchat.Activity.MainActivity;
import bui.quocdat.dchat.Activity.StartActivity;
import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.Other.PreferenceManager;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment implements View.OnClickListener {

    private TextInputEditText textEmail, textPassword;

    private LoadingDialog loadingDialog;

    private Socket socket;

    private PreferenceManager preferenceManager;

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

        view.findViewById(R.id.sign_in_forgot).setOnClickListener(this);
        view.findViewById(R.id.sign_in_button).setOnClickListener(this);

        //hide soft keyboard on android after clicking outside EditText
        textEmail.setOnFocusChangeListener((view12, b) -> {
            if (!b && getActivity() != null) {
                ((StartActivity) getActivity()).hideKeyboard(view12);
            }
        });
        textPassword.setOnFocusChangeListener((view1, b) -> {
            if (!b && getActivity() != null) {
                ((StartActivity) getActivity()).hideKeyboard(view1);
            }
        });

    }

    private void initView(View view) {

        loadingDialog = new LoadingDialog(getActivity());
        textEmail = view.findViewById(R.id.sign_in_email);
        textPassword = view.findViewById(R.id.sign_in_password);

        //socket
        socket = SocketManager.getInstance().getSocket();

        //my server
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getContext()));

    }

    @Override
    public void onClick(View view) {
        int idView = view.getId();

        switch (idView) {
            case R.id.sign_in_forgot: {
//                navController.navigate(R.id.action_signInFragment_to_resetPasswordFragment);
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

    private void loginToAccount(String email, String password) {

        loadingDialog.startLoadingDialog();

        socket.emit("login", email, password).on("successful", args -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            String id = String.valueOf((int) args[0]);
            @SuppressLint("HardwareIds")
            String android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            String android_name = getDeviceName();
            if (!android_id.isEmpty() && !android_name.isEmpty()) {
                socket.emit("insertdeviceinfor", id, android_id, android_name);
            }

            preferenceManager.putBoolean(Strings.STATUS, true);
            preferenceManager.putString(Strings.USER_ID, id);
            startActivity(new Intent(getContext(), MainActivity.class));
            loadingDialog.dismissDialog();
            getActivity().finish();

        })).on("wrongpassword", args -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Wrong password !", Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                });
            }

        }).on("wrongemail", args -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "This email has not been registered", Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                });
            }
        }).on("somethingwrong", args -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Sorry ! The server is under maintenance :(", Toast.LENGTH_LONG).show();
                    loadingDialog.dismissDialog();
                });
            }
        });
    }

    //socket
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    //my server
    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
