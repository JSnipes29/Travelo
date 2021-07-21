package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.databinding.FragmentSignupBinding;
import com.example.travelo.models.Inbox;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class SignupFragment extends DialogFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    public static final String TAG = "SignupFragment";
    FragmentSignupBinding binding;


    public SignupFragment() {
        // Required empty public constructor
    }


    public static SignupFragment newInstance(String param1) {
        SignupFragment fragment = new SignupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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
        binding = FragmentSignupBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnSignup.setOnClickListener((v) -> {
                Log.i(TAG, "User signed up");
                // Create new user
                ParseUser user = new ParseUser();
                // Set core properties
                user.setUsername(binding.etUsername.getText().toString());
                user.setPassword(binding.etPassword.getText().toString());
                Inbox inbox = new Inbox();
                inbox.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        user.put(Inbox.KEY, inbox);
                        ParseObject followers = ParseObject.create("Followers");
                        followers.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                user.put("followers", followers);
                                // Invoke sign up in background
                                user.signUpInBackground(exception -> {
                                    if (exception == null) {
                                        Log.i(TAG, "User successfully signed up");
                                        Toast.makeText(view.getContext(), "You made a new account", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.e(TAG, "Error signing up", exception);
                                    }
                                });
                            }
                        });

                    }
                });

                dismiss();
        });
        binding.etUsername.requestFocus();
    }
}