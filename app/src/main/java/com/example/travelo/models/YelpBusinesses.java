package com.example.travelo.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

    @SerializedName("price")
    String price;

    @SerializedName("categories")
    List<YelpCategory> categories;

    @SerializedName("location")
    YelpLocation location;

    @SerializedName("distance")
    double distanceMeters;

    private boolean added;

    private boolean button;


    public static YelpBusinesses makeBusiness(String name, double ratings, int numRatings,
                                              String imageUrl, String price,
                                              double distanceMeters, YelpLocation location,
                                              String category) {
        YelpBusinesses business = new YelpBusinesses();
        business.setName(name);
        business.setRating(ratings);
        business.setReviewCount(numRatings);
        business.setImageUrl(imageUrl);
        String p = price;
        business.setPrice(p);
        business.setDistanceMeters(distanceMeters);
        business.setLocation(location);
        List<YelpCategory> list = new ArrayList<>();
        list.add(YelpCategory.makeCategory(category));
        business.setCategories(list);
        return business;
    }

    public static double metersToMiles(double meters) {
        double factor = .00062137119;
        double miles = meters * factor;
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(miles));
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

    public String getPrice() {
        if (price == null) {
            return "";
        }
        return price;
    }

    public List<YelpCategory> getCategories() {
        return categories;
    }

    public YelpLocation getLocation() {
        return location;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setCategories(List<YelpCategory> categories) {
        this.categories = categories;
    }

    public void setLocation(YelpLocation location) {
        this.location = location;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
}
