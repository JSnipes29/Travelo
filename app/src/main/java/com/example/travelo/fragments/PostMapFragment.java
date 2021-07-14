package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.adapters.CustomWindowAdapter;
import com.example.travelo.databinding.FragmentPostMapBinding;
import com.example.travelo.models.Room;
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
import java.util.List;


public class PostMapFragment extends Fragment {


    public static final String TAG = "PostMapFragment";
    FragmentPostMapBinding binding;
    MapView mapView;
    GoogleMap map;
    Room room;

    public PostMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostMapBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        if (mapView != null) {
            mapView.getMapAsync(new OnMapReadyCallback() {
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
        return view;
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
            Toast.makeText(getContext(), "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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