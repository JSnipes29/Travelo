package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.adapters.InviteAdapter;
import com.example.travelo.databinding.FragmentInviteBinding;
import com.example.travelo.models.Room;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class InviteFragment extends DialogFragment {

    FragmentInviteBinding binding;
    List<Room> rooms;
    InviteAdapter inviteAdapter;
    public static final String TAG = "InviteFragment";

    public InviteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInviteBinding.inflate(getLayoutInflater(),container, false);
        View view = binding.getRoot();
        // Set up recycler view
        rooms = new ArrayList<>();
        String userId = getArguments().getString("userId");
        inviteAdapter = new InviteAdapter(getContext(), rooms, userId, this);
        binding.rvRooms.setAdapter(inviteAdapter);
        LinearLayoutManager roomsLayoutManager = new LinearLayoutManager(getContext());
        binding.rvRooms.setLayoutManager(roomsLayoutManager);
        queryRooms();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void queryRooms() {
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.include(Room.KEY_OWNER);
        roomQuery.whereEqualTo(Room.KEY_OWNER, ParseUser.getCurrentUser());
        roomQuery.addDescendingOrder(Room.KEY_CREATED_AT);
        roomQuery.findInBackground(new FindCallback<Room>() {
            @Override
            public void done(List<Room> queriedRooms, ParseException e) {
                if (e != null) {
                    Log.i(TAG, "Error getting rooms");
                    dismiss();
                    return;
                }
                rooms.addAll(queriedRooms);
                inviteAdapter.notifyDataSetChanged();
            }
        });
    }
}