package com.example.travelo;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("56FOAmOXvVk5GqnDojmLIpmnZW8SzMXr8JkIy4Cn")
                .clientKey("wsE9tdqbYL8gNd96TmlR791XKyOX5szd4ZruZpLZ")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
