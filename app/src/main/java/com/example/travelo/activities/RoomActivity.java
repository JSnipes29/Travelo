package com.example.travelo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.adapters.UsersAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.ActivityRoomBinding;
import com.example.travelo.fragments.EditMapFragment;
import com.example.travelo.fragments.InviteFriendsFragment;
import com.example.travelo.fragments.KickRoomFragment;
import com.example.travelo.fragments.RoomMessagesFragment;
import com.example.travelo.models.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class RoomActivity extends AppCompatActivity {

    public static final String TAG = "RoomActivity";
    ActivityRoomBinding binding;
    String id;
    String ownerId;
    UsersAdapter userAdapter;
    List<String[]> usersList;
    List<String> usernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        Context context = this;
        setContentView(view);
        Intent intent = getIntent();
        id = intent.getStringExtra("room");
        ownerId = intent.getStringExtra("ownerId");
        // Set up the app bar
        binding.bar.setOnMenuClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the draw when menu is clicked
                if (binding != null) {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        // Setup drawer view
        setupDrawerContent(binding.nvView);
        // Specify which class to query
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        // Specify the object id
        query.getInBackground(id, new GetCallback<Room>() {
            public void done(Room room, ParseException e) {
                if (e == null) {
                    JSONObject users = room.getUsers();
                    JSONObject profileImages = room.getProfileImages();
                    String profileUrl = ParseUser.getCurrentUser().getParseFile("profileImage").getUrl();
                    try {
                        users.put(ParseUser.getCurrentUser().getUsername(), false);
                        profileImages.put(ParseUser.getCurrentUser().getUsername(), profileUrl);
                    } catch (JSONException error) {
                        error.printStackTrace();
                        Log.e(TAG, "Couldn't upload users to json object", error);
                    }
                    room.setUsers(users);
                    room.setProfileImages(profileImages);
                    room.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.i(TAG, "Updated users to server");
                            } else {
                                Log.e(TAG, "Couldn't update users to server");
                            }
                        }
                    });
                    // Set up users in bar
                    usersList = new ArrayList<>();
                    usernames = new ArrayList<>();
                    JSONObject jsonUsers = room.getProfileImages();
                    Iterator<String> iter = jsonUsers.keys();

                    while(iter.hasNext()) {
                        String key = iter.next();
                        try {
                            String imageUrl = jsonUsers.getString(key);
                            String[] userArray = {key, imageUrl};
                            usersList.add(userArray);
                            usernames.add(key);
                        } catch (JSONException jsonException) {
                            Log.e(TAG, "Error getting users", jsonException);
                        }
                    }
                    // Bind the users who were in the room
                    userAdapter = new UsersAdapter(context, usersList, 1);
                    binding.rvUsers.setAdapter(userAdapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    binding.rvUsers.setLayoutManager(linearLayoutManager);

                    setBottomNavigation(room);
                } else {
                    Log.e(TAG, "Error joining room", e);
                }
            }
        });


    }

    public void setBottomNavigation(Room room) {
        // Handle bottom navigation selection
        final FragmentManager fragmentManager = getSupportFragmentManager();
        binding.bottomNavigationRoom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                Bundle bundle;
                switch (item.getItemId()) {
                    case R.id.action_map:
                        bundle = new Bundle();
                        bundle.putParcelable("room", Parcels.wrap(room));
                        fragment = new EditMapFragment();
                        fragment.setArguments(bundle);
                        break;
                    case R.id.action_message:
                        bundle = new Bundle();
                        bundle.putInt("type", 0);
                        bundle.putParcelable("room", Parcels.wrap(room));
                        fragment = new RoomMessagesFragment();
                        fragment.setArguments(bundle);
                        break;
                    default:
                        fragment = new RoomMessagesFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // Set default fragment
        binding.bottomNavigationRoom.setSelectedItemId(R.id.action_map);
    }

    // Setup the contents of the menu drawer
    public void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
    }
    // Choose what happens when item on menu drawer is selected
    public void selectDrawerItem(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.nav_invite:
                Log.i(TAG, "Clicked on invite");
                inviteFriends();
                break;
            case R.id.action_kick:
                Log.i(TAG, "Clicked on kick");
                kick();
            default:
                break;
        }
    }

    // Launch invite friends fragment
    public void inviteFriends() {
        if (!ParseUser.getCurrentUser().getObjectId().equals(ownerId)) {
            Toasty.error(this, "You can't invite friends", Toast.LENGTH_SHORT, true).show();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        InviteFriendsFragment inviteFriendsFragment = new InviteFriendsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("roomObjectId", id);
        inviteFriendsFragment.setArguments(bundle);
        inviteFriendsFragment.show(fragmentManager, "InviteFriends");
    }

    // Launch invite friends fragment
    public void kick() {
        if (!ParseUser.getCurrentUser().getObjectId().equals(ownerId)) {
            Toasty.error(this, "You can't kick people", Toast.LENGTH_SHORT, true).show();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        KickRoomFragment kickRoomFragment = new KickRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putString("roomObjectId", id);
        kickRoomFragment.setArguments(bundle);
        kickRoomFragment.show(fragmentManager, "KickRoom");
    }

    // Refresh user list on app bar
    public void refreshUsers(JSONObject jsonUsers) {
        Iterator<String> iter = jsonUsers.keys();
        boolean usersAdded = false;
        while(iter.hasNext()) {
            String key = iter.next();
            if (usernames.contains(key)) {
                continue;
            }
            try {
                String imageUrl = jsonUsers.getString(key);
                String[] userArray = {key, imageUrl};
                usersList.add(userArray);
                usernames.add(key);
                userAdapter.notifyDataSetChanged();
                usersAdded = true;
            } catch (JSONException jsonException) {
                Log.e(TAG, "Error getting users", jsonException);
            }
        }
        if (usersAdded) {
            Toasty.info(this, "Users joined", Toast.LENGTH_SHORT, true).show();
        }
    }
}