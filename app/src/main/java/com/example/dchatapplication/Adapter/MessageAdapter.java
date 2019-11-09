package com.example.dchatapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dchatapplication.Chat;
import com.example.dchatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int VIEW_TYPE_RIGHT = 1;
    public static final int VIEW_TYPE_LEFT = 0;

    private FirebaseUser firebaseUser;

    private String imageUrl;
    private List<Chat> mListData;
    private Context context;

    public MessageAdapter(List<Chat> mListData, String imageUrl, Context context) {
        this.mListData = mListData;
        this.imageUrl = imageUrl;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_right, parent, false);
            return new MessageAdapter.MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_lest, parent, false);
            return new MessageAdapter.MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Chat chat = mListData.get(position);
        holder.textView.setText(chat.getMessage());
        if (!imageUrl.equals("default") && holder.circleImageView != null) {
            Glide.with(context).load(imageUrl).into(holder.circleImageView);
        }
        if (position == mListData.size() - 1 && holder.circleImageView == null) {
            if (chat.getIsSeen().equals("true")) {
                holder.tvSeen.setText(context.getResources().getString(R.string.seen));
            } else {
                holder.tvSeen.setText(context.getResources().getString(R.string.not_seen));
            }
        } else if (holder.tvSeen != null) {
            holder.tvSeen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView circleImageView;
        public TextView textView;
        public TextView tvSeen;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.circle_view_item_chat);
            textView = itemView.findViewById(R.id.textView_item_chat);
            tvSeen = itemView.findViewById(R.id.tv_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mListData.get(position).getSender().equals(firebaseUser.getUid())) {
            return VIEW_TYPE_RIGHT;
        } else {
            return VIEW_TYPE_LEFT;
        }
    }
}
