package com.example.travelo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.R;
import com.example.travelo.activities.ProfileActivity;
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
        TextView tvBio;
        RelativeLayout rlUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvBio = itemView.findViewById(R.id.tvBio);
            rlUser = itemView.findViewById(R.id.rlUser);
        }

        public void bind(ParseUser user) {
            String username = user.getUsername();
            tvName.setText(username);
            String bio = user.getString("bio");
            tvBio.setText(bio);
            ParseFile profileImageFile = user.getParseFile("profileImage");
            if (profileImageFile != null) {
                String profileImageUrl = profileImageFile.getUrl();
                Glide.with(context)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(ivProfileImage);
            }
            rlUser.setOnClickListener(v -> onClick(username));
        }

        public void onClick(String username) {
            Log.i("SearchUserAdapter", "Clicked searched user");
            ProfileActivity.goToProfile(context, username);
        }
    }
}
