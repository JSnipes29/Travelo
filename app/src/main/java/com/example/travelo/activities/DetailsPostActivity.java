package com.example.travelo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travelo.R;
import com.example.travelo.adapters.CommentAdapter;
import com.example.travelo.adapters.CustomWindowAdapter;
import com.example.travelo.adapters.UsersAdapter;
import com.example.travelo.databinding.ActivityDetailsPostBinding;
import com.example.travelo.models.MarkerTag;
import com.example.travelo.models.Post;
import com.example.travelo.models.YelpBusinesses;
import com.example.travelo.models.YelpLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DetailsPostActivity extends AppCompatActivity {

    ActivityDetailsPostBinding binding;
    public static final String TAG = "DetailsPostActivity";
    Post post;
    MapView mapView;
    GoogleMap map;
    List<String[]> users;
    UsersAdapter userAdapter;
    JSONArray comments;
    CommentAdapter commentAdapter;

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
            binding.ivProfileImage.setClickable(true);
            binding.ivProfileImage.setOnClickListener(v -> ProfileActivity.goToProfile(this, user.getUsername()));
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
        // Bind the users who were in the room
        userAdapter = new UsersAdapter(this, users);
        binding.rvUsers.setAdapter(userAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.rvUsers.setLayoutManager(linearLayoutManager);

        // Bind the comments
        comments = post.getComments();
        commentAdapter = new CommentAdapter(this, comments);
        binding.rvComments.setAdapter(commentAdapter);
        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(this);
        binding.rvComments.setLayoutManager(commentLayoutManager);

        // Set up adding comments
        binding.btnSend.setOnClickListener(v -> {
            String comment = binding.etComment.getText().toString();
            if (comment.isEmpty()) {
                Toast.makeText(DetailsPostActivity.this, "Can't post empty comment", Toast.LENGTH_SHORT).show();
                return;
            }
            String username = ParseUser.getCurrentUser().getUsername();
            JSONObject jsonComment = new JSONObject();
            try {
                jsonComment.put("comment", comment);
                jsonComment.put("username", username);
            } catch (JSONException e) {
                Log.e(TAG, "Couldn't updated json object with comment", e);
            }
            JSONArray jsonComments = post.getComments();
            jsonComments.put(jsonComment);
            post.setComments(jsonComments);
            post.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Problem loading comment to server", e);
                        return;
                    }
                    Toasty.success(DetailsPostActivity.this, "Comment uploaded successfully", Toast.LENGTH_SHORT, true).show();
                    JSONArray updated = post.getComments();
                    comments = updated;
                    commentAdapter.update(comments);
                }
            });
            binding.etComment.setText("");
        });

        // Set up the map from the server
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
                    map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(@NonNull Marker marker) {
                            MarkerTag tag = (MarkerTag) marker.getTag();
                            // Define color of marker icon
                            BitmapDescriptor defaultMarker =
                                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                            Marker replace = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(tag.getLatitude(), tag.getLongitude()))
                                    .icon(defaultMarker));
                            replace.setTag(tag);
                            replace.setSnippet(marker.getSnippet());
                            replace.setDraggable(true);
                            Intent intent = new Intent(DetailsPostActivity.this, YelpLocationsActivity.class);
                            intent.putExtra("markerData", Parcels.wrap((MarkerTag)marker.getTag()));
                            marker.remove();
                            startActivity(intent);
                        }

                        @Override
                        public void onMarkerDrag(@NonNull Marker marker) {

                        }

                        @Override
                        public void onMarkerDragEnd(@NonNull Marker marker) {

                        }
                    });
                }
            });
        } else {
            Toasty.error(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT, true).show();
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
                    String imageUrl = place.getString("image_url");
                    String price = place.getString("price");
                    double distanceMeters = place.getDouble("distance");
                    String category = place.getString("category");
                    String address = place.getString("address");
                    String city = place.getString("city");
                    String state = place.getString("state");
                    String country = place.getString("country");
                    YelpLocation location = YelpLocation.makeLocation(address, city, state, country);
                    YelpBusinesses business = YelpBusinesses.makeBusiness(name, rating, numRatings, imageUrl, price, distanceMeters, location, category);
                    businesses.add(business);
                }
                // Define color of marker icon
                BitmapDescriptor defaultMarker =
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .icon(defaultMarker));
                marker.setTag(new MarkerTag(businesses, latitude, longitude));
                marker.setSnippet(user);
                marker.setDraggable(true);
            }
        } catch (JSONException e) {
            Log.e(TAG,"Error getting markers from server", e);
        }

    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toasty.success(this, "Map was loaded properly!", Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(this, "Error - Map was null!!", Toast.LENGTH_SHORT, true).show();
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