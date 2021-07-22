package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.travelo.R;
import com.example.travelo.databinding.ActivityProfileBinding;
import com.example.travelo.fragments.ProfileFragment;
import com.example.travelo.models.Inbox;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", getIntent().getParcelableExtra("parseUser"));
        Fragment fragment = new ProfileFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();

    }

    public static void goToProfile(Context context, String userid) {
        Intent intent = new Intent(context, ProfileActivity.class);
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo("username", userid);
        query.setLimit(1);
        query.include(Inbox.KEY);
        query.include("followers");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e("UsersAdapter","Couldn't retrieve user",e);
                    return;
                }
                if (objects.isEmpty()) {
                    Log.i("UsersAdapter", "Couldn't find user");
                    return;
                }
                ParseUser parseUser = objects.get(0);
                Log.i("UsersAdapter","Found user: " + parseUser.getObjectId());
                intent.putExtra("parseUser", Parcels.wrap(parseUser));
                context.startActivity(intent);
            }
        });

    }
}