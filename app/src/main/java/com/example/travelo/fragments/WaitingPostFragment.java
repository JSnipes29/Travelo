package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.adapters.NameAdapter;
import com.example.travelo.databinding.FragmentWaitingPostBinding;
import com.example.travelo.models.Room;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaitingPostFragment extends Fragment {

    public static final String TAG = "WaitingPostFragment";

    FragmentWaitingPostBinding binding;
    Room room;
    NameAdapter adapter;

    public WaitingPostFragment() {
        // Required empty public constructor
    }

    public static WaitingPostFragment newInstance() {
        WaitingPostFragment fragment = new WaitingPostFragment();
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
        binding = FragmentWaitingPostBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
        JSONObject jsonUsers = room.getUsers();
        List<String> users = new ArrayList<>();
        Iterator<String> iterator = jsonUsers.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                boolean ready = jsonUsers.getBoolean(key);
                if (!ready) {
                    users.add(key);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Couldn't load ready data from server", e);
            }
        }
        adapter = new NameAdapter(getContext(), users);
        binding.rvNames.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rvNames.setLayoutManager(linearLayoutManager);
        return view;
    }
}