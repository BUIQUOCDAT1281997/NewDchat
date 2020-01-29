package bui.quocdat.dchat.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import bui.quocdat.dchat.Adapter.UserAdapter;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Other.StringUtils;
import bui.quocdat.dchat.Other.User;
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
public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private View rootView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> listUser;
    private EditText etSearch;

    private LinearLayout linearLayoutLoaderView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        linearLayoutLoaderView = rootView.findViewById(R.id.linearLayout_search);

        //search
        etSearch = rootView.findViewById(R.id.search_friends_edit_text);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(StringUtils.unAccent(charSequence.toString().toLowerCase()));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //list user
        listUser = new ArrayList<>();

        //init RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view_all_user);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        //Add all user to list
        readAllUser();


        return rootView;
    }

    private void readAllUser() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (etSearch.getText().toString().equals("")) {
                    listUser.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            listUser.add(user);
                        }
                    }

                    Collections.shuffle(listUser);
                    recyclerView.setVisibility(View.VISIBLE);
                    linearLayoutLoaderView.setVisibility(View.GONE);
                    mAdapter = new UserAdapter(listUser, getContext(), false);
                    recyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUser(final String string) {
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference rf = FirebaseDatabase.getInstance().getReference("Users");

        rf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    String str = StringUtils.unAccent(user.getUserName().toLowerCase());
                    if (str.startsWith(string)
                            && !user.getId().equals(fuser.getUid())) {
                        listUser.add(user);
                    }
                }

                mAdapter = new UserAdapter(listUser, getContext(), false);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Query query = FirebaseDatabase.getInstance().getReference("Users")
         *                 .orderByChild("userName")
         *                 .startAt(string)
         *                 .endAt(string +"\uf8ff");
         *
         *         query.addValueEventListener(new ValueEventListener() {
         *             @Override
         *             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
         *                 listUser.clear();
         *                 for (DataSnapshot snapshot :dataSnapshot.getChildren()){
         *                     User user = snapshot.getValue(User.class);
         *                     if (!user.getId().equals(firebaseUser.getUid())){
         *                         listUser.add(user);
         *                     }
         *                 }
         *                 mAdapter = new UserAdapter(listUser, getContext(),false);
         *                 recyclerView.setAdapter(mAdapter);
         *             }
         *
         *             @Override
         *             public void onCancelled(@NonNull DatabaseError databaseError) {
         *
         *             }
         *         });
         */
    }

}
