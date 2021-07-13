package com.example.travelo;


import com.example.travelo.models.YelpSearchResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface YelpService {
    // Request method and URL specified in the annotation

    @GET("businesses/search")
    Call<YelpSearchResult> searchLocations(@Header("Authorization") String authHeader, @Query("latitude") double lat, @Query("longitude") double lon);
}
