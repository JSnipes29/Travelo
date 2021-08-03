package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
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

import com.example.travelo.R;
import com.example.travelo.adapters.MainFragmentPagerAdapter;
import com.example.travelo.databinding.ActivityMainBinding;
import com.example.travelo.fragments.AddTripFragment;
import com.example.travelo.fragments.HomeFragment;
import com.example.travelo.fragments.InboxFragment;
import com.example.travelo.fragments.ProfileFragment;
import com.parse.ParseUser;

import java.lang.reflect.Field;

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
        ParseUser.logOut();
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

}