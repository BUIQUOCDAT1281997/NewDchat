package bui.quocdat.dchat.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import bui.quocdat.dchat.Activity.MainActivity;
import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Activity.StartActivity;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;


public class SignUpFragment extends Fragment {

    private EditText userName, etEmail, password;
    private Button btnRegister;
    private NavController navController;
    private LoadingDialog loadingDialog;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private DatabaseReference referenceFromOnline;

    //data online/offline
    private HashMap<String, String> hm;

    private Socket socket;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


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
        initEditText(view);

        // Button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String strUserName = userName.getText().toString();
                String strEmail = etEmail.getText().toString();
                String strPassword = password.getText().toString();

                if (TextUtils.isEmpty(strUserName) || TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
                    Toast.makeText(getActivity(), getResources()
                            .getString(R.string.All_fields_are_required), Toast.LENGTH_LONG)
                            .show();
                } else
                    register(userName.getText().toString(), etEmail.getText().toString(), password.getText().toString());
            }
        });

        //hide soft keyboard on android after clicking outside EditText
        hideKeyboard(userName);
        hideKeyboard(etEmail);
        hideKeyboard(password);
    }

    private void initEditText(View view) {
        loadingDialog = new LoadingDialog(getActivity());
        userName = view.findViewById(R.id.sig_up_user_name);
        etEmail = view.findViewById(R.id.sign_up_email);
        password = view.findViewById(R.id.sign_up_password);
        btnRegister = view.findViewById(R.id.sign_up_button_create);
        auth = FirebaseAuth.getInstance();
        socket = SocketManager.getInstance().getSocket();
        //my server
        sharedPreferences = getActivity().getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

//    private void register(final String userName, String email, final String password) {
//
//        loadingDialog.startLoadingDialog();
//
//        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    FirebaseUser firebaseUser = auth.getCurrentUser();
//                    assert firebaseUser != null;
//                    String userID = firebaseUser.getUid();
//
//                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
//
//                    HashMap<String, String> hashMap = new HashMap<>();
//                    hashMap.put("id", userID);
//                    hashMap.put("userName", userName);
//                    hashMap.put("password", password);
//                    hashMap.put("avatarURL", "default");
//                    hashMap.put("status", "Today is good day");
//                    //hashMap.put("onoroff","online");
//
//                    referenceFromOnline = FirebaseDatabase.getInstance().getReference("Status").child(userID);
//
//                    hm = new HashMap<>();
//                    hm.put("online", "online");
//                    hm.put("id",userID);
//
//                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//
//                                referenceFromOnline.setValue(hm).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            ((StartActivity) getActivity()).toMainActivity();
//                                        }
//                                    }
//                                });
//                            }
//                        }
//                    });
//                   loadingDialog.dismissDialog();
//
//                } else {
//                    Toast.makeText(getActivity(), "You can't register wrong this email or password", Toast.LENGTH_LONG).show();
//                    loadingDialog.dismissDialog();
//                }
//            }
//        });
//    }

    private void register(final String userName, String email, final String password) {

        loadingDialog.startLoadingDialog();


        socket.emit("registeruser", "fName", userName, email, "+79967632256", password)
                .on("registraioncompleted", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String id = String.valueOf((int) args[0]);
                                editor.putString(Strings.STATUS, "true");
                                editor.putString(Strings.USER_ID, id);
                                editor.apply();
                                loadingDialog.dismissDialog();
                                startActivity(new Intent(getContext(), MainActivity.class));

                                getActivity().finish();
                            }
                        });
                    }
                })
                .on("emailexists", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Already have an account to register with this email", Toast.LENGTH_LONG).show();
                                etEmail.setText("");
                                loadingDialog.dismissDialog();
                            }
                        });
                    }
                });
    }

    private void hideKeyboard(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    ((StartActivity) getActivity()).hideKeyboard(view);
                }
            }
        });
    }

    /**
     * 1. Kiem tra phan text dua vao kix hown nua
     * 2. Them acion cho phan nho mk
     */

}
