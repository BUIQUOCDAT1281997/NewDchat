package com.example.dchatapplication.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dchatapplication.Activity.MainActivity;
import com.example.dchatapplication.Adapter.UserAdapter;
import com.example.dchatapplication.Chat;
import com.example.dchatapplication.R;
import com.example.dchatapplication.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView recyclerView;
    private View rootView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> listUser;
    private List<String> listID;

    //Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView =inflater.inflate(R.layout.fragment_friends, container, false);

        //init view
        initView(rootView);

        //init RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view_friends);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // app data to listUser
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listID.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    String currentID = firebaseUser.getUid();
                    if (chat.getReceiver().equals(currentID)&& !listID.contains(chat.getSender())){
                        listID.add(chat.getSender());
                    }
                    if (chat.getSender().equals(currentID)&&!listID.contains(chat.getReceiver())){
                        listID.add(chat.getReceiver());
                    }
                }

                writeToListUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    private void writeToListUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (listID.contains(user.getId())){
                        listUser.add(user);
                    }
                }

                mAdapter = new UserAdapter(listUser, getContext(),true);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView(View rootView) {
        //list user
        listUser = new ArrayList<>();
        listID = new ArrayList<>();

        //FireBase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
    }

}
