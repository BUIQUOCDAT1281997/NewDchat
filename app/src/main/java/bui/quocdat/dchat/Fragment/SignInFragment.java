package bui.quocdat.dchat.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import bui.quocdat.dchat.Activity.MainActivity;
import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Activity.StartActivity;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment implements View.OnClickListener {

    private TextInputEditText textEmail, textPassword;
    private NavController navController;

    private LoadingDialog loadingDialog;

    private Socket socket;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;



    //Firebase
//    private FirebaseAuth auth;


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
        loadingDialog = new LoadingDialog(getActivity());
        textEmail = view.findViewById(R.id.sign_in_email);
        textPassword = view.findViewById(R.id.sign_in_password);

        //socket
        socket = SocketManager.getInstance().getSocket();

        //my server
        sharedPreferences = getActivity().getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);



//        auth = FirebaseAuth.getInstance();
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

//        auth.signInWithEmailAndPassword(email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    ((StartActivity) getActivity()).toMainActivity();
//                } else {
//                    Toast
//                            .makeText(getActivity(), getResources().getString(R.string.Authentication_failed), Toast.LENGTH_LONG)
//                            .show();
//                }
//                loadingDialog.dismissDialog();
//            }
//        });

        socket.emit("login", email, password).on("successful", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String id = String.valueOf((int) args[0]);
                        String android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
                        String android_name = getDeviceName();
                        if (!android_id.isEmpty()&&!android_name.isEmpty()) {
                            socket.emit("insertdeviceinfor",id,android_id,android_name);
                        }

                        editor = sharedPreferences.edit();
                        editor.putString(Strings.STATUS,"true");
                        editor.putString(Strings.USER_ID, id);
                        editor.apply();
                        startActivity(new Intent(getContext(), MainActivity.class));
                        loadingDialog.dismissDialog();
                        getActivity().finish();

                    }
                });
            }
        }).on("wrongpassword", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Wrong password !", Toast.LENGTH_LONG).show();
                        loadingDialog.dismissDialog();
                    }
                });

            }
        }).on("wrongemail", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "This email has not been registered", Toast.LENGTH_LONG).show();
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }).on("somethingwrong", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Sorry ! The server is under maintenance :(", Toast.LENGTH_LONG).show();
                        loadingDialog.dismissDialog();
                    }
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
