package com.example.travelo.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class YelpCategory {
    @SerializedName("title")
    String title;

    public static YelpCategory makeCategory(String category) {
        YelpCategory yelpCategory = new YelpCategory();
        yelpCategory.setTitle(category);
        return yelpCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
