package com.example.travelo.constants;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Post;
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
    public static final String TAG = "Constants";
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

    public static boolean jsonStringArrayContains(JSONArray array, String string) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            String element = array.getString(i);
            if (element.equals(string)) {
                return true;
            }
        }
        return false;
    }

    // Setup action of a like button
    public static void setupLikeButton(Button btnLike, String postId) {
        final String userId = ParseUser.getCurrentUser().getObjectId();
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.getInBackground(postId, new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting data from parse", e);
                    return;
                }
                JSONArray likesArray = post.getLikesArray();
                try {
                    if (jsonStringArrayContains(likesArray, userId)) {
                        btnLike.setSelected(true);
                        btnLike.setOnClickListener(v -> unlike(btnLike, postId, userId));
                    } else {
                        btnLike.setSelected(false);
                        btnLike.setOnClickListener(v -> like(btnLike, postId, userId));
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        });
    }

    public static void unlike(Button btnLike, String postId, String userId) {
        btnLike.setSelected(false);
        setLikes(0, btnLike, postId, userId);
    }

    public static void like(Button btnLike, String postId, String userId) {
        btnLike.setSelected(true);
        setLikes(1, btnLike, postId, userId);
    }

    // 0 == unlike, 1 == like
    public static void setLikes(int parameter, Button btnLike, String postId, String userId) {
        btnLike.setClickable(false);
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.getInBackground(postId, new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting data from parse", e);
                    btnLike.setClickable(true);
                    return;
                }
                ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser currentUser, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error getting data from parse", e);
                            btnLike.setClickable(true);
                            return;
                        }
                        JSONArray likesArray = post.getLikesArray();
                        JSONArray liked = currentUser.getJSONArray("liked");
                        try {
                            if (parameter == 0) {
                                jsonStringArrayRemove(likesArray, userId);
                                jsonStringArrayRemove(liked, postId);
                            } else if (parameter == 1) {
                                likesArray.put(userId);
                                liked.put(postId);
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        post.setLikesArray(likesArray);
                        currentUser.put("liked", liked);
                        post.saveInBackground();
                        currentUser.saveInBackground();
                        btnLike.setClickable(true);
                    }
                });

            }
        });
    }

    public static void jsonStringArrayRemove(JSONArray array, String string) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            String element = array.getString(i);
            if (element.equals(string)) {
                array.remove(i);
                return;
            }
        }
    }
}
