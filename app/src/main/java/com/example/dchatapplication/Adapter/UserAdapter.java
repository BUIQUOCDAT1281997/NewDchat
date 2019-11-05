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
import com.example.dchatapplication.R;
import com.example.dchatapplication.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView .Adapter<UserAdapter.UserViewHolder>{

    private List<User> mDataAllUser;
    private Context mContext;

    // ViewHolder
    public class UserViewHolder extends RecyclerView.ViewHolder{

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

    public UserAdapter(List<User> mDataAllUser, Context mContext) {
        this.mDataAllUser=mDataAllUser;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {

        final User user = mDataAllUser.get(position);

        if (!user.getAvatarURL().equals("default")){
            Glide.with(mContext).load(user.getAvatarURL()).into(holder.imgUser);
        }else
            holder.imgUser.setImageResource(R.drawable.pngtest);

        //dots
        if (user.getOnoroff().equals("online")){
            holder.dotOn.setVisibility(View.VISIBLE);
        } else holder.dotOn.setVisibility(View.GONE);

        holder.tvUserName.setText(user.getUserName());
        holder.tvStatus.setText(user.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("userID",user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
       return mDataAllUser.size();
    }
}
