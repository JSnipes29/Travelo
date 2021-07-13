package com.example.travelo.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.RoomActivity;
import com.example.travelo.databinding.FragmentJoinRoomBinding;
import com.example.travelo.models.Room;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JoinRoomFragment extends DialogFragment {


    FragmentJoinRoomBinding binding;
    public static final String TAG = "JoinRoomFragment";

    public JoinRoomFragment() {
        // Required empty public constructor
    }


    public static JoinRoomFragment newInstance() {
        JoinRoomFragment fragment = new JoinRoomFragment();
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
        binding = FragmentJoinRoomBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        binding.btnEnter.setOnClickListener(v -> join(v));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void join(View v) {
        String roomId = binding.etRoomId.getText().toString();
        if (roomId.isEmpty()) {
            Toast.makeText(getContext(), "Must enter a room id", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        // Specify which class to query
        ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
        // Get Room with id
        query.whereEqualTo("roomId", roomId);
        // Find the room asynchronously
        query.findInBackground((rooms, error) -> {
            if (error != null) {
                Log.e(TAG, "Error joining room", error);
                dismiss();
                return;
            }
            // Check if it exists
            if (rooms.isEmpty()) {
                Toast.makeText(v.getContext(), "No room with id: " + roomId, Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }

            dismiss();
            Log.i(TAG, String.valueOf(getContext()));
            Intent intent = new Intent(v.getContext(), RoomActivity.class);
            String id = rooms.get(0).getObjectId();
            intent.putExtra("room", id);
            startActivity(intent);
        });
    }
}