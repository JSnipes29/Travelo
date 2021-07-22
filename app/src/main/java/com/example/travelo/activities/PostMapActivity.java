package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.travelo.R;
import com.example.travelo.databinding.ActivityPostMapBinding;
import com.example.travelo.fragments.PostMapFragment;
import com.example.travelo.fragments.WaitingPostFragment;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
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
            @Override
            public void done(Room room, ParseException e) {
                if (e == null) {
                    // Set the fragment
                    setFragment(room);
                    // The room is no longer joinable
                    room.setJoinable(false);
                    final String roomObjectId = room.getObjectId();
                    room.saveInBackground();
                    // The room message is removed from the inbox
                    ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                    userQuery.include(Inbox.KEY);
                    userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser currentUser, ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Problem loading user data from server", e);
                                return;
                            }
                            Inbox inbox = (Inbox) currentUser.getParseObject(Inbox.KEY);
                            JSONArray jsonInbox = inbox.getMessages();
                            int index = Inbox.indexOfRoomMessage(jsonInbox, roomObjectId);
                            if (index == -1) {
                                return;
                            }
                            jsonInbox.remove(index);
                            inbox.setMessages(jsonInbox);
                            inbox.saveInBackground(exception -> {
                                if (exception != null) {
                                    Log.e(TAG, "Couldn't remove room message from inbox", exception);
                                } else {
                                    Log.i(TAG, "Room message removed from inbox");
                                }
                            });
                        }
                    });
                } else {
                    Log.e(TAG, "Error joining room", e);
                }
            }
        });
    }

    // Set the fragment to waiting or post, depending on if all users are ready
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

    @Override
    public void onBackPressed() {
        return;
    }
}