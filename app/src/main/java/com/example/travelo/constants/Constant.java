package com.example.travelo.constants;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

public class Constant {
    public static void invite(Context context, Room room, String userId, final String TAG, DialogFragment fragment) {
        ParseQuery<ParseUser> userParseQuery = ParseQuery.getQuery(ParseUser.class);
        userParseQuery.include(Inbox.KEY);
        userParseQuery.getInBackground(userId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Problem loading user data from server", e);
                    return;
                }
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                String roomObjectId = room.getObjectId();
                String roomId = room.getRoomId();
                int index = Inbox.indexOfRoomMessage(jsonInbox, roomObjectId);
                // If the user already has an invite, return
                if (index != -1) {
                    Toasty.info(context, "Invite already sent", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                JSONObject roomMessage = new JSONObject();
                try {
                    roomMessage.put(roomObjectId, roomId);
                    roomMessage.put("id", InboxAdapter.ROOM_ID);
                } catch (JSONException jsonException) {
                    Log.e(TAG, "Couldn't edit json data", jsonException);
                }
                jsonInbox.put(roomMessage);
                inbox.setMessages(jsonInbox);
                inbox.saveInBackground(exception -> {
                    if (exception != null) {
                        Log.e(TAG, "Couldn't save room message in inbox", exception);
                    } else {
                        Log.i(TAG, "Room message saved in inbox");
                        if (context != null) {
                            Toasty.success(context, "Invite Sent", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
            }
        });
        if (fragment != null) {
            fragment.dismiss();
        }
    }
}
