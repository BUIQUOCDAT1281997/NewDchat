package bui.quocdat.dchat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import bui.quocdat.dchat.R;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureViewHolder> {

    private List<String> listUrlPicture;
    private Context mContext;

    public PictureAdapter(List<String> listUrlPicture, Context mContext) {
        this.listUrlPicture = listUrlPicture;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_picture, parent, false);
        return new PictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        Glide.with(mContext).load(listUrlPicture.get(position)).placeholder(R.color.textDefaultColor).into(holder.picture);
    }

    @Override
    public int getItemCount() {
        return listUrlPicture.size();
    }

    class PictureViewHolder extends RecyclerView.ViewHolder {

        ImageView picture;

        PictureViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.img_picture_all);
        }
    }

}
