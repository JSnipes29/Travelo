package com.example.travelo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelo.R;
import com.example.travelo.models.YelpBusinesses;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
    LayoutInflater mInflater;
    List<YelpBusinesses> businesses;
    YelpAdapter adapter;
    Context context;

    public CustomWindowAdapter(LayoutInflater i, Context c){
        mInflater = i;
        context = c;
    }

    // This defines the contents within the info window based on the marker
    @Override
    public View getInfoContents(Marker marker) {
        // Getting view from the layout file
        View v = mInflater.inflate(R.layout.custom_info_window, null);
        // Populate fields
        RecyclerView window = (RecyclerView) v.findViewById(R.id.rvInfoWindow);
        businesses = (List<YelpBusinesses>) marker.getTag();
        adapter = new YelpAdapter(context, businesses);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        window.setAdapter(adapter);
        window.setLayoutManager(linearLayoutManager);
        // Return info window contents
        return v;
    }

    // This changes the frame of the info window; returning null uses the default frame.
    // This is just the border and arrow surrounding the contents specified above
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
}
