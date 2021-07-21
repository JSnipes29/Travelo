package com.example.travelo.models;

import android.util.Log;

import com.example.travelo.adapters.InboxAdapter;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
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

    public static int indexOfRoomMessage(JSONArray array, String roomObjectId) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject message = array.getJSONObject(i);
                // If the message isn't a dm (a room message), continue
                if (message.length() != InboxAdapter.ROOM_LENGTH) {
                    continue;
                }
                // If the message contains the user id return the index
                String jsonUserId = message.keys().next();
                if (jsonUserId.equals(roomObjectId)) {
                    return i;
                }
            } catch (JSONException e) {
                Log.e("Inbox", "Error reading json data", e);
            }
        }
        return -1;
    }

}
