package bui.quocdat.dchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import bui.quocdat.dchat.Other.Comment;
import bui.quocdat.dchat.Other.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import bui.quocdat.dchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> mListData;
    private Context context;

    public CommentAdapter(List<Comment> mListData, Context context) {
        this.mListData = mListData;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentAdapter.CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentViewHolder holder, int position) {

        final Comment comment = mListData.get(position);

        holder.tvTime.setText(comment.getCurrentTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(comment.getSenderID());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                holder.tvName.setText(user.getUserName());
                holder.tvContentComment.setText(comment.getTextComment());
                Glide.with(context).load(user.getAvatarURL()).placeholder(R.drawable.ic_user).into(holder.imgUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return mListData.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imgUser;
        TextView tvName, tvTime, tvContentComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.item_comment_avatar_user);
            tvName = itemView.findViewById(R.id.item_comment_user_name);
            tvTime = itemView.findViewById(R.id.item_comment_time);
            tvContentComment = itemView.findViewById(R.id.tv_content_comment);
        }
    }
}
