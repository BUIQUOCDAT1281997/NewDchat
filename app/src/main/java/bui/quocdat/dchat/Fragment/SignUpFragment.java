package bui.quocdat.dchat.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bui.quocdat.dchat.Activity.MainActivity;
import bui.quocdat.dchat.Activity.StartActivity;
import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;


public class SignUpFragment extends Fragment {

    private EditText userName, etEmail, password, lastName, phone, again;
    private Button btnRegister;
    private NavController navController;
    private LoadingDialog loadingDialog;

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
                String strAgain = again.getText().toString();
                String strLastName = lastName.getText().toString();
                String strPhone = phone.getText().toString();

                    register(strUserName, strLastName,strEmail, strPhone, strPassword, strAgain);
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
        lastName = view.findViewById(R.id.sign_up_last_name);
        phone = view.findViewById(R.id.sign_up_phone);
        again = view.findViewById(R.id.sign_up_again);
        btnRegister = view.findViewById(R.id.sign_up_button_create);
        socket = SocketManager.getInstance().getSocket();
        //my server
        if (getActivity()!=null) {
            sharedPreferences = getActivity().getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
    }


    private void register(String fName, String lName, String mEmail, String mPhone, String mPassword, String mAgain) {

        if (checkInfo(fName, lName, mEmail, mPhone, mPassword, mAgain)){
            loadingDialog.startLoadingDialog();
            socket.emit("registeruser", fName, lName, mEmail, mPhone, mPassword)
                    .on("registraioncompleted", new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            if (getActivity()!=null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String id = String.valueOf((int) args[0]);
                                        editor = sharedPreferences.edit();
                                        editor.putString(Strings.STATUS, "true");
                                        editor.putString(Strings.USER_ID, id);
                                        editor.apply();
                                        loadingDialog.dismissDialog();
                                        startActivity(new Intent(getContext(), MainActivity.class));

                                        getActivity().finish();
                                    }
                                });
                            }
                        }
                    })
                    .on("emailexists", new Emitter.Listener() {
                        @Override
                        public void call(final Object... args) {
                            if (getActivity()!=null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "Already have an account to register with this email", Toast.LENGTH_LONG).show();
                                        etEmail.setText("");
                                        loadingDialog.dismissDialog();
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private boolean checkInfo(String fName, String lName, String mEmail, String mPhone, String mPassword, String mAgain) {
        if (fName.isEmpty()||lName.isEmpty()||mEmail.isEmpty()||mPassword.isEmpty()||mAgain.isEmpty()){
            Toast.makeText(getContext(), "Please enter full information", Toast.LENGTH_LONG).show();
            return false;
        }

        // check number phone
        Pattern p = Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]$");
        Matcher m = p.matcher(mPhone);
        if (!mPhone.isEmpty()&&!(m.matches())){
            Toast.makeText(getContext(), "is invalid Phone number", Toast.LENGTH_LONG).show();
            return false;
        }

        //check validate the password
        p = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,15})");
        m = p.matcher(mPassword);
        if (!m.matches()){
            Toast.makeText(getContext(),
                    "Must contain one digit, one lower case char, one upper case char, some special chars, length should be within 6 to 15 chars.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        if (!mPassword.equals(mAgain)){
            Toast.makeText(getContext(), "Retype the password incorrectly", Toast.LENGTH_LONG).show();
            return false;
        }

        //check email
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mEmail);
        if (!matcher.matches()){
            Toast.makeText(getContext(), "is invalid email", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;

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
}
