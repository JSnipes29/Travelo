package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.adapters.InviteFriendsAdapter;
import com.example.travelo.databinding.FragmentInviteFriendsBinding;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class InviteFriendsFragment extends DialogFragment {

    FragmentInviteFriendsBinding binding;
    String roomObjectId;
    List<String> users;
    InviteFriendsAdapter adapter;
    public static final String TAG = "InviteFriendsFragment";
    public InviteFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInviteFriendsBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        roomObjectId = getArguments().getString("roomObjectId");
        users = new ArrayList<>();
        adapter = new InviteFriendsAdapter(getContext(), users, roomObjectId, this);
        binding.rvFriends.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvFriends.setLayoutManager(linearLayoutManager);
        queryFriends();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void queryFriends() {
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.include("followers");
        userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser currentUser, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Trouble loading current user data");
                    Toasty.error(getContext(), "Trouble loading current user data", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                JSONArray friends = currentUser.getParseObject("followers").getJSONArray("friends");
                try {
                    for(int i = 0; i < friends.length(); i++) {
                        String userId = friends.getString(i);
                        users.add(userId);
                    }
                } catch (JSONException jsonException) {
                    Log.e(TAG, "Error getting json data", jsonException);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}