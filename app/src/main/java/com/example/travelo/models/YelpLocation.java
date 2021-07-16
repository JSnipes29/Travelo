package com.example.travelo.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class YelpLocation {
    @SerializedName("address1")
    String address;
    @SerializedName("city")
    String city;
    @SerializedName("state")
    String state;
    @SerializedName("country")
    String country;

    public static YelpLocation makeLocation(String address, String city, String state, String country) {
        YelpLocation yelpLocation = new YelpLocation();
        yelpLocation.setAddress(address);
        yelpLocation.setCity(city);
        yelpLocation.setState(state);
        yelpLocation.setCountry(country);
        return yelpLocation;
    }

    public static String formatAddress(String city, String state, String country) {
        return city + ", " + state + ", " + country;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
