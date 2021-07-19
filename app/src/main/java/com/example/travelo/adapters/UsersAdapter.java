package com.example.travelo.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.MainActivity;
import com.example.travelo.ProfileActivity;
import com.example.travelo.R;
import com.example.travelo.fragments.ProfileFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    List<String[]> users;
    Context context;

    public UsersAdapter(Context context, List<String[]> users) {
        this.context = context;
        this.users = users;
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
            ivProfileImage.setOnClickListener(v -> goToProfile(user[0]));
            tvName.setOnClickListener(v -> goToProfile(user[0]));
        }

        public void goToProfile(String userid) {
            Intent intent = new Intent(context, ProfileActivity.class);
            ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
            query.whereEqualTo("username", userid);
            query.setLimit(1);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e != null) {
                        Log.e("UsersAdapter","Couldn't retrieve user",e);
                        return;
                    }
                    if (objects.isEmpty()) {
                        Log.i("UsersAdapter", "Couldn't find user");
                        return;
                    }
                    ParseUser parseUser = objects.get(0);
                    Log.i("UsersAdapter","Found user: " + parseUser.getObjectId());
                    intent.putExtra("parseUser", Parcels.wrap(parseUser));
                    context.startActivity(intent);
                }
            });

        }
    }
}
