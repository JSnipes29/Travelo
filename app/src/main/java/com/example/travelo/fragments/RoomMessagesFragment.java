package com.example.travelo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.adapters.ChatAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.FragmentRoomMessagesBinding;
import com.example.travelo.models.Messages;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.concurrent.TimeUnit;


public class RoomMessagesFragment extends Fragment {

    public static final String TAG = "RoomMessagesFragment";
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    static final long POLL_INTERVAL = TimeUnit.SECONDS.toMillis(3);
    Handler handler = new Handler();
    Room room;
    Messages messagesObj;
    FragmentRoomMessagesBinding binding;
    ChatAdapter adapter;
    JSONArray messages;
    boolean firstLoad;
    Runnable refreshMessagesRunnable;
    int type;

    public RoomMessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoomMessagesBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        type = getArguments().getInt("type");
        if (type == 0) {
            // Get the room from the room activity
            room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
            Log.i(TAG, "Room id: " + room.getRoomId());
        } else if (type == 1) {
            messagesObj = (Messages) Parcels.unwrap(getArguments().getParcelable("messages"));
        }
        setupMessagePosting(type);
        // Setup handler that refreshes messages every few seconds
        refreshMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                refreshMessages(type);
                handler.postDelayed(this, POLL_INTERVAL);
            }
        };
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Set up button event handler which posts the entered message to Parse
    void setupMessagePosting(int type) {
        if (type == 1) {
            messages = messagesObj.getMessages();
        } else {
            messages = room.getMessages();
        }
        adapter = new ChatAdapter(messages, getContext(), ParseUser.getCurrentUser().getUsername());
        firstLoad = true;
        // Associate adapter with recycler view
        binding.rvChat.setAdapter(adapter);
        // Associate layout manager with recycler view
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvChat.setLayoutManager(linearLayoutManager);
        // When send button is clicked, create message object on Parse
        binding.ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the message the user typed in
                String data = binding.etMessage.getText().toString();
                // Put the message into a JSON object to put to a server
                JSONObject message = new JSONObject();
                try {
                    message.put("username", ParseUser.getCurrentUser().getUsername());
                    message.put("body", data);
                    message.put("profileImageUrl", ParseUser.getCurrentUser().getParseFile("profileImage").getUrl());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (type == 0) {
                    // Put the messages in the room object
                    JSONArray messages = room.getMessages();
                    messages.put(message);
                    room.setMessages(messages);
                    // Upload the message object to the server
                    room.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.i(TAG, "Message loaded to server");
                                refreshMessages(type);
                            } else {
                                Log.e(TAG, "Failed to save message", e);
                            }
                        }
                    });
                } else {
                    // Put the messages in the messages object
                    JSONArray messages = messagesObj.getMessages();
                    messages.put(message);
                    messagesObj.setMessages(messages);
                    // Upload the message object to the server
                    messagesObj.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.i(TAG, "Message loaded to server");
                                refreshMessages(type);
                            } else {
                                Log.e(TAG, "Failed to save message", e);
                            }
                        }
                    });
                }
                // Empty the message bar
                binding.etMessage.setText(null);
            }
        });
    }

    void refreshMessages(int type) {
        if (type == 0) {
            Context context = getContext();
            // Specify which class to query
            ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
            // Specify the object id
            query.getInBackground(room.getObjectId(), new GetCallback<Room>() {
                public void done(Room r, ParseException e) {
                    room = r;
                    try {
                        if (Constant.kicked(context, room, ParseUser.getCurrentUser().getObjectId())) {
                            return;
                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                    if (e == null) {
                        while (messages.length() > 0) {
                            messages.remove(0);
                        }
                        JSONArray updated = r.getMessages();
                        for (int i = 0; i < updated.length(); i++) {
                            try {
                                messages.put(updated.getJSONObject(i));
                            } catch (JSONException jsonException) {
                                jsonException.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (firstLoad) {
                            binding.rvChat.scrollToPosition(0);
                            firstLoad = false;
                        }
                    } else {
                        Log.e(TAG, "Error loading messages", e);
                    }
                }
            });
        } else {
            // Specify which class to query
            ParseQuery<Messages> query = ParseQuery.getQuery(Messages.class);
            // Specify the object id
            query.getInBackground(messagesObj.getObjectId(), new GetCallback<Messages>() {
                public void done(Messages m, ParseException e) {
                    messagesObj = m;
                    if (e == null) {
                        while (messages.length() > 0) {
                            messages.remove(0);
                        }
                        JSONArray updated = m.getMessages();
                        for (int i = 0; i < updated.length(); i++) {
                            try {
                                messages.put(updated.getJSONObject(i));
                            } catch (JSONException jsonException) {
                                jsonException.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (firstLoad) {
                            binding.rvChat.scrollToPosition(0);
                            firstLoad = false;
                        }
                    } else {
                        Log.e(TAG, "Error loading messages", e);
                    }
                }
            });
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // Only start checking for new messages when the app becomes active in foreground
        handler.postDelayed(refreshMessagesRunnable, POLL_INTERVAL);
    }

    @Override
    public void onPause() {
        // Stop background task from refreshing messages, to avoid unnecessary traffic & battery drain
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }
}