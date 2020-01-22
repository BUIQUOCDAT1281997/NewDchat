package com.example.dchatapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;


import com.example.dchatapplication.ConnectivityReceiver;
import com.example.dchatapplication.Other.MyApplication;
import com.example.dchatapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private NavController navController;

    //FireBase
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    private ConnectivityReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkConnection();

        //init view
        initFireBase();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);



    }

    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        if (isConnected) {
            findViewById(R.id.nav_host_fragment_main).setVisibility(View.VISIBLE);
            findViewById(R.id.bottom_nav).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_disconnect).setVisibility(View.GONE);

            //Toast.makeText(this,"connection", Toast.LENGTH_LONG).show();
        } else {
            findViewById(R.id.nav_host_fragment_main).setVisibility(View.GONE);
            findViewById(R.id.bottom_nav).setVisibility(View.GONE);
            findViewById(R.id.layout_disconnect).setVisibility(View.VISIBLE);

            //Toast.makeText(this,"disconnection", Toast.LENGTH_LONG).show();
        }
    }

    private void initFireBase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference = FirebaseDatabase.getInstance().getReference("Status").child(firebaseUser.getUid());

    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, (DrawerLayout) null);
    }

    private void setOnOff(String onOff){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online",onOff);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnOff("online");

        receiver = new ConnectivityReceiver();
        final IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);

        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setOnOff("offline");
        unregisterReceiver(receiver);
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}
