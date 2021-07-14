package com.example.travelo;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.travelo.adapters.YelpAdapter;
import com.example.travelo.databinding.ActivityYelpLocationsBinding;
import com.example.travelo.models.YelpBusinesses;
import com.example.travelo.models.YelpSearchResult;

import org.parceler.Parcels;

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
        double lat = getIntent().getDoubleExtra("lat", 0.0);
        double lon = getIntent().getDoubleExtra("lon",0.0);
        Log.i(TAG, "Lat: " + lat + " Lon: " + lon);
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
        yelpService.searchLocations("Bearer " + API_KEY,lat, lon).enqueue(new Callback<YelpSearchResult>() {
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

        // This callback will only be called when the activity is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                List<YelpBusinesses> added = new ArrayList<>();
                for (int i = 0; i < businesses.size(); i++) {
                    YelpBusinesses business = businesses.get(i);
                    if (business.getAdded()) {
                        added.add(business);
                    }
                }
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putParcelable("added", Parcels.wrap(added));
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();

            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }
}