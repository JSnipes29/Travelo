package com.example.travelo.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONObject;

@ParseClassName("Inbox")
public class Inbox extends ParseObject {

    public Inbox() {}
    public static final String KEY_MESSAGES = "messages";
    public static final String KEY_ROOMS = "rooms";
    public static final String KEY = "inbox";

    public void setMessages(JSONArray jsonObject) {
        put(KEY_MESSAGES, jsonObject);
    }

    public JSONArray getMessages() {
        return getJSONArray(KEY_MESSAGES);
    }

    public void setRooms(JSONArray jsonArray) {
        put(KEY_ROOMS, jsonArray);
    }

    public JSONArray getRooms() {
        return getJSONArray(KEY_ROOMS);
    }

}
