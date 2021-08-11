package com.example.travelo.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.databinding.FragmentRoomSettingsBinding;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import es.dmoral.toasty.Toasty;

public class RoomSettingsFragment extends DialogFragment {

    FragmentRoomSettingsBinding binding;
    String roomObjectId;

    public RoomSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoomSettingsBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        Context context = requireContext();
        roomObjectId = getArguments().getString("roomObjectId");
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.include("owner");
        roomQuery.getInBackground(roomObjectId, new GetCallback<Room>() {
            @Override
            public void done(Room room, ParseException e) {
                if (e != null) {
                    Toasty.error(context, "Error getting room data", Toast.LENGTH_SHORT, true).show();
                    dismiss();
                    return;
                }
                if (binding != null) {
                    // If user is owner, they can save settings
                    String ownerId = room.getOwner().getObjectId();
                    boolean inviteOnly = room.getIntiveOnly();
                    binding.cbInviteOnly.setChecked(inviteOnly);
                    if (ParseUser.getCurrentUser().getObjectId().equals(ownerId)) {
                        binding.btnSave.setVisibility(View.VISIBLE);
                        binding.btnSave.setOnClickListener(v -> saveSettings());
                    } else {
                        binding.cbInviteOnly.setClickable(false);
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    public void saveSettings() {
        if (binding == null) {
            return;
        }
        Context context = requireContext();
        boolean inviteOnly = binding.cbInviteOnly.isChecked();
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.getInBackground(roomObjectId, new GetCallback<Room>() {
            @Override
            public void done(Room room, ParseException e) {
                if (e != null) {
                    Toasty.error(context, "Error getting room data", Toast.LENGTH_SHORT, true).show();
                    dismiss();
                    return;
                }
                room.setInviteOnly(inviteOnly);
                room.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toasty.error(context, "Error getting room data", Toast.LENGTH_SHORT, true).show();
                        } else {
                            Toasty.success(context, "Successfully saved room settings", Toast.LENGTH_SHORT, true).show();
                        }
                        dismiss();
                    }
                });
            }
        });

    }
}