package com.example.travelo.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YelpSearchResult {
    @SerializedName("total")
    int total;
    @SerializedName("businesses")
    List<YelpBusinesses> businesses;

    public List<YelpBusinesses> getBusinesses() {
        return businesses;
    }

}
