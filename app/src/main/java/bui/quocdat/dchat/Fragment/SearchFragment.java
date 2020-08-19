package bui.quocdat.dchat.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import bui.quocdat.dchat.Adapter.UserAdapter;
import bui.quocdat.dchat.Other.StringUtils;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<User> listUser;

    private LinearLayout linearLayoutLoaderView;

    //my server
    private Socket socket;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString(Strings.USER_ID, "");

        linearLayoutLoaderView = rootView.findViewById(R.id.linearLayout_search);
        socket = SocketManager.getInstance().getSocket();

        //search
        EditText etSearch = rootView.findViewById(R.id.search_friends_edit_text);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
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
                            mAdapter = new UserAdapter(listUser, getContext());
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

    private void searchUser(final String string) {

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
                            mAdapter = new UserAdapter(listUser, getContext());
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
