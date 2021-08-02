package com.example.travelo.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.travelo.fragments.AddTripFragment;
import com.example.travelo.fragments.HomeFragment;
import com.example.travelo.fragments.InboxFragment;
import com.example.travelo.fragments.ProfileFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFragmentPagerAdapter extends FragmentStateAdapter {

    private static final List<Fragment> BASE_FRAGMENTS = Arrays.asList(new HomeFragment(), new AddTripFragment(), ProfileFragment.newInstance() , new InboxFragment());
    private static final int HOME_POSITION = 0;
    private static final int ADD_POSITION = 1;
    private static final int PROFILE_POSITION = 2;
    private static final int INBOX_POSITION = 3;
    public static final int COUNT = BASE_FRAGMENTS.size();

    public MainFragmentPagerAdapter(@NonNull  FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return BASE_FRAGMENTS.get(position);
    }

    @Override
    public int getItemCount() {
        return COUNT;
    }

    @Override
    public long getItemId(int position) {
        if (position == HOME_POSITION
                && createFragment(position).equals(BASE_FRAGMENTS.get(position))) {
            return HOME_POSITION;
        } else if (position == ADD_POSITION
                && createFragment(position).equals(BASE_FRAGMENTS.get(position))) {
            return ADD_POSITION;
        } else if (position == PROFILE_POSITION
                && createFragment(position).equals(BASE_FRAGMENTS.get(position))) {
            return PROFILE_POSITION;
        }
        else if (position == INBOX_POSITION
                && createFragment(position).equals(BASE_FRAGMENTS.get(position))) {
            return INBOX_POSITION;
        }
        return createFragment(position).hashCode();
    }

}
