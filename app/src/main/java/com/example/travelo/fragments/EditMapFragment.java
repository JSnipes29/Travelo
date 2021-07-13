package com.example.travelo.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.YelpLocationsActivity;
import com.example.travelo.databinding.FragmentEditMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class EditMapFragment extends Fragment implements GoogleMap.OnMapLongClickListener{

    public static final String TAG = "EditMapFragment";
    public static final int YELP_CODE = 200;
    FragmentEditMapBinding binding;
    MapView mapFragment;
    GoogleMap map;

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
        mapFragment = (MapView) view.findViewById(R.id.map);
        mapFragment.onCreate(savedInstanceState);
        mapFragment.onResume();
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
            });
        } else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
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
    }
}