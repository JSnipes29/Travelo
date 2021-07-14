package com.example.travelo.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class YelpBusinesses {
    @SerializedName("name")
    String name;

    @SerializedName("image_url")
    String imageUrl;

    @SerializedName("rating")
    double rating;

    @SerializedName("review_count")
    int reviewCount;

    @SerializedName("url")
    String url;

    private boolean added;

    private boolean button;

    public static void setAddedAll(List<YelpBusinesses> b) {
        for (int i = 0; i < b.size(); i++) {
            b.get(i).setAdded(false);
        }
    }

    public static void setButtonAll(List<YelpBusinesses> b, boolean value) {
        for (int i = 0; i < b.size(); i++) {
            b.get(i).setButton(value);
        }
    }

    public void setButton(boolean button) {
        this.button = button;
    }

    public boolean getButton() {
        return button;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public boolean getAdded() {
        return added;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public String getUrl() {
        return url;
    }
}
