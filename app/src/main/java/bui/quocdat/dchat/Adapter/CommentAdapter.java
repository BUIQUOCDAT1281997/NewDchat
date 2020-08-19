package bui.quocdat.dchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import bui.quocdat.dchat.Other.Comment;
import bui.quocdat.dchat.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private Context context;

    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        if (!comment.getUrlUser().isEmpty()) {
            Glide.with(context).load(comment.getUrlUser()).placeholder(R.color.textDefaultColor).into(holder.imageUser);
        }
        holder.tvUser.setText(comment.getUserName());
        holder.tvContent.setText(comment.getText());
        holder.tvTime.setText(comment.getCreated_at());
        if (!comment.getUrl().isEmpty()) {
            holder.imageComment.setVisibility(View.VISIBLE);
            Glide.with(context).load(comment.getUrl()).placeholder(R.color.textDefaultColor).into(holder.imageComment);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView imageUser, imageComment;
        TextView tvUser;
        TextView tvContent;
        TextView tvTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageUser = itemView.findViewById(R.id.item_comment_avatar_user);
            imageComment = itemView.findViewById(R.id.item_comment_url);
            tvUser = itemView.findViewById(R.id.item_comment_user_name);
            tvContent = itemView.findViewById(R.id.tv_content_comment);
            tvTime = itemView.findViewById(R.id.item_comment_time);
        }
    }
}
