package com.example.dchatapplication.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.dchatapplication.Activity.ChatActivity;
import com.example.dchatapplication.Activity.CommentActivity;
import com.example.dchatapplication.Activity.ShowPictureActivity;
import com.example.dchatapplication.Other.Like;
import com.example.dchatapplication.Other.MyBounceInterpolator;
import com.example.dchatapplication.Other.Post;
import com.example.dchatapplication.Other.User;
import com.example.dchatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> listData;
    private Context mContext;
    private boolean isMyPosts;

    public PostAdapter(List<Post> listData, Context mContext, boolean isMyPosts) {
        this.listData = listData;
        this.mContext = mContext;
        this.isMyPosts = isMyPosts;
    }

    public void didTapButton(ImageView i) {

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
                .inflate(R.layout.item_posts, parent, false);
        return new PostViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {
        final Post post = listData.get(position);

        setUserNameAndAvatar(post.getSenderID(),holder.imageUser,holder.tvUserName);

        if (!post.getPicturePost().equals("default")) {
            holder.imagePost.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(post.getPicturePost()).placeholder(R.drawable.ic_insert_photo_black_24dp).into(holder.imagePost);
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

        holder.tvTime.setText(post.getCurrentTime());
        holder.tvContent.setText(post.getCaption());

        if (isMyPosts){
            holder.iconToChat.setImageResource(R.drawable.ic_icon_delete);
            holder.iconToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(post.getCurrentTime());
                    databaseReference.removeValue();
                }
            });
        }else {
            holder.iconToChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.putExtra("userID", post.getSenderID());
                    mContext.startActivity(intent);
                }
            });
        }

        holder.tvLikes.setText(post.getCounterLikes()+" likes");
        checkLike(holder.iconLike, post,holder.tvLikes);


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

    @Override
    public int getItemCount() {
        if (listData.size()>100) return 100;
        else return listData.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageUser;
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

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("PeopleLike");
        DatabaseReference refFromPost = databaseReference.child(post.getNameReference());

        refFromPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                boolean youLiked = false;
                final int childCount = (int) dataSnapshot.getChildrenCount();
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
                            tvLikes.setText(childCount +" likes");
                        }
                    });

                }else {
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imageView.setImageResource(R.drawable.ic_icon_like_red);
                            didTapButton(imageView);
                           addLikes(post.getNameReference(),childCount);
                           imageView.setEnabled(false);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void addLikes(final String namePost, final int counterLikes){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
                    Glide.with(mContext).load(user.getAvatarURL()).placeholder(R.drawable.ic_insert_photo_black_24dp).into(avatar);
                }

                userName.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
