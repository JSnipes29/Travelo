package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.EndlessRecyclerViewScrollListener;
import com.example.travelo.adapters.PostAdapter;
import com.example.travelo.adapters.UsersAdapter;
import com.example.travelo.databinding.FragmentHomeBinding;
import com.example.travelo.models.Post;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {


    public static final int LIMIT = 2;
    public static final String TAG = "HomeFragment";

    FragmentHomeBinding binding;
    List<Post> posts;
    PostAdapter postAdapter;
    UsersAdapter usersAdapter;
    List<String[]> following;
    EndlessRecyclerViewScrollListener scrollListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        // Set up adapter for posts recycler view
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts);
        binding.rvPosts.setAdapter(postAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvPosts.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "Load more");
                queryPosts(1);
            }
        };
        binding.rvPosts.addOnScrollListener(scrollListener);
        binding.rvPosts.setNestedScrollingEnabled(false);
        // Query posts to populate feed
        queryPosts(0);

        // Set up adapter for users following
        following = new ArrayList<>();
        usersAdapter = new UsersAdapter(getContext(), following);
        binding.rvFollowing.setAdapter(usersAdapter);
        LinearLayoutManager followingLayoutManger = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvFollowing.setLayoutManager(followingLayoutManger);
        // Query users to populate users following
        queryFollowing();

        // Configure pull to refresh
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                queryPosts(2);
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void queryPosts(int parameter) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_OWNER);
        query.setLimit(LIMIT);
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
            } else if (parameter == 2) {
                Log.i(TAG, "Pulling to refresh");
                this.posts.clear();
                postAdapter.notifyDataSetChanged();
            }
            this.posts.addAll(posts);
            if (parameter == 1) {
                postAdapter.notifyItemRangeInserted(start, posts.size());
            } else {
                postAdapter.notifyDataSetChanged();
            }
            if (parameter == 2) {
                binding.swipeContainer.setRefreshing(false);
            }
            scrollListener.resetState();
        });
    }

    private void queryFollowing() {
        ParseQuery<ParseUser> q = ParseQuery.getQuery(ParseUser.class);
        q.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                JSONArray jsonFollowing = user.getJSONArray("following");
                for (int i = 0; i < jsonFollowing.length(); i++) {
                    ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
                    query.include("followers");
                    String id = null;
                    try {
                        id = jsonFollowing.getString(i);
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                    query.getInBackground(id, new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser object, ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error getting user", e);
                            }
                            String name = object.getUsername();
                            String imageUrl = object.getParseFile("profileImage").getUrl();
                            String[] userArray = {name, imageUrl};
                            following.add(userArray);
                            usersAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }
        });

    }
}