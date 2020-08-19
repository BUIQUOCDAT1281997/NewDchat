package bui.quocdat.dchat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import bui.quocdat.dchat.Activity.ChatActivity;
import bui.quocdat.dchat.Activity.CommentActivity;
import bui.quocdat.dchat.Fragment.NewsFragment;
import bui.quocdat.dchat.Other.MyBounceInterpolator;
import bui.quocdat.dchat.Other.Post;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import bui.quocdat.dchat.Socketconnetion.SocketManager;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> listData;
    private Context mContext;
    private int id;
    private NewsFragment fragment;
    private Socket socket;

    public PostAdapter(List<Post> listData, Context mContext, NewsFragment fragment) {
        this.listData = listData;
        this.mContext = mContext;
        this.fragment = fragment;
        this.socket = SocketManager.getInstance().getSocket();
        this.id = Integer
                .parseInt(Objects.requireNonNull(mContext.getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                        .getString(Strings.USER_ID, "")));
    }

    private void didTapButton(ImageView i) {

        final Animation myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
        i.startAnimation(myAnim);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);

        i.startAnimation(myAnim);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_posts_new, parent, false);
        return new PostViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {
        final Post post = listData.get(position);

        setUserNameAndAvatar(post.getUrlUser(), post.getFullName(), holder.imageUser, holder.tvUserName);

        if (!post.getUrlPost().isEmpty()) {
            holder.imagePost.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(post.getUrlPost()).placeholder(R.color.textDefaultColor).into(holder.imagePost);
        }

        checkLike(post, holder.tvLikes, holder.tvComment, holder.iconLike);

        holder.tvTime.setText(post.getCreated_at());
        holder.tvContent.setText(post.getCaption());

        if (post.getUser_id() == id) {
            holder.iconToChat.setImageResource(R.drawable.ic_icon_delete);
            holder.iconToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(post);

                }
            });
        } else {
            holder.iconToChat.setImageResource(R.drawable.ic_icon_send_to_user);
            holder.iconToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.putExtra("userID", post.getUser_id());
                    mContext.startActivity(intent);
                }
            });
        }

        holder.iconLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Socket socket = SocketManager.getInstance().getSocket();
                socket.emit("click_like", id, post.getId()).on("result_click_like", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        if ((int) args[2]== post.getId()){
                            if (fragment.getActivity()!=null) {
                                fragment.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final int code = (int) args[0];
                                        int sum = (int) args[1];
                                        if (code == 1) {
                                            holder.iconLike.setImageResource(R.drawable.ic_icon_like_red);
                                        } else holder.iconLike.setImageResource(R.drawable.ic_icon_like_white);
                                        didTapButton(holder.iconLike);
                                        holder.tvLikes.setText(String.valueOf(sum));
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });

        holder.iconComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("id_post", post.getId());
                mContext.startActivity(intent);
            }
        });
    }

    private void showDialog(final Post post) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext);
        builder.setMessage("You sure ?");
        builder.setCancelable(false);
        builder.setTitle("Delete Post");
        builder.setIcon(mContext.getResources().getDrawable(R.drawable.ic_icon_delete));
        builder.setBackground(mContext.getResources().getDrawable(R.drawable.alert_dialog_bg));
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                socket.emit("delete_post", post.getId(), post.getMedia_id()).on("result_from_delete_post", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        fragment.reloadPosts();
                    }
                });
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return Math.min(listData.size(), 100);
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageUser;
        TextView tvUserName, tvTime, tvContent, tvLikes, tvComment;
        ImageView imagePost, iconLike, iconComment, iconToChat;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);

            imageUser = itemView.findViewById(R.id.item_comment_avatar_user);
            tvUserName = itemView.findViewById(R.id.item_news_username);
            tvTime = itemView.findViewById(R.id.item_news_current_time);
            tvContent = itemView.findViewById(R.id.item_news_text_posts);
            imagePost = itemView.findViewById(R.id.item_news_image_posts);
            iconLike = itemView.findViewById(R.id.icon_like);
            iconComment = itemView.findViewById(R.id.icon_comment);
            iconToChat = itemView.findViewById(R.id.icon_call_friend);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvComment = itemView.findViewById(R.id.counter_comment_tv);

        }
    }

    private void checkLike(final Post post, final TextView tvLikes, TextView tvComments, final ImageView ivLikes) {
        tvLikes.setText(String.valueOf(post.getSum_likes()));
        tvComments.setText(String.valueOf(post.getSum_comments()));

        socket.emit("check_like", id, post.getId()).on("result_check_like", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final int code = (int) args[0];
                if ((int)args[1]==post.getId()) {
                    if (code == 200) {
                        ivLikes.setImageResource(R.drawable.ic_icon_like_red);
                    } else ivLikes.setImageResource(R.drawable.ic_icon_like_white);
                }
            }
        });
    }


    private void setUserNameAndAvatar(String urlAvatar, String name, ImageView avatar, TextView userName) {
        if (!urlAvatar.isEmpty()) {
            Glide.with(mContext).load(urlAvatar).placeholder(R.color.textDefaultColor).into(avatar);
        }
        userName.setText(name);

    }
}
