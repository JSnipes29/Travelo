package com.example.travelo.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

@ParseClassName("Messages")
public class Messages extends ParseObject {
    public Messages () {}

    public static final String KEY = "Messages";
    public static final String KEY_MESSAGES = "messages";

    public void setMessages(JSONArray jsonMessages) {
        put(KEY_MESSAGES, jsonMessages);
    }

    public JSONArray getMessages() {
        return getJSONArray(KEY_MESSAGES);
    }
}
