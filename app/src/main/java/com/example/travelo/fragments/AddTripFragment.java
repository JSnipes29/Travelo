package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.databinding.FragmentAddTripBinding;


public class AddTripFragment extends Fragment {

    FragmentAddTripBinding binding;

    public AddTripFragment() {
        // Required empty public constructor
    }


    public static AddTripFragment newInstance() {
        AddTripFragment fragment = new AddTripFragment();
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
        binding = FragmentAddTripBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        binding.btnCreateTrip.setOnClickListener((v -> createTrip()));

        binding.btnJoinTrip.setOnClickListener(v ->
            joinTrip()
        );
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void joinTrip() {
        //Launch join trip fragment
        FragmentManager fm = getParentFragmentManager();
        JoinRoomFragment joinRoom = new JoinRoomFragment();
        joinRoom.show(fm, "JoinRoom");
    }

    public void createTrip() {
        //Launch create room fragment
        FragmentManager fm = getParentFragmentManager();
        CreateRoomFragment joinRoom = new CreateRoomFragment();
        joinRoom.show(fm, "JoinRoom");
    }
}