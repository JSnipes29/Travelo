package com.example.travelo;

import android.app.Application;

import com.example.travelo.models.Post;
import com.example.travelo.models.Room;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();
        // Register Parse Models
        ParseObject.registerSubclass(Room.class);
        ParseObject.registerSubclass(Post.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("56FOAmOXvVk5GqnDojmLIpmnZW8SzMXr8JkIy4Cn")
                .clientKey("wsE9tdqbYL8gNd96TmlR791XKyOX5szd4ZruZpLZ")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
