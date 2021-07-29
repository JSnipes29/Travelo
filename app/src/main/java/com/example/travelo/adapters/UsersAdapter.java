package com.example.travelo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.activities.ProfileActivity;
import com.example.travelo.R;
import com.example.travelo.constants.Constant;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    List<String[]> users;
    Context context;
    int type;

    public UsersAdapter(Context context, List<String[]> users) {
        this.context = context;
        this.users = users;
        this.type = 0;
    }

    public UsersAdapter(Context context, List<String[]> users, int type) {
        this.context = context;
        this.users = users;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        String[] user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
        }

        public void bind(String[] user) {
            if (type == 1) {
                int imageSize = Constant.dpsToPixels(context, 45);
                ivProfileImage.getLayoutParams().height = imageSize;
                ivProfileImage.getLayoutParams().width = imageSize;
                tvName.setVisibility(View.GONE);
            }
            tvName.setText(user[0]);
            String url = user[1];
            if (url != null) {
                Glide.with(context)
                        .load(url)
                        .circleCrop()
                        .into(ivProfileImage);
            }
            ivProfileImage.setClickable(true);
            tvName.setClickable(true);
            ivProfileImage.setOnClickListener(v -> ProfileActivity.goToProfile(context, user[0]));
            tvName.setOnClickListener(v -> ProfileActivity.goToProfile(context, user[0]));
        }


    }
}
