package com.example.dchatapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dchatapplication.Activity.ChatActivity;
import com.example.dchatapplication.Chat;
import com.example.dchatapplication.R;
import com.example.dchatapplication.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> mDataAllUser;
    private Context mContext;
    private boolean isFriends;
    private String theLastMessage;

    // ViewHolder
    public class UserViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView dotOn;
        public CircleImageView imgUser;
        public TextView tvUserName, tvStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            this.dotOn = itemView.findViewById(R.id.status_on_off);
            this.imgUser = itemView.findViewById(R.id.item_avatar_user);
            this.tvUserName = itemView.findViewById(R.id.item_user_name);
            this.tvStatus = itemView.findViewById(R.id.item_status);
        }
    }

    public UserAdapter(List<User> mDataAllUser, Context mContext, boolean isFriends) {
        this.mDataAllUser = mDataAllUser;
        this.mContext = mContext;
        this.isFriends = isFriends;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {

        final User user = mDataAllUser.get(position);

        if (!user.getAvatarURL().equals("default")) {
            Glide.with(mContext).load(user.getAvatarURL()).into(holder.imgUser);
        } else
            holder.imgUser.setImageResource(R.drawable.pngtest);

        //dots
        if (user.getOnoroff().equals("online")) {
            holder.dotOn.setVisibility(View.VISIBLE);
        } else holder.dotOn.setVisibility(View.GONE);

        //textView
        holder.tvUserName.setText(user.getUserName());
        if (isFriends) {
            lastMessage(user.getId(), holder.tvStatus);
        } else
            holder.tvStatus.setText(user.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("userID", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataAllUser.size();
    }

    private void lastMessage(final String userID, final TextView imgLsatMessage) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if ((chat.getSender()
                            .equals(firebaseUser
                                    .getUid())
                            && chat
                            .getReceiver()
                            .equals(userID))
                            || (chat.getSender()
                            .equals(userID)
                            && chat
                            .getReceiver()
                            .equals(firebaseUser.getUid()))) {

                        theLastMessage = chat.getMessage();
                    }
                }

                if (!theLastMessage.equals("default")) {
                    if (theLastMessage.length()>=20){
                        theLastMessage = theLastMessage.substring(0,21)+"...";
                    }
                    imgLsatMessage.setText(theLastMessage);
                }else {
                    imgLsatMessage.setText("No Message");
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
