package bui.quocdat.dchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import bui.quocdat.dchat.Other.Message;
import bui.quocdat.dchat.Other.Strings;
import bui.quocdat.dchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int VIEW_TYPE_RIGHT = 1;
    public static final int VIEW_TYPE_LEFT = 0;

    private String imageUrl;
    private List<Message> mListData;
    private Context context;

    public MessageAdapter(List<Message> mListData, String imageUrl, Context context) {
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
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_lest, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mListData.get(position);
        holder.textView.setText(message.getText());
        if (!imageUrl.isEmpty() && holder.circleImageView != null) {
            Glide.with(context).load(imageUrl).into(holder.circleImageView);
        }

    }

    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

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
        if (String.valueOf(mListData.get(position).getSender_id()).equals(context.getSharedPreferences(Strings.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(Strings.USER_ID, ""))) {
            return VIEW_TYPE_RIGHT;
        } else {
            return VIEW_TYPE_LEFT;
        }
    }
}
