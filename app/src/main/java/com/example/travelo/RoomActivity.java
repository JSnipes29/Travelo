package com.example.travelo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.databinding.ActivityRoomBinding;
import com.example.travelo.fragments.EditMapFragment;
import com.example.travelo.fragments.RoomMessagesFragment;
import com.example.travelo.models.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

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

        // Specify which class to query
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        // Specify the object id
        query.getInBackground(id, new GetCallback<Room>() {
            public void done(Room room, ParseException e) {
                if (e == null) {
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
                switch (item.getItemId()) {
                    case R.id.action_map:
                        fragment = new EditMapFragment();
                        break;
                    case R.id.action_message:
                        Bundle bundle = new Bundle();
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