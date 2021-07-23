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
import com.example.travelo.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {

    List<ParseUser> users;
    Context context;

    public SearchUserAdapter(Context context, List<ParseUser> users) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_details_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserAdapter.ViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
        }

        public void bind(ParseUser user) {
            String username = user.getUsername();
            tvName.setText(username);
            ParseFile profileImageFile = user.getParseFile("profileImage");
            if (profileImageFile != null) {
                String profileImageUrl = profileImageFile.getUrl();
                Glide.with(context)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(ivProfileImage);
            }
        }
    }
}
