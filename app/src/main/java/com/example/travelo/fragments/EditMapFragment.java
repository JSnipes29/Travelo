package com.example.travelo.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.travelo.activities.DetailsPostActivity;
import com.example.travelo.activities.PostMapActivity;
import com.example.travelo.R;
import com.example.travelo.activities.RoomActivity;
import com.example.travelo.activities.YelpLocationsActivity;
import com.example.travelo.adapters.CustomWindowAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.FragmentEditMapBinding;
import com.example.travelo.models.MarkerTag;
import com.example.travelo.models.Room;
import com.example.travelo.models.YelpBusinesses;
import com.example.travelo.models.YelpLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class EditMapFragment extends Fragment implements GoogleMap.OnMapLongClickListener{

    public static final String TAG = "EditMapFragment";
    public static final int YELP_CODE = 200;
    FragmentEditMapBinding binding;
    MapView mapFragment;
    GoogleMap map;
    Room room;
    String color;
    boolean owner = false;
    public static final long POLL_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    Handler handler = new Handler();
    Runnable refreshMapRunnable;
    HashSet<LatLng> markerSet;

    public EditMapFragment() {
        // Required empty public constructor
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
        refreshMapRunnable = new Runnable() {
            @Override
            public void run() {
                refreshMap();
                handler.postDelayed(this, POLL_INTERVAL);
            }
        };
        markerSet = new HashSet<>();
        room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
        mapFragment = (MapView) view.findViewById(R.id.map);
        mapFragment.onCreate(savedInstanceState);
        mapFragment.onResume();
        if (mapFragment != null) {
            Context context = getContext();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater(), getContext()));
                    populateMap();
                    map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(@NonNull Marker marker) {
                            Log.i(TAG, "Removing marker");
                            // Pause the refresher
                            pauseRefreshMap();
                            MarkerTag tag = (MarkerTag) marker.getTag();
                            LatLng position = new LatLng(tag.getLatitude(), tag.getLongitude());
                            marker.remove();
                            ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
                            roomQuery.getInBackground(room.getObjectId(), new GetCallback<Room>() {
                                @Override
                                public void done(Room updatedRoom, ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Trouble updating room data", e);
                                        Toasty.error(context, "Error removing marker", Toast.LENGTH_SHORT, true).show();
                                        resumeRefreshMap();
                                        return;
                                    }
                                    room = updatedRoom;
                                    JSONObject jsonMap = room.getMap();
                                    try {
                                        JSONArray markers = jsonMap.getJSONArray("markers");
                                        for (int i = 0; i < markers.length(); i++) {
                                            JSONObject jsonMarker = markers.getJSONObject(i);
                                            double latitude = jsonMarker.getDouble("latitude");
                                            double longitude = jsonMarker.getDouble("longitude");
                                            LatLng jsonPosition = new LatLng(latitude, longitude);
                                            Log.i(TAG, "json:" + latitude + " " + longitude);
                                            Log.i(TAG, "delete:" + tag.getLatitude() + " " + tag.getLongitude());
                                            if (jsonPosition.equals(position)) {
                                                markers.remove(i);
                                                jsonMap.put("markers", markers);
                                                room.setMap(jsonMap);
                                                room.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        resumeRefreshMap();
                                                        if (e != null) {
                                                            Log.e(TAG, "Trouble updating room data", e);
                                                            Toasty.error(context, "Error removing marker", Toast.LENGTH_SHORT, true).show();
                                                            return;
                                                        }
                                                        Toasty.success(context, "Removed marker", Toast.LENGTH_SHORT, true).show();
                                                    }
                                                });
                                                return;
                                            }
                                        }
                                    } catch (JSONException jsonException) {
                                        Log.e(TAG,"Error getting markers from server", jsonException);
                                    }
                                }
                            });

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
            Toasty.error(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT, true).show();
        }
        // When ready go to the post map activity
        binding.btnReady.setOnClickListener(v -> {
            ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
            roomQuery.getInBackground(room.getObjectId(), new GetCallback<Room>() {
                @Override
                public void done(Room object, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error readying up", e);
                        Toasty.error(getContext(), "", Toasty.LENGTH_SHORT, true).show();
                    }
                    JSONObject users = room.getUsers();
                    try {
                        users.put(ParseUser.getCurrentUser().getUsername(), true);
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                    room.setUsers(users);
                    room.saveInBackground();
                    Intent intent = new Intent(getContext(), PostMapActivity.class);
                    String id = room.getObjectId();
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });

        });
        // Search for a location when the button is pressed
        binding.ibSearch.setOnClickListener(v -> {
            String address = binding.etSearch.getText().toString();
            if (address.isEmpty()) {
                return;
            }
            Geocoder geocoder = new Geocoder(getContext());
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocationName(address, 1);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't search for location" , e);
            }
            if (addressList == null) {
                Toasty.error(getContext(), "Place not found", Toast.LENGTH_SHORT, true).show();
                return;
            }
            Address place = addressList.get(0);
            LatLng latlng = new LatLng(place.getLatitude(), place.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            map.animateCamera(CameraUpdateFactory.zoomTo(14f));
        });

        // Set the spinner up to change the map type
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (map != null) {
                    int type = 1;
                    switch (position) {
                        case 0:
                            type = GoogleMap.MAP_TYPE_NORMAL;
                            break;
                        case 1:
                            type = GoogleMap.MAP_TYPE_SATELLITE;
                            break;
                        case 2:
                            type = GoogleMap.MAP_TYPE_TERRAIN;
                            break;
                        case 3:
                            type = GoogleMap.MAP_TYPE_HYBRID;
                            break;
                        default:
                            break;
                    }
                    map.setMapType(type);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        color = "green";
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
            Toasty.success(getContext(), "Map was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setOnMapLongClickListener(this);
        } else {
            Toasty.error(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT, true).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
        resumeRefreshMap();
    }

    @Override
    public void onPause() {
        pauseRefreshMap();
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
        setColor();
        intent.putExtra("color", color);
        startActivityForResult(intent, YELP_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == YELP_CODE) {
            Log.i(TAG, "Back from YELP");
            double lat = data.getDoubleExtra("lat",0.0);
            double lon = data.getDoubleExtra("lon",0.0);
            String markerColor = data.getStringExtra("color");
            Log.i(TAG, "Lat: " + lat + " Long: " + lon + " Color: " + markerColor);
            Bundle bundle = data.getExtras();
            List<YelpBusinesses> businesses = Parcels.unwrap(bundle.getParcelable("added"));
            Log.i(TAG, "Added Size: " + businesses.size());
            YelpBusinesses.setButtonAll(businesses, false);
            // Define color of marker icon
            BitmapDescriptor defaultMarker = MarkerTag.colorMarker(markerColor);
            LatLng position = new LatLng(lat, lon);
            MarkerTag tag = new MarkerTag(businesses, lat, lon, color);
            markerSet.add(position);
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(defaultMarker));
            marker.setTag(tag);
            marker.setSnippet(ParseUser.getCurrentUser().getUsername());
            marker.setDraggable(true);
            dropPinEffect(marker);
            // Populate the yelp data into a json object
            // Upload the json data to the server
            JSONObject jsonMarker = new JSONObject();
            try {
                jsonMarker.put("user", ParseUser.getCurrentUser().getUsername());
                jsonMarker.put("latitude", lat);
                jsonMarker.put("longitude", lon);
                jsonMarker.put("color", markerColor);
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
                ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
                roomQuery.getInBackground(room.getObjectId(), new GetCallback<Room>() {
                    @Override
                    public void done(Room updatedRoom, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error getting room data from server", e);
                        }
                        room = updatedRoom;
                        JSONObject jsonMap = room.getMap();
                        try {
                            JSONArray markers = jsonMap.getJSONArray("markers");
                            markers.put(jsonMarker);
                            jsonMap.put("markers", markers);
                        } catch (JSONException jsonException) {
                            Log.e(TAG, "Couldn't edit json data", jsonException);
                        }
                        room.setMap(jsonMap);
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
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error loading marker to json object", e);
            }

        }
    }

    // Get the marker data from the Parse server and add it to the map
    public void populateMap() {
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.include("owner");
        roomQuery.getInBackground(room.getObjectId(), new GetCallback<Room>() {
            @Override
            public void done(Room updatedRoom, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Trouble getting room data from server to populate map", e);
                    Toasty.error(getContext(), "Couldn't populate map", Toasty.LENGTH_SHORT, true).show();
                }
                room = updatedRoom;
                owner = room.getParseUser("owner").getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
                JSONObject jsonMap = room.getMap();
                try {
                    JSONArray markers = jsonMap.getJSONArray("markers");
                    for (int i = 0; i < markers.length(); i++) {
                        JSONObject jsonMarker = markers.getJSONObject(i);
                        List<YelpBusinesses> businesses = new ArrayList<>();
                        double latitude = jsonMarker.getDouble("latitude");
                        double longitude = jsonMarker.getDouble("longitude");
                        String markerColor = jsonMarker.getString("color");
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
                        LatLng position = new LatLng(latitude, longitude);
                        MarkerTag tag = new MarkerTag(businesses, latitude, longitude, color);
                        // Define color of marker icon
                        BitmapDescriptor defaultMarker = MarkerTag.colorMarker(markerColor);
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(position)
                                .icon(defaultMarker));
                        marker.setTag(tag);
                        marker.setSnippet(user);
                        markerSet.add(position);
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        String username = currentUser.getUsername();
                        if (user.equals(username) || owner) {
                            marker.setDraggable(true);
                        } else {
                            marker.setDraggable(false);
                        }
                    }
                } catch (JSONException jsonException) {
                    Log.e(TAG,"Error getting markers from server", jsonException);
                }
            }
        });

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

    public void refreshMap() {
        if (map == null) {
            return;
        }
        Context context = getContext();
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.getInBackground(room.getObjectId(), new GetCallback<Room>() {
            @Override
            public void done(Room updatedRoom, ParseException e) {
                room = updatedRoom;
                try {
                    if (Constant.kicked(context, room, ParseUser.getCurrentUser().getObjectId())) {
                        return;
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
                if (e != null) {
                    Log.e(TAG, "Couldn't refresh map", e);
                    return;
                }
                JSONObject jsonMap = room.getMap();
                try {
                    JSONArray markers = jsonMap.getJSONArray("markers");
                    for (int i = 0; i < markers.length(); i++) {
                        JSONObject jsonMarker = markers.getJSONObject(i);
                        double latitude = jsonMarker.getDouble("latitude");
                        double longitude = jsonMarker.getDouble("longitude");
                        LatLng position = new LatLng(latitude, longitude);
                        if (markerSet.contains(position)) {
                            continue;
                        }
                        List<YelpBusinesses> businesses = new ArrayList<>();
                        String markerColor = jsonMarker.getString("color");
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
                        MarkerTag tag = new MarkerTag(businesses, latitude, longitude, color);
                        // Define color of marker icon
                        BitmapDescriptor defaultMarker = MarkerTag.colorMarker(markerColor);
                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(position)
                                .icon(defaultMarker));
                        marker.setTag(tag);
                        marker.setSnippet(user);
                        dropPinEffect(marker);
                        markerSet.add(position);
                        String username = ParseUser.getCurrentUser().getUsername();
                        marker.setDraggable(user.equals(username) || owner);
                    }
                } catch (JSONException jsonException) {
                    Log.e(TAG,"Error getting markers from server", jsonException);
                }

                if (getActivity() != null) {
                    JSONObject jsonUsers = room.getProfileImages();
                    ((RoomActivity) getActivity()).refreshUsers(jsonUsers);
                }

            }
        });
    }

    // Set the color from the radio button
    public void setColor() {
        int color_id = binding.colorGroup.getCheckedRadioButtonId();
        switch (color_id) {
            case R.id.color_red:
                color = "red";
                break;
            case R.id.color_orange:
                Log.i(TAG, "Orange");
                color = "orange";
                break;
            case R.id.color_yellow:
                color = "yellow";
                break;
            case R.id.color_green:
                color = "green";
                break;
            case R.id.color_blue:
                color = "blue";
                break;
            case R.id.color_purple:
                color = "violet";
                break;
            default:
                color = "green";
                break;

        }
    }

    public void pauseRefreshMap() {
        handler.removeCallbacksAndMessages(null);
    }

    public void resumeRefreshMap() {
        handler.postDelayed(refreshMapRunnable, POLL_INTERVAL);
    }
}