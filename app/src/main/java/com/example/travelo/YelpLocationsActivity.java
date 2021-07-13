package com.example.travelo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.travelo.models.YelpSearchResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class YelpLocationsActivity extends AppCompatActivity {

    public static final String BASE_URL = "https://api.yelp.com/v3/";
    public static final String API_KEY = "qSxMjBAZFTXrq4tPrZTAr1ZqAs9hJ9zOnjcUCV7HLMxmb-VsgE9rpi6zdEI2uQZzpgMFAKDbX-8Qg7fDCZurv8D6XxWA7dGhMI0gFNm2M0cM6eGAWpYpBxgwgALuYHYx";
    public static final String TAG = "YelpLocationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelp_locations);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        YelpService yelpService = retrofit.create(YelpService.class);
        yelpService.searchLocations("Bearer " + API_KEY,39.1, -75.5).enqueue(new Callback<YelpSearchResult>() {
            @Override
            public void onResponse(Call<YelpSearchResult> call, Response<YelpSearchResult> response) {
                Log.i(TAG, "onResponse: " + response);
            }

            @Override
            public void onFailure(Call<YelpSearchResult> call, Throwable t) {
                Log.e(TAG, "onFailure", t);
            }
        });

    }
}