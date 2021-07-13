package com.example.travelo.models;

import com.google.gson.annotations.SerializedName;

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
