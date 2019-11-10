package com.example.dchatapplication.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


import android.os.Bundle;


import com.example.dchatapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private NavController navController;

    //FireBase
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init view
        initFireBase();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

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
        //        hashMap.put("onorof",onOff);


        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOnOff("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setOnOff("offline");
    }
}
