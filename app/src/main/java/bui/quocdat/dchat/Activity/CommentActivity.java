package bui.quocdat.dchat.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bui.quocdat.dchat.Adapter.CommentAdapter;
import bui.quocdat.dchat.Other.Comment;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

public class CommentActivity extends AppCompatActivity {

    private Socket socket;
    private TextView tvSumLike;
    private List<Comment> commentList;

    private CommentAdapter commentAdapter;
    private RecyclerView recyclerView;

    private ImageView ivSend;
    private EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //init
        socket = SocketManager.getInstance().getSocket();
        final String id_post = String.valueOf(getIntent().getIntExtra("id_post", 0));
        tvSumLike = findViewById(R.id.tv_comment_likes);
        commentList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view_all_comment);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        ivSend = findViewById(R.id.send_from_comment);
        etContent = findViewById(R.id.text_from_comment);
        final String id = getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(Strings.USER_ID, "");

        // check information of post
        socket.emit("check_inf_and_list_posts", id_post).on("result_inf_and_list_posts", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int sum_like = (int) args[0];
                        tvSumLike.setText(sum_like+" likes");
                        JSONArray arrayPost = (JSONArray) args[1];
                        commentList.clear();
                        try {
                            Comment comment;
                            for (int i = 0; i < arrayPost.length(); i++) {
                                JSONObject object = arrayPost.getJSONObject(i);
                                comment = new Comment(
                                        object.getInt("id"),
                                        object.getInt("post_id"),
                                        object.getInt("user_id"),
                                        object.getString("text"),
                                        object.getString("url"),
                                        object.getString("created_at"),
                                        object.getString("urlUser"),
                                        object.getString("userName")
                                );
                                commentList.add(comment);
                            }
                            commentAdapter = new CommentAdapter(commentList, getApplicationContext());
                            recyclerView.setAdapter(commentAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = etContent.getText().toString();
                if (!text.isEmpty()) {
                    socket.emit("newComment", id_post, id, text);
                    etContent.setText("");
                }
            }
        });

        socket.on("result_newComment", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject object = (JSONObject) args[0];
                        try {
                            Comment comment = new Comment(
                                    object.getInt("id"),
                                    object.getInt("post_id"),
                                    object.getInt("user_id"),
                                    object.getString("text"),
                                    object.getString("url"),
                                    object.getString("created_at"),
                                    object.getString("urlUser"),
                                    object.getString("userName")
                            );
                            commentList.add(comment);
                            commentAdapter = new CommentAdapter(commentList, getApplicationContext());
                            recyclerView.setAdapter(commentAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

    }
}