package com.example.travelo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.adapters.YelpAdapter;
import com.example.travelo.databinding.ActivityYelpLocationsBinding;
import com.example.travelo.models.YelpBusinesses;
import com.example.travelo.models.YelpSearchResult;

import java.util.ArrayList;
import java.util.List;

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

    ActivityYelpLocationsBinding binding;
    List<YelpBusinesses> businesses;
    YelpAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYelpLocationsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        businesses = new ArrayList<>();
        adapter = new YelpAdapter(this, businesses);
        binding.rvBusinesses.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvBusinesses.setLayoutManager(linearLayoutManager);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        YelpService yelpService = retrofit.create(YelpService.class);
        yelpService.searchLocations("Bearer " + API_KEY,39.1, -75.5).enqueue(new Callback<YelpSearchResult>() {
            @Override
            public void onResponse(Call<YelpSearchResult> call, Response<YelpSearchResult> response) {
                Log.i(TAG, "onResponse: " + response);
                YelpSearchResult body = response.body();
                if (body == null) {
                    Log.i(TAG, "Invalid response from Yelp");
                    return;
                }
                businesses.clear();
                businesses.addAll(body.getBusinesses());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<YelpSearchResult> call, Throwable t) {
                Log.e(TAG, "onFailure", t);
            }
        });

    }
}