package com.example.travelo.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.activities.RoomActivity;
import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.databinding.FragmentCreateRoomBinding;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Room;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class CreateRoomFragment extends DialogFragment {

    FragmentCreateRoomBinding binding;
    public static final String TAG = "CreateRoomFragment";
    Room room;

    public CreateRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreateRoomBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        binding.btnCreate.setOnClickListener(v -> create(v));
        return view;
    }

    public void create(View v) {
        String roomId = binding.etRoomId.getText().toString();
        // Don't create room if id is empty
        if (roomId.isEmpty()) {
            // Show error message
            Toasty.error(getContext(), "Must enter a room id", Toast.LENGTH_SHORT, true).show();
            return;
        }
        Room room = new Room(roomId);
        room.setOwner(ParseUser.getCurrentUser());
        JSONObject users = new JSONObject();
        JSONObject profileImages = new JSONObject();
        String profileUrl = ParseUser.getCurrentUser().getParseFile("profileImage").getUrl();
        try {
            users.put(ParseUser.getCurrentUser().getUsername(), false);
            profileImages.put(ParseUser.getCurrentUser().getUsername(), profileUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject map = new JSONObject();
        try {
            map.put("owner", ParseUser.getCurrentUser().getUsername());
            map.put("markers", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        room.setMap(map);
        room.setUsers(users);
        room.setProfileImages(profileImages);
        // Specify which class to query
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        // Get Room with id
        query.whereEqualTo("roomId", roomId);
        // Find the room asynchronously
        query.findInBackground(new FindCallback<Room>() {
            @Override
            public void done(List<Room> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error saving room", e);
                }
                if (!objects.isEmpty()) {
                    Toasty.error(v.getContext(), "Can't make a room with this id", Toast.LENGTH_SHORT, true).show();
                    dismiss();
                    return;
                } else {
                    room.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error saving room");
                                dismiss();
                                return;
                            }
                            Log.i(TAG, "Done Creating Room");
                            Toasty.success(v.getContext(), "Created Map!", Toast.LENGTH_SHORT, true).show();
                            dismiss();
                            Intent intent = new Intent(v.getContext(), RoomActivity.class);
                            intent.putExtra("room", room.getObjectId());
                            intent.putExtra("ownerId", ParseUser.getCurrentUser().getObjectId());
                            startActivity(intent);

                            // Add to inbox
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
                                    String roomObjectId = room.getObjectId();
                                    int index = Inbox.indexOfRoomMessage(jsonInbox, roomObjectId);
                                    if (index != -1) {
                                        return;
                                    }
                                    JSONObject roomMessage = new JSONObject();
                                    try {
                                        roomMessage.put("roomObjectId", roomObjectId);
                                        roomMessage.put("roomId", roomId);
                                        roomMessage.put("id", InboxAdapter.ROOM_ID);
                                    } catch (JSONException jsonException) {
                                        Log.e(TAG, "Couldn't edit json data", jsonException);
                                    }
                                    jsonInbox.put(roomMessage);
                                    inbox.setMessages(jsonInbox);
                                    inbox.saveInBackground(exception -> {
                                        if (exception != null) {
                                            Log.e(TAG, "Couldn't save room message in inbox", exception);
                                        } else {
                                            Log.i(TAG, "Room message saved in inbox");
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });




    }
}