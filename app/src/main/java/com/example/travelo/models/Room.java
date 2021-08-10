package com.example.travelo.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;


@ParseClassName("Room")
public class Room extends ParseObject {
    public Room() {}

    public Room(String id) {
        super();
        put(KEY_ROOM_ID, id);
    }

    public static final String KEY_OWNER = "owner";
    public static final String KEY_MESSAGES = "messages";
    public static final String KEY_USERS = "users";
    public static final String KEY_ROOM_ID = "roomId";
    public static final String KEY_MAP = "map";
    public static final String KEY_PROFILE_IMAGES = "profileImages";
    public static final String KEY_JOINABLE = "joinable";
    public static final String KEY_KICKED = "kicked";
    public static final String KEY_INVITE_ONLY = "inviteOnly";

    public ParseUser getOwner() {
        return getParseUser(KEY_OWNER);
    }

    public void setOwner(ParseUser user) {
        put(KEY_OWNER, user);
    }
    public JSONArray getMessages() {
        return getJSONArray(KEY_MESSAGES);
    }

    public void setMessages(JSONArray messages) {
        put(KEY_MESSAGES, messages);
    }

    public JSONObject getUsers() {
        return getJSONObject(KEY_USERS);
    }

    public void setUsers(JSONObject users) {
        put(KEY_USERS, users);
    }

    public String getRoomId() {
        return getString(KEY_ROOM_ID);
    }

    public void setMap(JSONObject map) {
        put(KEY_MAP, map);
    }

    public JSONObject getMap() {
        return getJSONObject(KEY_MAP);
    }

    public void setProfileImages(JSONObject profileImages) {
        put(KEY_PROFILE_IMAGES, profileImages);
    }
    public JSONObject getProfileImages() {
        return getJSONObject(KEY_PROFILE_IMAGES);
    }

    public void setJoinable(boolean joinable) {
        put(KEY_JOINABLE, joinable);
    }

    public boolean getJoinable() {
        return getBoolean(KEY_JOINABLE);
    }

    public void setKicked(JSONArray kicked) {
        put(KEY_KICKED, kicked);
    }

    public JSONArray getKicked() {
        return getJSONArray(KEY_KICKED);
    }

    public void setInviteOnly(boolean inviteOnly) {
        put(KEY_INVITE_ONLY, inviteOnly);
    }

    public boolean getIntiveOnly() {
        return getBoolean(KEY_INVITE_ONLY);
    }
}
