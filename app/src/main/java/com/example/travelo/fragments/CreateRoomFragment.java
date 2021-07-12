package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.databinding.FragmentCreateRoomBinding;

public class CreateRoomFragment extends DialogFragment {

    FragmentCreateRoomBinding binding;

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

        return view;
    }
}