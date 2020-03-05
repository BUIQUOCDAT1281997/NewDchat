package bui.quocdat.dchat.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import bui.quocdat.dchat.Adapter.UserAdapter;
import bui.quocdat.dchat.Notification.Token;
import bui.quocdat.dchat.Other.ChatList;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Other.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {


    private RecyclerView recyclerView;
    private View rootView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> listUser;
    private List<ChatList> listID;

    private NavController navController;

    private LinearLayout linearLayoutLoaderView;

    //Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        //init view
        initView();

        //init RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view_friends);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // app data to ListFirends
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listID.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    listID.add(chatList);
                }

                addUserToList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return rootView;
    }


    private void addUserToList() {
        DatabaseReference drf = FirebaseDatabase.getInstance().getReference("Users");
        drf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (ChatList chatList : listID) {
                        if (chatList.getId().equals(user.getId())) {
                            listUser.add(user);
                            break;
                        }
                    }
                }
                recyclerView.setVisibility(View.VISIBLE);
                linearLayoutLoaderView.setVisibility(View.GONE);
                mAdapter = new UserAdapter(listUser, getContext(), true);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initView() {

        linearLayoutLoaderView = rootView.findViewById(R.id.linearLayout_search);
        //list user
        listUser = new ArrayList<>();
        listID = new ArrayList<>();

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
