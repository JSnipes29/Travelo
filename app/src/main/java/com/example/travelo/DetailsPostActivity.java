package com.example.travelo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travelo.adapters.CustomWindowAdapter;
import com.example.travelo.adapters.UsersAdapter;
import com.example.travelo.databinding.ActivityDetailsPostBinding;
import com.example.travelo.models.Post;
import com.example.travelo.models.YelpBusinesses;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DetailsPostActivity extends AppCompatActivity {

    ActivityDetailsPostBinding binding;
    public static final String TAG = "DetailsPostActivity";
    Post post;
    MapView mapView;
    GoogleMap map;
    List<String[]> users;
    UsersAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsPostBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        post = (Post) Parcels.unwrap(getIntent().getParcelableExtra("post"));
        ParseUser user = post.getOwner();
        binding.tvName.setText(user.getUsername());
        binding.tvDescription.setText(post.getDescription());
        String url = user.getParseFile("profileImage").getUrl();
        if (url != null) {
            Glide.with(this)
                    .load(url)
                    .circleCrop()
                    .into(binding.ivProfileImage);
        }
        users = new ArrayList<>();
        JSONObject jsonUsers = post.getUsers();
        Iterator<String> iter = jsonUsers.keys();

        while(iter.hasNext()) {
            String key = iter.next();
            try {
                String imageUrl = jsonUsers.getString(key);
                String[] userArray = {key, imageUrl};
                users.add(userArray);
            } catch (JSONException e) {
                Log.e(TAG, "Error getting users", e);
            }
        }
        userAdapter = new UsersAdapter(this, users);
        binding.rvUsers.setAdapter(userAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvUsers.setLayoutManager(linearLayoutManager);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        if (mapView != null) {
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater(), DetailsPostActivity.this));
                    populateMap();
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

    }

    // Get the marker data from the Parse server and add it to the map
    public void populateMap() {
        JSONObject jsonMap = post.getMap();
        try {
            JSONArray markers = jsonMap.getJSONArray("markers");
            for (int i = 0; i < markers.length(); i++) {
                JSONObject jsonMarker = markers.getJSONObject(i);
                List<YelpBusinesses> businesses = new ArrayList<>();
                double latitude = jsonMarker.getDouble("latitude");
                double longitude = jsonMarker.getDouble("longitude");
                String user = jsonMarker.getString("user");
                JSONArray places = jsonMarker.getJSONArray("places");
                for (int j = 0; j < places.length(); j++) {
                    JSONObject place = places.getJSONObject(j);
                    String name = place.getString("name");
                    double rating = place.getDouble("rating");
                    int numRatings = place.getInt("num_ratings");
                    //String imageUrl = place.getString("image_url");
                    YelpBusinesses business = YelpBusinesses.makeBusiness(name, rating, numRatings, null);
                    businesses.add(business);
                }
                // Define color of marker icon
                BitmapDescriptor defaultMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .icon(defaultMarker));
                marker.setTag(businesses);
                marker.setSnippet(user);
            }
        } catch (JSONException e) {
            Log.e(TAG,"Error getting markers from server", e);
        }
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}