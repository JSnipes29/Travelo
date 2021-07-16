package com.example.travelo.models;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class MarkerTag {
    public List<YelpBusinesses> locations;
    public double latitude;
    public double longitude;


    // Empty constructor for parcelable
    public MarkerTag() {}

    public MarkerTag(List<YelpBusinesses> locations, double latitude, double longitude) {
        super();
        this.locations = locations;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public List<YelpBusinesses> getLocations() {
        return locations;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
