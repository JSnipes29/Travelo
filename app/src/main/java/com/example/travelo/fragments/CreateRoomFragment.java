package com.example.travelo.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.RoomActivity;
import com.example.travelo.databinding.FragmentCreateRoomBinding;
import com.example.travelo.models.Room;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateRoomFragment extends DialogFragment {

    FragmentCreateRoomBinding binding;
    public static final String TAG = "CreateRoomFragment";

    public CreateRoomFragment() {
        // Required empty public constructor
    }

    public static CreateRoomFragment newInstance() {
        CreateRoomFragment fragment = new CreateRoomFragment();
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
        binding = FragmentCreateRoomBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        binding.btnCreate.setOnClickListener(v -> create());
        return view;
    }

    public void create() {
        String roomId = binding.etRoomId.getText().toString();
        // Don't create room if id is empty
        if (roomId.isEmpty()) {
            Toast.makeText(getContext(), "Must enter a room id", Toast.LENGTH_SHORT).show();
            return;
        }
        Room room = new Room(roomId);
        room.setOwner(ParseUser.getCurrentUser());
        JSONArray users = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            obj.put("username", ParseUser.getCurrentUser().getUsername());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        users.put(obj);
        room.setUsers(users);
        room.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.i(TAG, "Done Creating Room");
            }
        });
        Toast.makeText(getContext(), "Created Trip!", Toast.LENGTH_SHORT).show();
        dismiss();
        Intent intent = new Intent(getContext(), RoomActivity.class);
        startActivity(intent);

    }
}