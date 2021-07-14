package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.databinding.FragmentWaitingPostBinding;

public class WaitingPostFragment extends Fragment {

    FragmentWaitingPostBinding binding;

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
        // Inflate the layout for this fragment
        return view;
    }
}