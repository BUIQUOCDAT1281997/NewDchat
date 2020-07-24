package bui.quocdat.dchat.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
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
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Other.StringUtils;
import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    //my server
    private Socket socket;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString(Strings.USER_ID, "");

        linearLayoutLoaderView = rootView.findViewById(R.id.linearLayout_search);
        socket = SocketManager.getInstance().getSocket();

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
//        readAllUser();
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
                            Collections.shuffle(listUser);
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

//    private void readAllUser() {
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (etSearch.getText().toString().equals("")) {
//                    listUser.clear();
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        User user = snapshot.getValue(User.class);
//                        if (!user.getId().equals(firebaseUser.getUid())) {
//                            listUser.add(user);
//                        }
//                    }
//
//                    Collections.shuffle(listUser);
//                    recyclerView.setVisibility(View.VISIBLE);
//                    linearLayoutLoaderView.setVisibility(View.GONE);
//                    mAdapter = new UserAdapter(listUser, getContext(), false);
//                    recyclerView.setAdapter(mAdapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void searchUser(final String string) {
//        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference rf = FirebaseDatabase.getInstance().getReference("Users");
//
//        rf.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listUser.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    String str = StringUtils.unAccent(user.getUserName().toLowerCase());
//                    if (str.startsWith(string)
//                            && !user.getId().equals(fuser.getUid())) {
//                        listUser.add(user);
//                    }
//                }
//
//                mAdapter = new UserAdapter(listUser, getContext(), false);
//                recyclerView.setAdapter(mAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        socket.emit("searchUser", string).on("resultSearchUser", new Emitter.Listener() {
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
                            mAdapter = new UserAdapter(listUser, getContext(), false);
                            recyclerView.setAdapter(mAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

}
