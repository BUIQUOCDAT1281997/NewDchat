package bui.quocdat.dchat.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;


import bui.quocdat.dchat.ConnectivityReceiver;
import bui.quocdat.dchat.Other.MyApplication;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

import com.github.nkzawa.socketio.client.Socket;
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

    private Socket socket;

    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        id = sharedPreferences.getString(Strings.USER_ID, "");

        checkConnection();

        socket = SocketManager.getInstance().getSocket();

        //init view
//        initFireBase();

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

    private void setStatus(Boolean isOnline) {
        if (isOnline) {
            socket.emit("setOnline", id);
        } else socket.emit("setOffline", id);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setOnOff("online");
        setStatus(true);

        receiver = new ConnectivityReceiver();
        final IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);

        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        setOnOff("offline");
        setStatus(false);

        unregisterReceiver(receiver);
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}
