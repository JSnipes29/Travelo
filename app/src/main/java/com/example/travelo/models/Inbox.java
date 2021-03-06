package com.example.travelo.models;

import android.util.Log;

import com.example.travelo.adapters.InboxAdapter;
import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

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

    // Find index of a room message
    public static int indexOfRoomMessage(JSONArray array, String roomObjectId) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject message = array.getJSONObject(i);
                // If the message isn't a dm (a room message), continue
                if (message.getInt("id") != InboxAdapter.ROOM_ID) {
                    continue;
                }
                // If the message room object id equals to roomObjectId return the index
                String jsonRoomId = message.getString("roomObjectId");
                if (jsonRoomId.equals(roomObjectId)) {
                    return i;
                }
            } catch (JSONException e) {
                Log.e("Inbox", "Error reading json data", e);
            }
        }
        return -1;
    }

    // Find index of friend request
    public static int indexOfFriendRequest(JSONArray array, String userId) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject message = array.getJSONObject(i);
                // If the message isn't a dm (a room message), continue
                if (message.getInt("id") != InboxAdapter.FR_ID) {
                    continue;
                }
                // If the message contains the user id return the index
                String jsonUserId = message.getString("userId");
                if (jsonUserId.equals(userId)) {
                    return i;
                }
            } catch (JSONException e) {
                Log.e("Inbox", "Error reading json data", e);
            }
        }
        return -1;
    }

    // Find index of friend request sent message
    public static int indexOfFriendRequestSent(JSONArray array, String userId) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject message = array.getJSONObject(i);
                // If the message isn't a dm (a room message), continue
                if (message.getInt("id") != InboxAdapter.FR_SENT_ID) {
                    continue;
                }
                // If the message contains the user id return the index
                String jsonUserId = message.getString("userId");
                if (jsonUserId.equals(userId)) {
                    return i;
                }
            } catch (JSONException e) {
                Log.e("Inbox", "Error reading json data", e);
            }
        }
        return -1;
    }

    // Find index of dm
    public static int indexOfDM(JSONArray array, String messagesId) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject message = array.getJSONObject(i);
                // If the message isn't a dm continue
                if (message.getInt("id") != InboxAdapter.DM_ID) {
                    continue;
                }
                // If the message contains the user id return the index
                String jsonMessagesId = message.getString("messages");
                if (jsonMessagesId.equals(messagesId)) {
                    return i;
                }
            } catch (JSONException e) {
                Log.e("Inbox", "Error reading json data", e);
            }
        }
        return -1;
    }

}
