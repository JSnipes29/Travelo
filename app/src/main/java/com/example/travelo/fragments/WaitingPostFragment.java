package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.adapters.NameAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.FragmentWaitingPostBinding;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaitingPostFragment extends Fragment {

    public static final String TAG = "WaitingPostFragment";

    FragmentWaitingPostBinding binding;
    Room room;
    NameAdapter adapter;
    List<String> users;

    public WaitingPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWaitingPostBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        // Get room from bundle
        room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
        JSONObject jsonUsers = room.getUsers();
        users = new ArrayList<>();
        Iterator<String> iterator = jsonUsers.keys();
        // Get a list of unready users
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                boolean ready = jsonUsers.getBoolean(key);
                if (!ready) {
                    users.add(key);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Couldn't load ready data from server", e);
            }
        }
        adapter = new NameAdapter(getContext(), users);
        binding.rvNames.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvNames.setLayoutManager(linearLayoutManager);
        String ownerId = room.getOwner().getObjectId();
        if (ownerId.equals(ParseUser.getCurrentUser().getObjectId())) {
            ItemTouchHelper itemTouchHelper = Constant.setupKickSwipe(getContext(), users, adapter, room.getObjectId(), 1);
            itemTouchHelper.attachToRecyclerView(binding.rvNames);
        }

        // Setup refresh listener which triggers new data loading
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                refresh();
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return view;

    }

    public void refresh() {
        // Specify which class to query
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        // Specify the object id
        query.getInBackground(room.getObjectId(), new GetCallback<Room>() {
            public void done(Room r, ParseException e) {
                binding.swipeContainer.setRefreshing(false);
                if (e == null) {
                    room = r;
                    JSONObject jsonUsers = r.getUsers();
                    try {
                        jsonUsers.put(ParseUser.getCurrentUser().getUsername(), true);
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                    room.setUsers(jsonUsers);
                    room.saveInBackground();
                    users.clear();
                    Iterator<String> iterator = jsonUsers.keys();
                    // Get a list of unready users
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        try {
                            boolean ready = jsonUsers.getBoolean(key);
                            if (!ready) {
                                users.add(key);
                            }
                        } catch (JSONException error) {
                            Log.e(TAG, "Couldn't load ready data from server", error);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (users.isEmpty()) {
                        Log.i(TAG, "Going to post");
                        final FragmentManager fragmentManager = getParentFragmentManager();
                        Fragment fragment = new PostMapFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("room", Parcels.wrap(r));
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                    }
                } else {
                    Log.e(TAG, "Error joining room", e);
                }
            }
        });

    }
}