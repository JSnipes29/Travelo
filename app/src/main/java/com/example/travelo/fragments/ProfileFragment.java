package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.travelo.EndlessRecyclerViewScrollListener;
import com.example.travelo.R;
import com.example.travelo.adapters.PostAdapter;
import com.example.travelo.databinding.FragmentProfileBinding;
import com.example.travelo.models.Post;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    FragmentProfileBinding binding;
    ParseUser user;
    PostAdapter postAdapter;
    List<Post> posts;
    EndlessRecyclerViewScrollListener scrollListener;
    public static final int LIMIT = 4;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        user = (ParseUser) Parcels.unwrap(getArguments().getParcelable("user"));
        binding.tvName.setText(user.getUsername());
        Glide.with(getContext())
                .load(user.getParseFile("profileImage").getUrl())
                .circleCrop()
                .into(binding.ivProfileImage);
        List<String> following = jsonToList(user.getJSONArray("following"));
        List<String> followers = jsonToList(user.getParseObject("followers").getJSONArray("followers"));
        binding.tvFollowersCount.setText(String.valueOf(followers.size()));
        binding.tvFollowingCount.setText(String.valueOf(following.size()));
        // Don't show the following button if the user is the current user
        String currentUserId = ParseUser.getCurrentUser().getObjectId();
        if (user.getObjectId().equals(currentUserId)) {
            binding.btnFollow.setVisibility(View.GONE);
        } else {
            if (followers.contains(currentUserId)) {
                Log.i(TAG, "Following");
                binding.btnFollow.setText(R.string.following);
                binding.btnFollow.setOnClickListener(v -> unFollow());
            } else {
                binding.btnFollow.setText(R.string.follow);
                binding.btnFollow.setOnClickListener(v -> follow());
            }
        }
        // Bind posts to recycler view
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts);
        binding.rvPosts.setAdapter(postAdapter);
        LinearLayoutManager postLayoutManager = new LinearLayoutManager(getContext());
        binding.rvPosts.setLayoutManager(postLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(postLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                queryPosts(1);
            }
        };
        binding.rvPosts.addOnScrollListener(scrollListener);
        queryPosts(0);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void follow() {
        binding.tvFollowersCount.setText(String.valueOf(1 + Integer.parseInt(binding.tvFollowersCount.getText().toString())));
        // Button now unfollows on click
        binding.btnFollow.setText(R.string.following);
        binding.btnFollow.setOnClickListener(v -> unFollow());
        binding.btnFollow.setClickable(false);
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser currentUser, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Couldn't find current user", e);
                }
                // Add current user to users followers
                ParseObject followersObject = user.getParseObject("followers");
                JSONArray followers = followersObject.getJSONArray("followers");
                followers.put(currentUser.getObjectId());
                user.getParseObject("followers").put("followers", followers);
                // Add user to current users following
                JSONArray following = currentUser.getJSONArray("following");
                following.put(user.getObjectId());
                currentUser.put("following", following);
                // Save both to the Parse server
                followersObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Couldn't update followers");
                        } else {
                            Log.i(TAG, "Updated followers");
                        }
                    }
                });
                currentUser.saveInBackground();

                // Button is clickable again
                binding.btnFollow.setClickable(true);

            }
        });

    }

    public void unFollow() {
        // Button now follows on click
        binding.btnFollow.setText(R.string.follow);
        binding.btnFollow.setOnClickListener(v -> follow());
        binding.tvFollowersCount.setText(String.valueOf(Integer.parseInt(binding.tvFollowersCount.getText().toString()) - 1));
        binding.btnFollow.setClickable(false);
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser currentUser, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Couldn't find current user", e);
                }
                // Add current user to users followers
                ParseObject followersObject = user.getParseObject("followers");
                JSONArray followers = followersObject.getJSONArray("followers");
                followers = jsonDelete(followers, (currentUser.getObjectId()));
                user.getParseObject("followers").put("followers", followers);
                // Add user to current users following
                JSONArray following = currentUser.getJSONArray("following");
                following = jsonDelete(following, user.getObjectId());
                currentUser.put("following", following);
                // Save both to the Parse server
                followersObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Couldn't update followers");
                        } else {
                            Log.i(TAG, "Updated followers");
                        }
                    }
                });
                currentUser.saveInBackground();

                // Button is clickable again
                binding.btnFollow.setClickable(true);

            }
        });

    }

    private void queryPosts(int parameter) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_OWNER);
        query.setLimit(LIMIT);
        query.whereEqualTo("userArray", user.getUsername());
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        final int start;
        if (parameter == 1) {
            start = posts.size();
            query.whereLessThan(Post.KEY_CREATED_AT, posts.get(posts.size() - 1).getCreatedAt());
        } else {
            start = 0;
        }
        query.findInBackground((posts, e) -> {
            if (e != null) {
                Log.e(TAG, "Couldn't get post", e);
                return;
            }
            for (Post post: posts) {
                Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getOwner().getUsername());
            }
            if (parameter == 1) {
                Log.i(TAG, "Endless scrolling in effect");
            }
            this.posts.addAll(posts);
            if (parameter == 1) {
                postAdapter.notifyItemRangeInserted(start, posts.size());
            } else {
                postAdapter.notifyDataSetChanged();
            }
            scrollListener.resetState();
        });
    }

    private JSONArray jsonDelete(JSONArray jsonArray, String query) {
        JSONArray res = jsonArray;
        for (int i = 0; i < res.length(); i++) {
            try {
                String q = res.getString(i);
                if (q.equals(query)) {
                    res.remove(i);
                    return res;
                }
            } catch (JSONException e) {
                Log.e(TAG,"Error deleting from json array" ,e);
            }
        }
        return res;
    }

    public List<String> jsonToList(JSONArray array) {
        List<String> res = new ArrayList<>();
        if (array != null) {
            for (int i=0;i<array.length();i++){
                try {
                    res.add(array.getString(i));
                } catch (JSONException e) {
                    Log.e(TAG, "Error converting json array to list", e);
                }

            }
        }
        return res;
    }
}