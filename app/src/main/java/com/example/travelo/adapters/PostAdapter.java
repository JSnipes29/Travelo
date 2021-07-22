package com.example.travelo.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.activities.DetailsPostActivity;
import com.example.travelo.activities.ProfileActivity;
import com.example.travelo.R;
import com.example.travelo.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    Context context;
    List<Post> posts;
    AppCompatActivity activity;

    public PostAdapter(Context context, List<Post> posts, AppCompatActivity activity) {
        this.context = context;
        this.posts = posts;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  PostAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        TextView tvName;
        ImageView ivProfileImage;
        TextView tvDescription;
        TextView tvTimestamp;
        RelativeLayout rlPost;
        final ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            rlPost = itemView.findViewById(R.id.rlPost);
            ivImage = itemView.findViewById(R.id.ivImage);
        }

        public void bind(Post post) {
            tvDescription.setText(post.getDescription());
            tvTimestamp.setText(Post.getRelativeTimeAgo(post.getCreatedAt().toString()));
            ParseUser user = post.getOwner();
            tvName.setText(user.getUsername());
            String url = user.getParseFile("profileImage").getUrl();
            if (url != null) {
                Glide.with(context)
                        .load(url)
                        .circleCrop()
                        .into(ivProfileImage);
                ivProfileImage.setClickable(true);
                ivProfileImage.setOnClickListener(v -> ProfileActivity.goToProfile(context, user.getUsername()));
            }
            ParseFile image = post.getPhoto();
            // If the image is null, don't show the image
            if (image == null) {
                ivImage.setVisibility(View.GONE);
            } else {
                Glide.with(context)
                        .load(image.getUrl())
                        .into(ivImage);
            }
            // On click go to detail post
            rlPost.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailsPostActivity.class);
                intent.putExtra("post", Parcels.wrap(post));
                // Shared content transition
                Pair<View, String> profileImage = Pair.create((View)ivProfileImage, "profileImage");
                Pair<View, String> name = Pair.create((View)tvName, "name");
                Pair<View, String> description = Pair.create((View)tvDescription, "description");
                Pair<View, String> imagePair = Pair.create((View)ivImage, "image");
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                                profileImage, name, description, imagePair);
                context.startActivity(intent, options.toBundle());
            });
        }

    }
}
