package com.example.travelo.models;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class MarkerTag {
    public List<YelpBusinesses> locations;
    public double latitude;
    public double longitude;
    String color;


    // Empty constructor for parcelable
    public MarkerTag() {}

    public MarkerTag(List<YelpBusinesses> locations, double latitude, double longitude) {
        super();
        this.locations = locations;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MarkerTag(List<YelpBusinesses> locations, double latitude, double longitude, String color) {
        super();
        this.locations = locations;
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
    }

    public static BitmapDescriptor colorMarker(String color) {
        BitmapDescriptor marker =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
        switch (color) {
            case "blue":
                marker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                break;
            case "red":
                marker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                break;
            case "orange":
                marker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                break;
            case "yellow":
                marker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                break;
            case "violet":
                marker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                break;
            default:
                marker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                break;

        }
        return marker;
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

    public String getColor() {
        return color;
    }
}
