package com.example.travelo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.databinding.ActivityRoomBinding;
import com.example.travelo.fragments.EditMapFragment;
import com.example.travelo.fragments.RoomMessagesFragment;
import com.example.travelo.models.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.parceler.Parcels;

public class RoomActivity extends AppCompatActivity {

    public static final String TAG = "RoomActivity";
    ActivityRoomBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Intent intent = getIntent();
        String id = intent.getStringExtra("room");
        // Set up the app bar
        binding.bar.setOnMenuClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the draw when menu is clicked
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });
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
}