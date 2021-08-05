package com.example.travelo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.travelo.adapters.SearchUserAdapter;
import com.example.travelo.listeners.EndlessRecyclerViewScrollListener;
import com.example.travelo.R;
import com.example.travelo.adapters.PostAdapter;
import com.example.travelo.adapters.UsersAdapter;
import com.example.travelo.databinding.FragmentHomeBinding;
import com.example.travelo.models.Post;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class HomeFragment extends Fragment {


    public static final int LIMIT = 10;
    public static final String TAG = "HomeFragment";

    FragmentHomeBinding binding;
    List<Post> posts;
    PostAdapter postAdapter;
    UsersAdapter usersAdapter;
    List<String[]> following;
    EndlessRecyclerViewScrollListener scrollListener;
    List<ParseUser> searchedUsers;
    SearchUserAdapter searchUserAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        // Setup toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Set up empty searched users
        searchedUsers = new ArrayList<>();
        searchUserAdapter = new SearchUserAdapter(getContext(), searchedUsers);
        binding.rvSearchedUsers.setAdapter(searchUserAdapter);
        LinearLayoutManager searchedUsersLayoutManager = new LinearLayoutManager(getContext());
        binding.rvSearchedUsers.setLayoutManager(searchedUsersLayoutManager);
        // Set up adapter for posts recycler view
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, (AppCompatActivity) getActivity());
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

    // Query posts from the server, parameter 0 == onStart, 1 == endless scrolling, 2 == pull to refresh
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
        // If starting, start the shimmer effect
        if (parameter == 0 && binding != null) {
            binding.shimmerLayout.startShimmer();
        }
        Context context = getContext();
        query.findInBackground((posts, e) -> {
            if (e != null) {
                Log.e(TAG, "Couldn't get post", e);
                if (parameter == 0) {
                    getPostsFromLocal();
                } else if (parameter == 2) {
                    Toasty.error(context, "Error refreshing home feed", Toast.LENGTH_SHORT, true).show();
                    if (binding != null) {
                        binding.swipeContainer.setRefreshing(false);
                    }
                }
                return;
            }
            for (Post post: posts) {
                Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getOwner().getUsername());
                // Pin the post in the local database on start
                if (parameter == 0) {
                    ParseObject.unpinAllInBackground("HomePosts", e1 -> {
                        if (e1 != null) {
                            Log.e(TAG, "Error unpinning posts from local datastore");
                            return;
                        }
                        ParseObject.pinAllInBackground("HomePosts", posts);
                    });
                }
            }
            if (parameter == 1) {
                Log.i(TAG, "Endless scrolling in effect");
            } else if (parameter == 2) {
                // If pulling to refresh notify the adapter that items have been removed
                Log.i(TAG, "Pulling to refresh");
                int size = this.posts.size();
                this.posts.clear();
                postAdapter.notifyItemRangeRemoved(start, size);
                scrollListener.resetState();
            }
            // Add the new posts to the posts list
            this.posts.addAll(posts);
            // If endless scrolling, notify adapter that items have been inserted
            if (parameter == 1) {
                postAdapter.notifyItemRangeInserted(start, posts.size());
                Log.i(TAG, "Size: " + this.posts.size());
            } else {
                postAdapter.notifyDataSetChanged();
            }
            // Set refreshing marker to false for pulling to refresh
            if (parameter == 2 && binding != null) {
                binding.swipeContainer.setRefreshing(false);
            }
            // If on start, remove the shimmer and set the recycler view to visible
            if (parameter == 0) {
                if (binding != null) {
                    binding.shimmerLayout.setVisibility(View.GONE);
                    binding.shimmerLayout.hideShimmer();
                    binding.rvPosts.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void queryFollowing() {
        ParseQuery<ParseUser> q = ParseQuery.getQuery(ParseUser.class);
        q.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting following data", e);
                    return;
                }
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


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Search the server using the query
                searchView.clearFocus();
                if (binding != null) {
                    // Set the feed recycler view to gone
                    binding.rvPosts.setVisibility(View.GONE);
                    // Set the following recycler view and border to gone
                    binding.rvFollowing.setVisibility(View.GONE);
                    binding.border.setVisibility(View.GONE);
                }
                searchedUsers.clear();
                searchUserAdapter.notifyDataSetChanged();
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchClickListener(v -> binding.tvAppName.setVisibility(View.GONE));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Clear the searched users view
                searchedUsers.clear();
                searchUserAdapter.notifyDataSetChanged();
                binding.searchShimmerLayout.setVisibility(View.GONE);
                binding.rvSearchedUsers.setVisibility(View.GONE);
                // Make the feed and other elements visible again
                binding.tvAppName.setVisibility(View.VISIBLE);
                binding.rvPosts.setVisibility(View.VISIBLE);
                binding.rvFollowing.setVisibility(View.VISIBLE);
                binding.border.setVisibility(View.VISIBLE);
                return false;
            }
        });
    }

    public void searchUsers(String query) {
        if (binding != null) {
            binding.searchShimmerLayout.setVisibility(View.VISIBLE);
            binding.searchShimmerLayout.startShimmer();
        }
        ParseQuery<ParseUser> usersQuery = ParseQuery.getQuery(ParseUser.class);
        usersQuery.whereContains("username", query);
        usersQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting data from server");
                    return;
                }
                searchedUsers.addAll(users);
                searchUserAdapter.notifyDataSetChanged();
                if (binding != null) {
                    binding.searchShimmerLayout.hideShimmer();
                    binding.searchShimmerLayout.setVisibility(View.GONE);
                    binding.rvSearchedUsers.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void getPostsFromLocal() {
        Context context = getContext();
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.fromLocalDatastore();
        postQuery.include(Post.KEY_OWNER);
        postQuery.addDescendingOrder(Post.KEY_CREATED_AT);
        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> localPosts, ParseException e) {
                if (e != null) {
                    Toasty.error(context, "Error getting posts from local datastore", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                posts.addAll(localPosts);
                postAdapter.notifyDataSetChanged();
                if (binding != null) {
                    binding.shimmerLayout.setVisibility(View.GONE);
                    binding.shimmerLayout.hideShimmer();
                    binding.rvPosts.setVisibility(View.VISIBLE);
                }
                Log.i(TAG, "Home feed using local datastore");
            }
        });
    }

}