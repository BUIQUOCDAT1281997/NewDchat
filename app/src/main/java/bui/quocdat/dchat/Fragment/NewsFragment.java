package bui.quocdat.dchat.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;

import bui.quocdat.dchat.Adapter.PostAdapter;
import bui.quocdat.dchat.Other.Post;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    //FireBase
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceFromPosts;

    // my server
    private Socket socket;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_new, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString(Strings.USER_ID, "");

        initViewAndFireBase(rootView);


        //load image to avatarUser
//        DatabaseReference referenceFromUsers = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//        referenceFromUsers.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//                if (!user.getAvatarURL().equals("default")) {
//                    try {
//                        Glide.with(getActivity().getApplicationContext())
//                                .load(user.getAvatarURL())
//                                .placeholder(R.drawable.ic_user)
//                                .into(imageCurrentUser);
//                    }catch (NullPointerException ignored){
//                        Log.e("Error", ignored.getMessage());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


//        readAllPost();

        //my server
        socket.emit("infAndListPosts", id).on("resultInfAndListPost", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                            for (int i = 0; i < listData.length(); i++) {
                                current = listData.getJSONObject(i);
                                post = new Post(Integer.parseInt(current.getString("id"))
                                        , Integer.parseInt(current.getString("user_id"))
                                        , current.getString("caption")
                                        , Integer.parseInt(current.getString("media_id"))
                                        , Integer.parseInt(current.getString("sum_like"))
                                        , Integer.parseInt(current.getString("sum_comments"))
                                        , current.getString("urlUser")
                                        , current.getString("urlPost")
                                        , current.getString("full_name")
                                        , current.getString("created_at"));
                                listPost.add(post);
                            }
                            Collections.reverse(listPost);
                            recyclerView.setVisibility(View.VISIBLE);
                            linearLayoutLoaderView.setVisibility(View.GONE);
                            postAdapter = new PostAdapter(listPost, getContext(), false);
                            recyclerView.setAdapter(postAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        return rootView;
    }


    private void initViewAndFireBase(View view) {
        linearLayoutLoaderView = view.findViewById(R.id.linearLayout_search);
        imageCurrentUser = view.findViewById(R.id.post_image_user);
        recyclerView = view.findViewById(R.id.recycler_view_all_post);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        socket = SocketManager.getInstance().getSocket();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceFromPosts = FirebaseDatabase.getInstance().getReference("Posts");

        listPost = new ArrayList<>();
    }

//    private void readAllPost(){
//
//        listData.clear();
//
//        referenceFromPosts.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listData.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Post post = snapshot.getValue(Post.class);
//                    /*
//                    if ((!post.getSenderID().equals(firebaseUser.getUid()))){
//                        listData.add(post);
//                    }
//                     */
//                    listData.add(post);
//                }
//                Collections.reverse(listData);
//                recyclerView.setVisibility(View.VISIBLE);
//                linearLayoutLoaderView.setVisibility(View.GONE);
//                postAdapter = new PostAdapter(listData, getContext(), false);
//                recyclerView.setAdapter(postAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

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
