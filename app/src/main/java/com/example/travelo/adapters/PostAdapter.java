package com.example.travelo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.example.travelo.activities.DetailsPostActivity;
import com.example.travelo.activities.ProfileActivity;
import com.example.travelo.R;
import com.example.travelo.constants.Constant;
import com.example.travelo.models.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.json.JSONException;
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
        Button btnLike;
        ImageView ivLikeAnimation;
        final TextView tvLikeCount;
        TextView tvCommentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            rlPost = itemView.findViewById(R.id.rlPost);
            ivImage = itemView.findViewById(R.id.ivImage);
            btnLike = itemView.findViewById(R.id.btnLike);
            ivLikeAnimation = itemView.findViewById(R.id.ivLikeAnimation);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
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
            // Setup comments count
            int commentsSize = post.getComments().length();
            tvCommentCount.setText(String.valueOf(commentsSize));

            // Setup like button
            Constant.setupLikeButton(btnLike, post.getObjectId(), tvLikeCount);

            ivLikeAnimation.setVisibility(View.GONE);

            // Setup long click to like
            ivImage.setOnLongClickListener(v -> {
                Log.i("PostAdapter","Long clicked to like");
                if (btnLike.isClickable()) {
                    btnLike.performClick();
                    Drawable drawable = ivLikeAnimation.getDrawable();
                    ivLikeAnimation.setVisibility(View.VISIBLE);
                    if (drawable instanceof AnimatedVectorDrawableCompat) {
                        AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) drawable;
                        avd.start();
                    } else if (drawable instanceof AnimatedVectorDrawable) {
                        AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawable;
                        avd.start();
                    }
                }
                return true;
            });
            // On click go to detailed post
            ivImage.setOnClickListener(v -> goToDetailedPost(post));
            rlPost.setOnClickListener(v -> goToDetailedPost(post));
        }

        public void goToDetailedPost(Post post) {
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
        }


    }
}
