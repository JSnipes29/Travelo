package com.example.travelo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;

import com.example.travelo.R;
import com.example.travelo.databinding.ActivityMessagesBinding;
import com.example.travelo.fragments.RoomMessagesFragment;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Messages;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

public class MessagesActivity extends AppCompatActivity {

    ActivityMessagesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        String messagesId = getIntent().getStringExtra("messagesId");
        ParseQuery<Messages> query = ParseQuery.getQuery(Messages.class);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        query.getInBackground(messagesId, new GetCallback<Messages>() {
            @Override
            public void done(Messages messages, ParseException e) {
                Fragment fragment = new RoomMessagesFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", 1);
                bundle.putParcelable("messages", Parcels.wrap(messages));
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });

    }
}