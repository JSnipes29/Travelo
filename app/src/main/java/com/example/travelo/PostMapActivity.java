package com.example.travelo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.travelo.databinding.ActivityPostMapBinding;
import com.example.travelo.fragments.PostMapFragment;
import com.example.travelo.fragments.WaitingPostFragment;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.Iterator;

public class PostMapActivity extends AppCompatActivity {

    public static final String TAG = "PostMapActivity";
    ActivityPostMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostMapBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        // Specify which class to query
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        // Specify the object id
        query.getInBackground(id, new GetCallback<Room>() {
            public void done(Room room, ParseException e) {
                if (e == null) {
                    setFragment(room);
                } else {
                    Log.e(TAG, "Error joining room", e);
                }
            }
        });
    }

    private void setFragment(Room room) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putParcelable("room", Parcels.wrap(room));
        JSONObject users = room.getUsers();
        Iterator<String> iter = users.keys();
        boolean ready = true;
        while(iter.hasNext()) {
            String key = iter.next();
            try {
                boolean r = users.getBoolean(key);
                // If users isn't ready set variable to false
                if (!r) {
                    ready = false;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // If ready go to post fragment
        if (ready) {
            fragment = new PostMapFragment();
        } else {
           // else go to waiting fragment
            fragment = new WaitingPostFragment();
        }
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }
}