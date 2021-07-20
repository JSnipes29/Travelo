package com.example.travelo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.travelo.databinding.ActivityMainBinding;
import com.example.travelo.fragments.AddTripFragment;
import com.example.travelo.fragments.HomeFragment;
import com.example.travelo.fragments.InboxFragment;
import com.example.travelo.fragments.ProfileFragment;
import com.example.travelo.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // handle navigation selection
        binding.bottomNavigation.setOnNavigationItemSelectedListener(
                item -> {
                    Fragment fragment;
                    Bundle bundle;
                    switch (item.getItemId()) {
                        case R.id.action_home:
                            fragment = new HomeFragment();
                            break;
                        case R.id.action_add_trip:
                            fragment = new AddTripFragment();
                            break;
                        case R.id.action_profile:
                            bundle = new Bundle();
                            bundle.putParcelable("user", null);
                            fragment = new ProfileFragment();
                            fragment.setArguments(bundle);
                            break;
                        case R.id.action_inbox:
                            fragment = new InboxFragment();
                            break;
                        default:
                            fragment = null;
                            break;
                    }
                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                    return true;
                });
        // Set default selection
        binding.bottomNavigation.setSelectedItemId(R.id.action_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void logout(MenuItem mi) {
        ParseUser.logOut();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}