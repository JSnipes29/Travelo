package com.example.travelo.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONObject;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_OWNER = "owner";
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_USERS = "users";
    public static final String KEY_MAP = "map";
    public static final String KEY_DESCRIPTION = "description";
    public static final String DEFAULT_DESCRIPTION = "Map";


    public Post() {}

    public static Post createPost(JSONObject map, String description, JSONObject users) {
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
        return post;
    }

    public ParseUser getOwner() {
        return getParseUser(KEY_OWNER);
    }

    public void setOwner(ParseUser user) {
        put(KEY_OWNER, user);
    }

    public JSONObject getComments() {
        return getJSONObject(KEY_COMMENTS);
    }

    public void setComments(JSONObject messages) {
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


}