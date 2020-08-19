package bui.quocdat.dchat.Adapter;

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

import java.util.List;

import bui.quocdat.dchat.Activity.ChatActivity;
import bui.quocdat.dchat.Other.User;
import bui.quocdat.dchat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> mDataAllUser;
    private Context mContext;

    // ViewHolder
    static class UserViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgUser;
        TextView tvUserName, tvStatus;
        ImageView ivDot;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imgUser = itemView.findViewById(R.id.item_avatar_user);
            this.tvUserName = itemView.findViewById(R.id.item_user_name);
            this.tvStatus = itemView.findViewById(R.id.item_status);
            this.ivDot =  itemView.findViewById(R.id.item_user_dot);
        }
    }

    public UserAdapter(List<User> mDataAllUser, Context mContext) {
        this.mDataAllUser = mDataAllUser;
        this.mContext = mContext;
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

        if (!user.getUrl().isEmpty()) {
            Glide.with(mContext).load(user.getUrl()).placeholder(R.drawable.ic_launcher_round_black_white).into(holder.imgUser);
        } else
            holder.imgUser.setImageResource(R.drawable.ic_launcher_round_black_white);

//        setBorderImgUser(holder.imgUser, user.getId());

        if (user.getStatus()) {
            holder.imgUser.setBorderWidth((int) mContext.getResources().getDimension(R.dimen.border_online));
        } else
            holder.imgUser.setBorderWidth((int) mContext.getResources().getDimension(R.dimen.border_offline));


        //textView
        holder.tvUserName.setText(user.getFullName());

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("userID", user.getId());
            mContext.startActivity(intent);
        });

        if (!user.getLastMess().getText().isEmpty()) {
            holder.tvStatus.setText(user.getLastMess().getText());
            if (!user.getLastMess().isSeen()) {
                holder.ivDot.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataAllUser.size();
    }

//    private void lastMessage(final String userID, final TextView imgLsatMessage) {
//        theLastMessage = "default";
//        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Chat chat = snapshot.getValue(Chat.class);
//                    if ((chat.getSender()
//                            .equals(firebaseUser
//                                    .getUid())
//                            && chat
//                            .getReceiver()
//                            .equals(userID))
//                            || (chat.getSender()
//                            .equals(userID)
//                            && chat
//                            .getReceiver()
//                            .equals(firebaseUser.getUid()))) {
//
//                        theLastMessage = chat.getMessage();
//                        if (chat.getIsSeen().equals("false")) {
//                            imgLsatMessage.setBackgroundResource(R.drawable.custom_button);
//
//                        } else {
//                            imgLsatMessage.setBackgroundResource(R.drawable.custom_view_to_circle);
//                        }
//                    }
//                }
//
//                if (!theLastMessage.equals("default")) {
//                    if (theLastMessage.length() >= 20) {
//                        theLastMessage = theLastMessage.substring(0, 21) + "...";
//                    }
//                    imgLsatMessage.setText(theLastMessage);
//                } else {
//                    imgLsatMessage.setText("No Message");
//                }
//                theLastMessage = "default";
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

}
