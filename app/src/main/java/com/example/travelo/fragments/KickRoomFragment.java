package com.example.travelo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.adapters.NameAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.FragmentKickRoomBinding;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KickRoomFragment extends DialogFragment {

    FragmentKickRoomBinding binding;
    List<String> users;
    NameAdapter adapter;
    String roomId;
    public static final String TAG = "KickRoomFragment";
    public KickRoomFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentKickRoomBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        roomId = getArguments().getString("roomObjectId");
        users = new ArrayList<>();
        adapter = new NameAdapter(getContext(), users);
        binding.rvUsers.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvUsers.setLayoutManager(linearLayoutManager);
        getUsers();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void getUsers() {
        Context context = getContext();
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.getInBackground(roomId, new GetCallback<Room>() {
            @Override
            public void done(Room room, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting room data", e);
                    dismiss();
                    return;
                }
                JSONObject jsonUsers = room.getUsers();
                Iterator<String> iterator = jsonUsers.keys();
                // Get a list of unready users
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    try {
                        boolean ready = jsonUsers.getBoolean(key);
                        if (!ready) {
                            users.add(key);
                        }
                    } catch (JSONException jsonException) {
                        Log.e(TAG, "Couldn't load ready data from server", jsonException);
                    }
                }
                adapter.notifyDataSetChanged();
                Constant.setupKickSwipe(context, users, adapter, binding.rvUsers,roomId);
            }
        });
    }
}