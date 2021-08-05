package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.adapters.MainFragmentPagerAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.ActivityMainBinding;
import com.example.travelo.fragments.AddTripFragment;
import com.example.travelo.fragments.CreateRoomFragment;
import com.example.travelo.fragments.HomeFragment;
import com.example.travelo.fragments.InboxFragment;
import com.example.travelo.fragments.JoinRoomFragment;
import com.example.travelo.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.lang.reflect.Field;

import es.dmoral.toasty.Toasty;
import shortbread.Shortcut;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    MainFragmentPagerAdapter pagerAdapter;
    boolean swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        if (checkUser()) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }
        final FragmentManager fragmentManager = getSupportFragmentManager();
        pagerAdapter = new MainFragmentPagerAdapter(fragmentManager, getLifecycle());
        binding.viewPager.setAdapter(pagerAdapter);
        swipe = false;
        // handle navigation selection
        binding.bottomNavigation.setOnNavigationItemSelectedListener(
                item -> {
                    int currentTabPosition = getPage(item.getItemId());
                    binding.viewPager.setCurrentItem(currentTabPosition, swipe);
                    return true;
                });
        // Set default selection
        binding.bottomNavigation.setSelectedItemId(R.id.action_home);
        binding.viewPager.setCurrentItem(0);
        pagerAdapter.notifyDataSetChanged();

        // Reduce swipe sensitivity between fragments
        try {
            final Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);

            final RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(binding.viewPager);

            final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);

            final int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 4);
        } catch (Exception ignore) {
        }

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.i("Main", "Position: " + position);
                swipe = true;
                binding.bottomNavigation.setSelectedItemId(getItemId(position));
                swipe = false;
            }
        });

    }


    @Override
    public void onBackPressed() {
        return;
    }

    public void logout(MenuItem mi) {
        ParseObject.unpinAllInBackground();
        ParseUser.logOutInBackground();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public int getPage(int itemId) {
        int currentTabPosition;
        switch (itemId) {
            case R.id.action_home:
                currentTabPosition = 0;
                break;
            case R.id.action_add_trip:
                currentTabPosition = 1;
                break;
            case R.id.action_profile:
                currentTabPosition = 2;
                break;
            case R.id.action_inbox:
                currentTabPosition = 3;
                break;
            default:
                currentTabPosition = 0;
                break;
        }
        return currentTabPosition;
    }

    public int getItemId(int position) {
        int id;
        switch (position) {
            case 0:
                id = R.id.action_home;
                break;
            case 1:
                id = R.id.action_add_trip;
                break;
            case 2:
                id = R.id.action_profile;
                break;
            case 3:
                id = R.id.action_inbox;
                break;
            default:
                id = R.id.action_home;
                break;
        }
        return id;
    }

    public void shortcut(int shortcut) {
        FragmentManager fm = getSupportFragmentManager();
        switch (shortcut) {
            case Constant.CREATE_MAP_SHORTCUT:
                //Launch create room fragment
                CreateRoomFragment createRoomFragment = new CreateRoomFragment();
                createRoomFragment.show(fm, "CreateRoom");
                break;
            case Constant.JOIN_MAP_SHORTCUT:
                //Launch join room fragment
                JoinRoomFragment joinRoomFragment = new JoinRoomFragment();
                joinRoomFragment.show(fm, "JoinRoom");
                break;
            default:
                return;
        }
    }

    @Shortcut(id = "create_map", icon = R.drawable.ic_baseline_add_location_alt_24, shortLabel = "Create Map")
    public void createTrip() {
        shortcut(Constant.CREATE_MAP_SHORTCUT);
    }

    @Shortcut(id = "join_map", icon = R.drawable.ic_baseline_add_location_alt_24, shortLabel = "Join Map")
    public void joinTrip() {
        shortcut(Constant.JOIN_MAP_SHORTCUT);
    }

    // Checks if a user is logged in
    public boolean checkUser() {
        if (ParseUser.getCurrentUser() == null) {
            Toasty.error(this, "Must be logged in", Toast.LENGTH_SHORT, true).show();
            return true;
        }
        return false;
    }

    // Opens drawer
    public void openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    public NavigationView getNavigationView() {
        return binding.nvView;
    }

    public void setupDrawer(int menu) {
        binding.nvView.getMenu().clear();
        binding.nvView.inflateMenu(menu);
    }

}