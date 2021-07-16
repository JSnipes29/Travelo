package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.travelo.R;
import com.example.travelo.databinding.FragmentProfileBinding;
import com.example.travelo.models.Room;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    ParseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        user = (ParseUser) Parcels.unwrap(getArguments().getParcelable("user"));
        binding.tvName.setText(user.getUsername());
        Glide.with(getContext())
                .load(user.getParseFile("profileImage").getUrl())
                .circleCrop()
                .into(binding.ivProfileImage);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}