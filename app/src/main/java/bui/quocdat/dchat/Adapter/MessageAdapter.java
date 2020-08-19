package bui.quocdat.dchat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.card.MaterialCardView;

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
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        final Message message = mListData.get(position);
        holder.textView.setText(message.getText());
        if (!imageUrl.isEmpty() && holder.circleImageView != null) {
            Glide.with(context).load(imageUrl).into(holder.circleImageView);
        }
        if (!message.getUrl().isEmpty() && message.getType().equals("picture")){
            holder.cardView.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getUrl()).into(holder.iv_url);
        } else if (message.getType().equals("pdf")) {
            holder.cardView.setVisibility(View.VISIBLE);
            holder.iv_url.setImageResource(R.drawable.ic_pdf);
            holder.iv_url.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getUrl()));
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }

        if (holder.tvSeen!=null && message.isSeen()) {
            holder.green_dot.setVisibility(View.GONE);
        }

        if (position==mListData.size()-1 && holder.tvSeen!=null) {
            holder.tvSeen.setVisibility(View.VISIBLE);
            if (message.isSeen()) {
                holder.tvSeen.setText("Seen");
            }
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
        public MaterialCardView cardView;
        public ImageView iv_url;
        public ImageView green_dot;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.circle_view_item_chat);
            textView = itemView.findViewById(R.id.textView_item_chat);
            tvSeen = itemView.findViewById(R.id.tv_seen);
            cardView = itemView.findViewById(R.id.item_chat_card_view);
            iv_url = itemView.findViewById(R.id.item_chat_image);
            green_dot = itemView.findViewById(R.id.imageView10);
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
