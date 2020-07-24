package bui.quocdat.dchat.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bui.quocdat.dchat.Adapter.UserAdapter;
import bui.quocdat.dchat.Notification.Token;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {


    private RecyclerView recyclerView;
    private View rootView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> listUser;
    private NavController navController;

    private LinearLayout linearLayoutLoaderView;

    //Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    //my server
    private Socket socket;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString(Strings.USER_ID, "");

        //init view
        initView();

        //init RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view_friends);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // app data to ListFirends
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listID.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    ChatList chatList = snapshot.getValue(ChatList.class);
//                    listID.add(chatList);
//                }
//
//                addUserToList();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        socket.emit("getAllUser", id).on("resultAllUser", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray data = (JSONArray) args[0];
                        try {
                            listUser = new ArrayList<>();
                            User user;
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject current = data.getJSONObject(i);
                                user = new User(Integer.parseInt(current.getString("id")),
                                        current.getString("phone"),
                                        current.getString("email"),
                                        current.getString("password"),
                                        current.getString("full_name"),
                                        current.getString("preferences"),
                                        current.getString("created_at"),
                                        current.getBoolean("status"),
                                        current.getString("url"));
                                listUser.add(user);
                            }
                            recyclerView.setVisibility(View.VISIBLE);
                            linearLayoutLoaderView.setVisibility(View.GONE);
                            mAdapter = new UserAdapter(listUser, getContext(), false);
                            recyclerView.setAdapter(mAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        return rootView;
    }


//    private void addUserToList() {
//        DatabaseReference drf = FirebaseDatabase.getInstance().getReference("Users");
//        drf.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listUser.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    for (ChatList chatList : listID) {
//                        if (chatList.getId().equals(user.getId())) {
//                            listUser.add(user);
//                            break;
//                        }
//                    }
//                }
//                recyclerView.setVisibility(View.VISIBLE);
//                linearLayoutLoaderView.setVisibility(View.GONE);
//                mAdapter = new UserAdapter(listUser, getContext(), true);
//                recyclerView.setAdapter(mAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


    private void initView() {
        socket = SocketManager.getInstance().getSocket();

        linearLayoutLoaderView = rootView.findViewById(R.id.linearLayout_search);
        //list user
        listUser = new ArrayList<>();
//        listID = new ArrayList<>();

        //FireBase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
    }

    private void updateToken(String strToken){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(strToken);
        reference.child(firebaseUser.getUid()).setValue(token);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        view.findViewById(R.id.img_search_user).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.img_search_user){
            navController.navigate(R.id.action_friendsFragment_to_searchFragment);
        }
    }
}
