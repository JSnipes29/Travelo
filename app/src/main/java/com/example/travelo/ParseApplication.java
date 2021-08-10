package com.example.travelo;

import android.app.Application;

import com.example.travelo.models.Inbox;
import com.example.travelo.models.Messages;
import com.example.travelo.models.Post;
import com.example.travelo.models.Room;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();
        // Register Parse Models
        ParseObject.registerSubclass(Room.class);
        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Inbox.class);
        ParseObject.registerSubclass(Messages.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.parse_app_id))
                .clientKey(getString(R.string.parse_client_key))
                .server(getString(R.string.parse_api_address))
                .enableLocalDataStore()
                .build()
        );
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("GCMSenderId", getString(R.string.gcm_sender_id));
        installation.saveInBackground();
    }
}
