package com.example.travelo.constants;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelo.activities.MainActivity;
import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.adapters.NameAdapter;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Post;
import com.example.travelo.models.Room;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class Constant {
    public static final String TAG = "Constants";
    public static final String CLICK_BIO = "Click to edit bio";
    public static final int MAX_BIO_LENGTH = 144;
    public static final int CREATE_MAP_SHORTCUT = 1;
    public static final int JOIN_MAP_SHORTCUT = 2;
    public static final float MAP_ZOOM = 12f;
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
                    roomMessage.put("roomObjectId", roomObjectId);
                    roomMessage.put("roomId", roomId);
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
    public static void setupLikeButton(Button btnLike, String postId, TextView tvLikeCount) {
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
                    int likesCount = likesArray.length();
                    tvLikeCount.setText(String.valueOf(likesCount));
                    if (jsonStringArrayContains(likesArray, userId)) {
                        btnLike.setSelected(true);
                        btnLike.setOnClickListener(v -> unlike(btnLike, postId, userId, tvLikeCount));
                    } else {
                        btnLike.setSelected(false);
                        btnLike.setOnClickListener(v -> like(btnLike, postId, userId, tvLikeCount));
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }
        });
    }

    public static void unlike(Button btnLike, String postId, String userId, TextView tvLikeCount) {
        btnLike.setSelected(false);
        setLikes(0, btnLike, postId, userId, tvLikeCount);
    }

    public static void like(Button btnLike, String postId, String userId, TextView tvLikeCount) {
        btnLike.setSelected(true);
        setLikes(1, btnLike, postId, userId, tvLikeCount);
    }

    // 0 == unlike, 1 == like
    public static void setLikes(int parameter, Button btnLike, String postId, String userId, TextView tvLikeCount) {
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
                                btnLike.setOnClickListener(v -> like(btnLike, postId, userId, tvLikeCount));
                            } else if (parameter == 1) {
                                likesArray.put(userId);
                                liked.put(postId);
                                btnLike.setOnClickListener(v -> unlike(btnLike, postId, userId, tvLikeCount));
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        int likesCount = likesArray.length();
                        tvLikeCount.setText(String.valueOf(likesCount));
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

    public static boolean jsonStringObjectContains(JSONObject object, String string) {
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (key.equals(string)) {
                return true;
            }
        }
        return false;
    }

    public static boolean kicked(AppCompatActivity context, Room room, String userId) throws JSONException {
        if (room == null) {
            String message = "Room is no longer available";
            Toasty.error(context, message, Toast.LENGTH_SHORT).show();
            context.finish();
            return true;
        }
        JSONArray kicked = room.getKicked();
        if (jsonStringArrayContains(kicked, userId)) {
            String message = "You have been kicked";
            Toasty.error(context, message, Toast.LENGTH_SHORT).show();
            context.finish();
            return true;
        }
        return false;
    }

    public static void kick(Context context, ParseUser user, String userId, String username, String roomId, int type) {
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.getInBackground(roomId, new GetCallback<Room>() {
            @Override
            public void done(Room room, ParseException e) {
                JSONArray kicked = room.getKicked();
                kicked.put(userId);
                room.setKicked(kicked);
                JSONObject users = room.getUsers();
                users.remove(username);
                room.setUsers(users);
                if (type == 0) {
                    JSONObject profileImages = room.getProfileImages();
                    profileImages.remove(username);
                    room.setProfileImages(profileImages);
                }
                room.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toasty.error(context, "Error getting user data", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        Toasty.success(context, "Removed user from room", Toast.LENGTH_SHORT, true).show();
                    }
                });
            }
        });
        // Remove message from inbox
        Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
        JSONArray jsonInbox = inbox.getMessages();
        int index = Inbox.indexOfRoomMessage(jsonInbox, roomId);
        if (index == -1) {
            return;
        }
        jsonInbox.remove(index);
        inbox.setMessages(jsonInbox);
        inbox.saveInBackground();
    }

    public static ItemTouchHelper setupKickSwipe(Context context, List<String> users, NameAdapter adapter, String roomId, int type) {
        // Configure swipe to remove
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.i(TAG, "On move");
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                String name = users.get(position);
                ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                userQuery.whereEqualTo("username", name);
                userQuery.include(Inbox.KEY);
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> userList, ParseException e) {
                        if (e != null || userList.isEmpty()) {
                            Toasty.error(context, "Error getting user data", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        ParseUser user = userList.get(0);
                        String username = user.getUsername();
                        String userId = user.getObjectId();
                        users.remove(position);
                        adapter.notifyItemRemoved(position);
                        if (userId.equals(ParseUser.getCurrentUser().getObjectId())) {
                            return;
                        }
                        kick(context, user, userId, username, roomId, type);
                    }
                });




            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        return itemTouchHelper;
    }

    public static void removeMessage(String userId, String messageId, int messageType) {
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.include(Inbox.KEY);
        userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Problem loading user data from server", e);
                    return;
                }
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                int index;
                switch (messageType) {
                    case InboxAdapter.ROOM_ID:
                        index = Inbox.indexOfRoomMessage(jsonInbox, messageId);
                        break;
                    case InboxAdapter.FR_ID:
                        index = Inbox.indexOfFriendRequest(jsonInbox, messageId);
                        break;
                    case InboxAdapter.FR_SENT_ID:
                        index = Inbox.indexOfFriendRequestSent(jsonInbox, messageId);
                        break;
                    case InboxAdapter.DM_ID:
                        index = -1;
                        break;
                    default:
                        index = -1;
                        break;
                }
                if (index == -1) {
                    return;
                }
                jsonInbox.remove(index);
                inbox.setMessages(jsonInbox);
                inbox.saveInBackground(exception -> {
                    if (exception != null) {
                        Log.e(TAG, "Couldn't remove message from inbox", exception);
                    } else {
                        Log.i(TAG, "Message removed from inbox");
                    }
                });
            }
        });
    }

    public static void deleteRoom(Context context, String roomId, String userId) {
        ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
        roomQuery.getInBackground(roomId, new GetCallback<Room>() {
            @Override
            public void done(Room room, ParseException e) {
                if (e != null) {
                    Log.e(context.getClass().getSimpleName(), "Error getting room data", e);
                    return;
                }
                room.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toasty.error(context, "Error deleting room", Toast.LENGTH_SHORT, true).show();
                        } else {
                            Toasty.success(context, "Room successfully deleted", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
                removeMessage(userId, roomId, InboxAdapter.ROOM_ID);
            }
        });
    }

    public static int indexOfMessage(JSONObject message, JSONArray inbox) throws JSONException {
        int id = message.getInt("id");
        int index = -1;
        switch (id) {
            case InboxAdapter.ROOM_ID:
                String roomId = message.getString("roomObjectId");
                index = Inbox.indexOfRoomMessage(inbox, roomId);
                break;
            case InboxAdapter.FR_ID:
                String frMessageId = message.getString("userId");
                index = Inbox.indexOfFriendRequest(inbox, frMessageId);
                break;
            case InboxAdapter.FR_SENT_ID:
                String frsMssageId = message.getString("userId");
                index = Inbox.indexOfFriendRequestSent(inbox, frsMssageId);
                break;
            case InboxAdapter.DM_ID:
                String messagesId = message.getString("messages");
                index = Inbox.indexOfDM(inbox, messagesId);
                break;
            default:
                index = -1;
                break;
        }
        return index;
    }

    public static void centerMap(Context context, GoogleMap map, List<LatLng> markerLocations) {
        int numMarkers = markerLocations.size();
        CameraUpdate cameraUpdate;
        switch (numMarkers) {
            case 0:
                return;
            case 1:
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(markerLocations.get(0), MAP_ZOOM);
                map.moveCamera(cameraUpdate);
                break;
            default:
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng location: markerLocations) {
                    builder.include(location);
                }
                LatLngBounds bounds = builder.build();
                int width = context.getResources().getDisplayMetrics().widthPixels;
                int height = context.getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (height * 0.20);
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                map.moveCamera(cameraUpdate);
                break;
        }

    }


    public static int dpsToPixels(Context context, int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }
}
