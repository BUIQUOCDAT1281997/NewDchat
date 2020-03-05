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
import bui.quocdat.dchat.Activity.ChatActivity;
import bui.quocdat.dchat.Activity.CommentActivity;
import bui.quocdat.dchat.Activity.ShowPictureActivity;
import bui.quocdat.dchat.Other.Like;
import bui.quocdat.dchat.Other.MyBounceInterpolator;
import bui.quocdat.dchat.Other.Post;
import bui.quocdat.dchat.Other.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import bui.quocdat.dchat.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> listData;
    private Context mContext;
    private boolean isMyPosts;
    private int childCount = 0;
    private FirebaseUser firebaseUser;

    public PostAdapter(List<Post> listData, Context mContext, boolean isMyPosts) {
        this.listData = listData;
        this.mContext = mContext;
        this.isMyPosts = isMyPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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

        setUserNameAndAvatar(post.getSenderID(),holder.imageUser,holder.tvUserName);

        if (!post.getPicturePost().equals("default")) {
            holder.imagePost.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(post.getPicturePost()).placeholder(R.color.textDefaultColor).into(holder.imagePost);
            holder.imagePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ShowPictureActivity.class);
                    intent.putExtra("pictureURL", post.getPicturePost());
                    mContext.startActivity(intent);
                }
            });
        }else {
            holder.imagePost.setVisibility(View.GONE);
        }

        checkLike(holder.iconLike, post,holder.tvLikes);

        holder.tvTime.setText(post.getCurrentTime());
        holder.tvContent.setText(post.getCaption());

        if (isMyPosts || post.getSenderID().equals(firebaseUser.getUid())){
            holder.iconToChat.setImageResource(R.drawable.ic_icon_delete);
            holder.iconToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(post);

                }
            });
        }else {
            holder.iconToChat.setImageResource(R.drawable.ic_icon_send_to_user);
            holder.iconToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.putExtra("userID", post.getSenderID());
                    intent.putExtra("likes", childCount);
                    mContext.startActivity(intent);
                }
            });
        }

        // Cai nay de tu tu
        holder.iconComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("namePost", post.getNameReference());
                mContext.startActivity(intent);
            }
        });
    }

    private void showDialog(final Post post){
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
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(post.getNameReference());
                databaseReference.removeValue();
                DatabaseReference referenceFromPeopleLike = FirebaseDatabase.getInstance().getReference("PeopleLike").child(post.getNameReference());
                referenceFromPeopleLike.removeValue();
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        if (listData.size()>100) return 100;
        else return listData.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageUser;
        TextView tvUserName , tvTime , tvContent, tvLikes;
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

        }
    }

    private void checkLike(final ImageView imageView, final Post post, final TextView tvLikes){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PeopleLike");
        DatabaseReference refFromPost = databaseReference.child(post.getNameReference());

        refFromPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                boolean youLiked = false;
               childCount = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Like like = snapshot.getValue(Like.class);
                    if (like.getUserID().equals(firebaseUser.getUid())){
                        youLiked = true;
                    }
                }
                if (youLiked){
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageResource(R.drawable.ic_icon_like_red);
                        }
                    });

                }else {
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imageView.setImageResource(R.drawable.ic_icon_like_red);
                            didTapButton(imageView);
                           addLikes(post.getNameReference());
                           imageView.setEnabled(false);
                        }
                    });
                }
                tvLikes.setText(String.valueOf(childCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void addLikes(final String namePost){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PeopleLike")
                .child(namePost)
                .child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    reference.child("userID").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUserNameAndAvatar(String userID, final ImageView avatar, final TextView userName){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (!user.getAvatarURL().equals("default")){
                    Glide.with(mContext).load(user.getAvatarURL()).placeholder(R.color.textDefaultColor).into(avatar);
                }

                userName.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
