package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.adapters.SearchUserAdapter;
import com.example.travelo.databinding.ActivityUsersBinding;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class UsersActivity extends AppCompatActivity {

    ActivityUsersBinding binding;
    List<ParseUser> users;
    SearchUserAdapter adapter;
    public static final String TAG = "UsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        // Setup recycler view
        users = new ArrayList<>();
        adapter = new SearchUserAdapter(this, users);
        binding.rvUsers.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvUsers.setLayoutManager(linearLayoutManager);

        // Get the type, 0 == friends, 1 == followers, 2 == following
        int type = getIntent().getIntExtra("type", 0);
        queryCurrentUser(type);
    }

    public void queryCurrentUser(int parameter) {
        ParseQuery<ParseUser> currentUserQuery = ParseQuery.getQuery(ParseUser.class);
        currentUserQuery.include("followers");
        // Get the current user
        currentUserQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser currentUser, ParseException e) {
                ParseObject followers = currentUser.getParseObject("followers");
                JSONArray jsonUsers;
                if (parameter == 0) {
                    jsonUsers = followers.getJSONArray("friends");
                } else if (parameter == 1) {
                    jsonUsers = followers.getJSONArray("followers");
                } else if (parameter == 2) {
                    jsonUsers = currentUser.getJSONArray("following");
                } else {
                    return;
                }
                try {
                    getUsers(jsonUsers);
                } catch (JSONException jsonException) {
                    Log.e(TAG, "Error getting json data", jsonException);
                }
            }
        });
    }

    public void getUsers(JSONArray jsonUsers) throws JSONException {
        int numUsers = jsonUsers.length();
        if (numUsers == 0) {
            Toasty.info(this, "There are no users here", Toast.LENGTH_SHORT, true).show();
        }
        for (int i = 0; i < numUsers; i++) {
            String userId = jsonUsers.getString(i);
            ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
            userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    users.add(user);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}