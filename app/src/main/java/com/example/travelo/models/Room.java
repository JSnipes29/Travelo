package com.example.travelo.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;


@ParseClassName("Room")
public class Room extends ParseObject {
    public Room() {}

    public Room(String id) {
        super();
        put("roomId", id);
    }

    public static final String KEY_OWNER = "owner";
    public static final String KEY_MESSAGES = "messages";
    public static final String KEY_USERS = "users";
    public static final String KEY_ROOM_ID = "roomId";

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

    public JSONArray getUsers() {
        return getJSONArray(KEY_USERS);
    }

    public void setUsers(JSONArray messages) {
        put(KEY_USERS, messages);
    }

    public String getRoomId() {
        return getString(KEY_ROOM_ID);
    }

}
