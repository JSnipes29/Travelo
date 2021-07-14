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


    public static YelpBusinesses makeBusiness(String name, double ratings, int numRatings, String imageUrl) {
        YelpBusinesses business = new YelpBusinesses();
        business.setName(name);
        business.setRating(ratings);
        business.setReviewCount(numRatings);
        business.setImageUrl(imageUrl);
        return business;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
}
