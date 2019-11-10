package com.example.dchatapplication.Notification;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseService";

    @Override
    public void onNewToken(String s) {
        //sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(String s) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(s);
        reference.child(firebaseUser.getUid()).setValue(token);
    }


}
