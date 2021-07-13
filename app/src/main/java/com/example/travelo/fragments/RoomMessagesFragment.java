package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.databinding.FragmentRoomMessagesBinding;
import com.example.travelo.models.Room;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;


public class RoomMessagesFragment extends Fragment {

    public static final String TAG = "RoomMessagesFragment";
    Room room;

    FragmentRoomMessagesBinding binding;

    public RoomMessagesFragment() {
        // Required empty public constructor
    }

    public static RoomMessagesFragment newInstance(String param1, String param2) {
        RoomMessagesFragment fragment = new RoomMessagesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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

        room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
        Log.i(TAG, "Room id: " + room.getRoomId());
        setupMessagePosting();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Set up button event handler which posts the entered message to Parse
    void setupMessagePosting() {

        // When send button is clicked, create message object on Parse
        binding.ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = binding.etMessage.getText().toString();
                JSONObject message = new JSONObject();
                try {
                    message.put("username", ParseUser.getCurrentUser().getUsername());
                    message.put("body", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray messages = room.getMessages();
                messages.put(message);
                room.setMessages(messages);
                room.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i(TAG, "Message loaded to server");
                        } else {
                            Log.e(TAG, "Failed to save message", e);
                        }
                    }
                });
                binding.etMessage.setText(null);
            }
        });
    }
}