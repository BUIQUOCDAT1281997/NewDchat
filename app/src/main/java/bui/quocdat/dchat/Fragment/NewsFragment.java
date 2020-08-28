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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bui.quocdat.dchat.Adapter.PostAdapter;
import bui.quocdat.dchat.Other.Post;
import bui.quocdat.dchat.Other.PreferenceManager;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    private NavController navController;

    private List<Post> listPost;
    private PostAdapter postAdapter;

    private RecyclerView recyclerView;
    private CircleImageView imageCurrentUser;

    private LinearLayout linearLayoutLoaderView;

    private SwipeRefreshLayout swipeRefreshLayout;

    // my server
    private Socket socket;
    private String id;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_new, container, false);

        id = new PreferenceManager(getContext()).getString(Strings.USER_ID);

        initViewAndFireBase(rootView);

        //my server
        reloadPosts();
        swipeRefreshLayout.setOnRefreshListener(this::reloadPosts);

        return rootView;
    }

    public void reloadPosts() {
        swipeRefreshLayout.setRefreshing(true);
        listPost = new ArrayList<>();
        socket.emit("infAndListPosts", id).on("resultInfAndListPost", args -> {
            if (getActivity()!=null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        JSONObject infUser = (JSONObject) args[0];
                        try {
                            String url = infUser.getString("url");
                            if (!url.isEmpty()) {
                                Glide.with(getActivity())
                                        .load(url)
                                        .into(imageCurrentUser);
                            }
                            //TODO
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JSONArray listData = (JSONArray) args[1];
                        try {
                            Post post;
                            JSONObject current;
                            listPost.clear();
                            for (int i = 0; i < listData.length(); i++) {
                                current = listData.getJSONObject(i);
                                post = new Post(Integer.parseInt(current.getString("id"))
                                        , Integer.parseInt(current.getString("user_id"))
                                        , current.getString("caption")
                                        , Integer.parseInt(current.getString("media_id"))
                                        , Integer.parseInt(current.getString("sum_like"))
                                        , Integer.parseInt(current.getString("sum_comments"))
                                        , current.getString("created_at")
                                        , current.getString("urlUser")
                                        , current.getString("urlPost")
                                        , current.getString("full_name"));
                                listPost.add(post);
                            }
                            Collections.reverse(listPost);
//                            recyclerView.setVisibility(View.VISIBLE);
//                            linearLayoutLoaderView.setVisibility(View.GONE);
                            postAdapter = new PostAdapter(listPost, getContext(), NewsFragment.this);
                            recyclerView.setAdapter(postAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }



    private void initViewAndFireBase(View view) {
        linearLayoutLoaderView = view.findViewById(R.id.linearLayout_search);
        imageCurrentUser = view.findViewById(R.id.post_image_user);
        recyclerView = view.findViewById(R.id.recycler_view_all_post);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        socket = SocketManager.getInstance().getSocket();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        view.findViewById(R.id.text_view_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_newsFragment_to_createNewPostFragment);
            }
        });
        view.findViewById(R.id.to_create_new_post_from_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("data", "here_we_go");
                navController.navigate(R.id.action_newsFragment_to_createNewPostFragment, bundle);
            }
        });
    }

}
