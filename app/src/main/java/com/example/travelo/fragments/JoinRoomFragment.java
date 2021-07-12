package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.databinding.FragmentJoinRoomBinding;

public class JoinRoomFragment extends DialogFragment {


    FragmentJoinRoomBinding binding;

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

        binding.btnEnter.setOnClickListener(v -> join());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void join() {
        if (binding.etRoomId.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Must enter a room id", Toast.LENGTH_SHORT).show();
            return;
        }
        dismiss();
        //Intent intent = new Intent(getContext(), CreateRoomActivity.class);
        //startActivity(intent);
    }
}