package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.databinding.FragmentInboxBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class InboxFragment extends Fragment {

    FragmentInboxBinding binding;
    InboxAdapter inboxAdapter;
    List<JSONObject> list;

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();

        list = new ArrayList<>();
        inboxAdapter = new InboxAdapter(list, getContext());
        binding.rvInbox.setAdapter(inboxAdapter);
        LinearLayoutManager inboxLayoutManager = new LinearLayoutManager(getContext());
        binding.rvInbox.setLayoutManager(inboxLayoutManager);
        return view;
    }

    public void queryInbox() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}