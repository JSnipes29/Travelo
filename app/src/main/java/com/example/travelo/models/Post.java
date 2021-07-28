package com.example.travelo.models;

import android.text.format.DateUtils;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_OWNER = "owner";
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_USERS = "users";
    public static final String KEY_MAP = "map";
    public static final String KEY_DESCRIPTION = "description";
    public static final String DEFAULT_DESCRIPTION = "Map";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_USER_ARRAY = "userArray";
    public static final String KEY_LIKES_ARRAY = "likesArray";


    public Post() {}

    public static Post createPost(JSONObject map, String description, JSONObject users, File photo) {
        Post post = new Post();
        post.setMap(map);
        post.setOwner(ParseUser.getCurrentUser());
        if (description.isEmpty()) {
            String d = ParseUser.getCurrentUser().getUsername() + "'s " + DEFAULT_DESCRIPTION;
            post.setDescription(d);
        } else {
            post.setDescription(description);
        }
        post.setUsers(users);
        JSONArray usersArray = new JSONArray();
        Iterator<String> iter = users.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            usersArray.put(key);
        }
        post.setUserArray(usersArray);
        if (photo != null) {
            ParseFile parseFilePhoto = new ParseFile(photo);
            post.setPhoto(parseFilePhoto);
        }
        return post;
    }


    public static Post createPost(JSONObject map, String description, JSONObject users, ParseFile photo) {
        Post post = new Post();
        post.setMap(map);
        post.setOwner(ParseUser.getCurrentUser());
        if (description.isEmpty()) {
            String d = ParseUser.getCurrentUser().getUsername() + "'s " + DEFAULT_DESCRIPTION;
            post.setDescription(d);
        } else {
            post.setDescription(description);
        }
        post.setUsers(users);
        JSONArray usersArray = new JSONArray();
        Iterator<String> iter = users.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            usersArray.put(key);
        }
        post.setUserArray(usersArray);
        if (photo != null) {
            post.setPhoto(photo);
        }
        return post;
    }

    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public ParseUser getOwner() {
        return getParseUser(KEY_OWNER);
    }

    public void setOwner(ParseUser user) {
        put(KEY_OWNER, user);
    }

    public JSONArray getComments() {
        return getJSONArray(KEY_COMMENTS);
    }

    public void setComments(JSONArray messages) {
        put(KEY_COMMENTS, messages);
    }

    public JSONObject getUsers() {
        return getJSONObject(KEY_USERS);
    }

    public void setUsers(JSONObject users) {
        put(KEY_USERS, users);
    }

    public void setMap(JSONObject map) {
        put(KEY_MAP, map);
    }

    public JSONObject getMap() {
        return getJSONObject(KEY_MAP);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setPhoto(ParseFile photo) {
        put(KEY_PHOTO, photo);
    }

    public ParseFile getPhoto() {
        return getParseFile(KEY_PHOTO);
    }

    public void setUserArray(JSONArray array) {
        put(KEY_USER_ARRAY, array);
    }

    public void setLikesArray(JSONArray array) {
        put(KEY_LIKES_ARRAY, array);
    }

    public JSONArray getLikesArray() {
        return getJSONArray(KEY_LIKES_ARRAY);
    }


}
