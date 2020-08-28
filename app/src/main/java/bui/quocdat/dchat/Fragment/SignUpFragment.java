package bui.quocdat.dchat.Fragment;

import android.content.Intent;
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

import com.github.nkzawa.socketio.client.Socket;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bui.quocdat.dchat.Activity.MainActivity;
import bui.quocdat.dchat.Activity.StartActivity;
import bui.quocdat.dchat.Other.LoadingDialog;
import bui.quocdat.dchat.Other.PreferenceManager;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;


public class SignUpFragment extends Fragment {

    private EditText userName, etEmail, password, lastName, phone, again;
    private Button btnRegister;
    private LoadingDialog loadingDialog;

    private Socket socket;

    private PreferenceManager preferenceManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initEditText(view);

        // Button
        btnRegister.setOnClickListener(view1 -> {

            String strUserName = userName.getText().toString();
            String strEmail = etEmail.getText().toString();
            String strPassword = password.getText().toString();
            String strAgain = again.getText().toString();
            String strLastName = lastName.getText().toString();
            String strPhone = phone.getText().toString();

                register(strUserName, strLastName,strEmail, strPhone, strPassword, strAgain);
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
        preferenceManager = new PreferenceManager(Objects.requireNonNull(getContext()));
    }


    private void register(String fName, String lName, String mEmail, String mPhone, String mPassword, String mAgain) {

        if (checkInfo(fName, lName, mEmail, mPhone, mPassword, mAgain)){
            loadingDialog.startLoadingDialog();
            socket.emit("registeruser", fName, lName, mEmail, mPhone, mPassword)
                    .on("registraioncompleted", args -> {
                        if (getActivity()!=null) {
                            getActivity().runOnUiThread(() -> {
                                String id = String.valueOf((int) args[0]);
                                preferenceManager.putBoolean(Strings.STATUS, true);
                                preferenceManager.putString(Strings.USER_ID, id);
                                preferenceManager.putString(Strings.KEY_FIRST_NAME, fName);
                                preferenceManager.putString(Strings.KEY_LAST_NAME, lName);
                                preferenceManager.putString(Strings.KEY_EMAIL, mEmail);
                                loadingDialog.dismissDialog();
                                startActivity(new Intent(getContext(), MainActivity.class));

                                getActivity().finish();
                            });
                        }
                    })
                    .on("emailexists", args -> {
                        if (getActivity()!=null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Already have an account to register with this email", Toast.LENGTH_LONG).show();
                                etEmail.setText("");
                                loadingDialog.dismissDialog();
                            });
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
        editText.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                ((StartActivity) getActivity()).hideKeyboard(view);
            }
        });
    }
}
