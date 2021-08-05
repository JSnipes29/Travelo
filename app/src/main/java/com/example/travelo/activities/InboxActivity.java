package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;

import com.example.travelo.R;
import com.example.travelo.databinding.ActivityInboxBinding;
import com.example.travelo.fragments.InboxFragment;
import com.example.travelo.fragments.ProfileFragment;

public class InboxActivity extends AppCompatActivity {

    ActivityInboxBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInboxBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        Bundle bundle = new Bundle();
        bundle.putBoolean("allMessages", getIntent().getBooleanExtra("allMessages", false));
        Fragment fragment = new InboxFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
    }
}