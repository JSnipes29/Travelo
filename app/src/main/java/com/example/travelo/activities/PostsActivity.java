package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.adapters.PostAdapter;
import com.example.travelo.databinding.ActivityPostsBinding;
import com.example.travelo.models.Post;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class PostsActivity extends AppCompatActivity {

    ActivityPostsBinding binding;
    List<Post> posts;
    PostAdapter postAdapter;
    int type;
    String userId;

    public static final String TAG = "PostsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        type = getIntent().getIntExtra("type", 0);
        userId = getIntent().getStringExtra("userId");

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(this, posts, this);
        binding.rvPosts.setAdapter(postAdapter);
        LinearLayoutManager postLayoutManager = new LinearLayoutManager(this);
        binding.rvPosts.setLayoutManager(postLayoutManager);
        queryPosts();
    }

    public void queryPosts() {
        Context context = this;
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting user data", e);
                    Toasty.error(context, "Error retrieving data from server", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                JSONArray array = user.getJSONArray("liked");
                try {
                    addPosts(array);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        });
    }

    public void addPosts(JSONArray jsonPosts) throws JSONException {
        for (int i = 0; i < jsonPosts.length(); i++) {
            String postId = jsonPosts.getString(i);
            ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
            postQuery.include("owner");
            postQuery.getInBackground(postId, new GetCallback<Post>() {
                @Override
                public void done(Post post, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error getting post data", e);
                        return;
                    }
                    posts.add(post);
                    postAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}