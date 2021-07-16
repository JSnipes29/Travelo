package com.example.travelo.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import com.example.travelo.PostMapActivity;
import com.example.travelo.R;
import com.example.travelo.YelpLocationsActivity;
import com.example.travelo.adapters.CustomWindowAdapter;
import com.example.travelo.databinding.FragmentEditMapBinding;
import com.example.travelo.models.Room;
import com.example.travelo.models.YelpBusinesses;
import com.example.travelo.models.YelpLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class EditMapFragment extends Fragment implements GoogleMap.OnMapLongClickListener{

    public static final String TAG = "EditMapFragment";
    public static final int YELP_CODE = 200;
    FragmentEditMapBinding binding;
    MapView mapFragment;
    GoogleMap map;
    Room room;

    public EditMapFragment() {
        // Required empty public constructor
    }

    public static EditMapFragment newInstance() {
        EditMapFragment fragment = new EditMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditMapBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
        mapFragment = (MapView) view.findViewById(R.id.map);
        mapFragment.onCreate(savedInstanceState);
        mapFragment.onResume();
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater(), getContext()));
                    populateMap();
                }
            });
        } else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
        binding.btnReady.setOnClickListener(v -> {
            JSONObject users = room.getUsers();
            try {
                users.put(ParseUser.getCurrentUser().getUsername(), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            room.setUsers(users);
            room.saveInBackground();
            Intent intent = new Intent(getContext(), PostMapActivity.class);
            String id = room.getObjectId();
            intent.putExtra("id", id);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(getContext(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setOnMapLongClickListener(this);
        } else {
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapFragment.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Log.i(TAG, "Long Pressed Map");
        Intent intent = new Intent(getContext(), YelpLocationsActivity.class);
        intent.putExtra("lat",latLng.latitude);
        intent.putExtra("lon", latLng.longitude);
        startActivityForResult(intent, YELP_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == YELP_CODE) {
            Log.i(TAG, "Back from YELP");
            double lat = data.getDoubleExtra("lat",0.0);
            double lon = data.getDoubleExtra("lon",0.0);
            Log.i(TAG, "Lat: " + lat + " Long: " + lon);
            Bundle bundle = data.getExtras();
            List<YelpBusinesses> businesses = Parcels.unwrap(bundle.getParcelable("added"));
            Log.i(TAG, "Added Size: " + businesses.size());
            YelpBusinesses.setButtonAll(businesses, false);
            // Define color of marker icon
            BitmapDescriptor defaultMarker =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .icon(defaultMarker));
            marker.setTag(businesses);
            marker.setSnippet(ParseUser.getCurrentUser().getUsername());
            dropPinEffect(marker);
            // Populate the yelp data into a json object
            // Upload the json data to the server
            JSONObject jsonMarker = new JSONObject();
            try {
                jsonMarker.put("user", ParseUser.getCurrentUser().getUsername());
                jsonMarker.put("latitude", lat);
                jsonMarker.put("longitude", lon);
                JSONArray places = new JSONArray();
                for (int i = 0; i < businesses.size(); i++) {
                    YelpBusinesses business = businesses.get(i);
                    YelpLocation location = business.getLocation();
                    JSONObject place = new JSONObject();
                    place.put("name", business.getName());
                    place.put("rating", business.getRating());
                    place.put("num_ratings", business.getReviewCount());
                    place.put("image_url", business.getImageUrl());
                    place.put("price", business.getPrice());
                    place.put("category", business.getCategories().get(0).getTitle());
                    place.put("distance", business.getDistanceMeters());
                    place.put("address", location.getAddress());
                    place.put("city", location.getCity());
                    place.put("state", location.getState());
                    place.put("country", location.getCountry());
                    places.put(place);
                }
                jsonMarker.put("places", places);
                JSONObject jsonMap = room.getMap();
                JSONArray markers = jsonMap.getJSONArray("markers");
                markers.put(jsonMarker);
                jsonMap.put("markers", markers);
                room.setMap(jsonMap);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error loading marker to server", e);
            }
            room.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i(TAG, "Loaded marker to server");
                    } else {
                        Log.e(TAG, "Couldn't save markers to server", e);
                    }
                }
            });
        }
    }

    // Get the marker data from the Parse server and add it to the map
    public void populateMap() {
        JSONObject jsonMap = room.getMap();
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

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }
}