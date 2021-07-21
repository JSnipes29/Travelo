package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.travelo.R;
import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.databinding.FragmentInboxBinding;
import com.example.travelo.models.Inbox;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class InboxFragment extends Fragment {

    FragmentInboxBinding binding;
    InboxAdapter inboxAdapter;
    List<JSONObject> list;
    public static final String TAG = "InboxFragment";

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
        ((AppCompatActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        list = new ArrayList<>();
        inboxAdapter = new InboxAdapter(list, getContext());
        binding.rvInbox.setAdapter(inboxAdapter);
        LinearLayoutManager inboxLayoutManager = new LinearLayoutManager(getContext());
        binding.rvInbox.setLayoutManager(inboxLayoutManager);
        queryInbox(0);
        return view;
    }

    public void queryInbox(int parameter) {
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.include(Inbox.KEY);
        userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                jsonToList(jsonInbox);
            }
        });

    }

    public void jsonToList(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            try {
                list.add(array.getJSONObject(i));
            } catch (JSONException e) {
                Log.e(TAG, "Error loading data to json", e);

            }
        }
        inboxAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.inbox_menu, menu);
    }
}