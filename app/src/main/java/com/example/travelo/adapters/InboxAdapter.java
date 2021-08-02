package com.example.travelo.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.activities.MessagesActivity;
import com.example.travelo.R;
import com.example.travelo.activities.RoomActivity;
import com.example.travelo.constants.Constant;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Messages;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.MessageViewHolder> {
    private List<JSONObject> messages;
    private Context context;

    public static final String TAG = "InboxAdapter";
    private static final int MESSAGE_DM = 629;
    private static final int MESSAGE_ROOM = 1229;
    public static final int MESSAGE_FR = 2001;
    public static final int MESSAGE_FR_SENT = 2002;
    public static final int DM_ID = 4;
    public static final int FR_ID = 2;
    public static final int ROOM_ID = 1;
    public static final int FR_SENT_ID = 3;

    public InboxAdapter(List<JSONObject> list, Context c) {
        messages = list;
        context = c;
    }

    @Override
    public int getItemViewType(int position) {
        int type = typeOfMessage(position);
        switch (type) {
            case DM_ID:
                return MESSAGE_DM;
            case ROOM_ID:
                return MESSAGE_ROOM;
            case FR_ID:
                return MESSAGE_FR;
            case FR_SENT_ID:
                return MESSAGE_FR_SENT;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // If incoming message, inflate income message layout
        // else inflate outgoing message layout
        if (viewType == MESSAGE_ROOM) {
            View contactView = inflater.inflate(R.layout.item_room_message, parent, false);
            return new RoomMessageViewHolder(contactView);
        } else if (viewType == MESSAGE_DM) {
            View contactView = inflater.inflate(R.layout.item_dm_message, parent, false);
            return new DMViewHolder(contactView);
        } else if (viewType == MESSAGE_FR) {
            View contactView = inflater.inflate(R.layout.item_friend_request, parent, false);
            return new FRViewHolder(contactView);
        } else if (viewType == MESSAGE_FR_SENT) {
            View contactView = inflater.inflate(R.layout.item_friend_request_sent, parent, false);
            return new FRSentViewHolder(contactView);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        JSONObject obj = messages.get(position);
        holder.bindMessage(obj, position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private int typeOfMessage(int position) {
        JSONObject message = messages.get(position);
        int id = -1;
        try {
            id = message.getInt("id");
        } catch (JSONException e) {
            Log.e(TAG, "Error reading inbox message id", e);
        }
        return id;

    }

    public abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(JSONObject message, int position);
    }

    public class RoomMessageViewHolder extends MessageViewHolder {
        RecyclerView rvUsers;
        TextView tvRoomId;
        Button btnJoin;
        Button btnDelete;

        public RoomMessageViewHolder(View itemView) {
            super(itemView);
            tvRoomId = (TextView) itemView.findViewById(R.id.tvRoomId);
            rvUsers = (RecyclerView) itemView.findViewById(R.id.rvUsers);
            btnJoin = (Button) itemView.findViewById(R.id.btnJoin);
            btnDelete = (Button) itemView.findViewById(R.id.btnDelete);
        }

        @Override
        public void bindMessage(JSONObject message, int position) {
            Iterator<String> iter = message.keys();
            String key = iter.next();
            String roomObjectId;
            if (key.equals("id")) {
                roomObjectId = iter.next();
            } else {
                roomObjectId = key;
            }
            Log.i(TAG, "Id: " + roomObjectId);
            ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
            query.include(Room.KEY_OWNER);
            query.getInBackground(roomObjectId, new GetCallback<Room>() {
                @Override
                public void done(Room room, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Couldn't bind room message", e);
                        messages.remove(position);
                        if (room == null) {
                            String userId = ParseUser.getCurrentUser().getObjectId();
                            Constant.removeMessage(userId, roomObjectId, InboxAdapter.ROOM_ID);
                        }
                        return;
                    }

                    String roomId = room.getRoomId();
                    tvRoomId.setText(roomId);
                    // Bind the users who were in the room
                    List<String[]> users = new ArrayList<>();
                    JSONObject jsonUsers = room.getProfileImages();
                    Iterator<String> iter = jsonUsers.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            String imageUrl = jsonUsers.getString(key);
                            String[] userArray = {key, imageUrl};
                            users.add(userArray);
                        } catch (JSONException error) {
                            Log.e(TAG, "Error getting users", error);
                        }
                    }
                    UsersAdapter userAdapter = new UsersAdapter(context, users);
                    rvUsers.setAdapter(userAdapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    rvUsers.setLayoutManager(linearLayoutManager);
                    btnJoin.setOnClickListener(v -> goToRoom(roomObjectId));
                    // Setup delete room button
                    String roomOwnerId = room.getOwner().getObjectId();
                    String userId = ParseUser.getCurrentUser().getObjectId();
                    if (roomOwnerId.equals(userId)) {
                        btnDelete.setVisibility(View.VISIBLE);
                        btnDelete.setOnClickListener(v -> deleteRoom(roomObjectId, userId, position));
                    }
                }
            });
        }

        public void goToRoom(String roomObjectId) {
            Log.i(TAG, "Going to room");
            // Specify which class to query
            ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
            query.include("owner");
            // Find the room asynchronously
            query.getInBackground(roomObjectId, new GetCallback<Room>() {
                @Override
                public void done(Room room, ParseException error) {
                    if (error != null) {
                        Log.e(TAG, "Error joining room", error);
                        return;
                    }

                    if (room == null) {
                        Toasty.error(context, "This room is no longer available", Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    if (!room.getJoinable()) {
                        if (!room.getOwner().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            Toasty.error(context, "This is room is no longer joinable", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                    }
                    JSONArray kicked = room.getKicked();
                    try {
                        if (Constant.jsonStringArrayContains(kicked, ParseUser.getCurrentUser().getObjectId())) {
                            Toasty.error(context, "You have been kicked from this room", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                    Intent intent = new Intent(context, RoomActivity.class);
                    intent.putExtra("room", roomObjectId);
                    String ownerId = room.getOwner().getObjectId();
                    intent.putExtra("ownerId", ownerId);
                    context.startActivity(intent);
                }

            });
        }

        public void deleteRoom(String roomId, String userId, int position) {
            messages.remove(position);
            notifyItemRemoved(position);
            Constant.deleteRoom(context, roomId, userId);
        }
    }
        public class DMViewHolder extends MessageViewHolder {
            ImageView ivProfileImage;
            TextView tvName;
            TextView tvBody;
            RelativeLayout rlDmItem;

            public DMViewHolder(View itemView) {
                super(itemView);
                ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
                tvBody = (TextView) itemView.findViewById(R.id.tvBody);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                rlDmItem = (RelativeLayout) itemView.findViewById(R.id.rlDmItem);
            }

            @Override
            public void bindMessage(JSONObject dm, int position) {
                String profileUrl = null;
                String name = null;
                String messagesId = null;
                try {
                    profileUrl = dm.getString("profileImage");
                    name = dm.getString("username");
                    messagesId = dm.getString("messages");

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Couldn't get json data", e);
                }
                ParseQuery<Messages> query = ParseQuery.getQuery(Messages.class);
                query.getInBackground(messagesId, new GetCallback<Messages>() {
                    @Override
                    public void done(Messages messages, ParseException e) {
                        JSONArray jsonMessages = messages.getMessages();
                        JSONObject message = null;
                        String text = null;
                        try {
                            message = jsonMessages.getJSONObject(jsonMessages.length() - 1);
                            text = message.getString("username") + ": " + message.getString("body");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                            Log.e(TAG, "Trouble updating json data with dm", jsonException);
                        }

                        tvBody.setText(text);
                    }
                });
                Glide.with(context)
                        .load(profileUrl)
                        .circleCrop() // create an effect of a round profile picture
                        .into(ivProfileImage);
                tvName.setText(name);
                final String mId = messagesId;
                rlDmItem.setOnClickListener(v -> goToMessages(mId));
            }


            // Go to the messages view so user can type new messages
            public void goToMessages(String messagesId) {
                Log.i(TAG, "Clicked on dm");
                Intent intent = new Intent(context, MessagesActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("messagesId", messagesId);
                context.startActivity(intent);
            }
        }

        public class FRViewHolder extends MessageViewHolder {
            TextView tvName;
            ImageView ivProfileImage;
            Button btnAccept;
            Button btnReject;
            public FRViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnReject = itemView.findViewById(R.id.btnReject);
            }

            public void bindMessage(JSONObject message, int position) {
                String name = "";
                String tempUserId = null;
                try {
                    name = message.getString("name");
                    tempUserId = message.getString("userId");
                } catch (JSONException e) {
                    Log.e(TAG, "Error with json data", e);
                }
                tvName.setText(name);
                ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                if (tempUserId == null) {
                    return;
                }
                final String userId = tempUserId;
                userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error loading user data");
                        }
                        String profileUrl = user.getParseFile("profileImage").getUrl();
                        Glide.with(context)
                                .load(profileUrl)
                                .circleCrop() // create an effect of a round profile picture
                                .into(ivProfileImage);
                    }
                });
                // Make friends when accept is clicked
                btnAccept.setOnClickListener(v -> {
                    ParseQuery<ParseUser> currentUserQuery = ParseQuery.getQuery(ParseUser.class);
                    currentUserQuery.include("followers");
                    currentUserQuery.include(Inbox.KEY);
                    currentUserQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser currentUser, ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Couldn't get current user data", e);
                            }
                            ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                            userQuery.include("followers");
                            userQuery.include(Inbox.KEY);
                            userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser user, ParseException e1) {
                                    if (e1 != null) {
                                        Log.e(TAG, "Couldn't get current user data", e1);
                                    }
                                    String currentUserId = currentUser.getObjectId();
                                    Inbox currentInbox = (Inbox) currentUser.getParseObject(Inbox.KEY);
                                    JSONArray currentJsonInbox = currentInbox.getMessages();
                                    // Get the index of the friend request
                                    int index = Inbox.indexOfFriendRequest(currentJsonInbox, userId);
                                    // If the friend request isn't there, display message and return
                                    if (index == -1) {
                                        Log.i(TAG, "Friend request removed");
                                        Toasty.error(context, "Friend request has been removed", Toast.LENGTH_SHORT, true).show();
                                        return;
                                    }
                                    // Get and set friends for both users
                                    ParseObject currentFriendsObj = currentUser.getParseObject("followers");
                                    ParseObject friendsObj = user.getParseObject("followers");
                                    JSONArray currentFriends = currentFriendsObj.getJSONArray("friends");
                                    JSONArray friends = friendsObj.getJSONArray("friends");
                                    currentFriends.put(userId);
                                    friends.put(currentUserId);
                                    currentFriendsObj.put("friends", currentFriends);
                                    friendsObj.put("friends", friends);
                                    currentFriendsObj.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) {
                                                Toasty.error(context, "Error saving friends", Toast.LENGTH_SHORT, true).show();
                                                return;
                                            }
                                            Toasty.success(context, "Accepted friend request", Toast.LENGTH_SHORT, true).show();
                                        }
                                    });
                                    friendsObj.saveInBackground();
                                    // Remove friend request from inbox
                                    currentJsonInbox.remove(index);
                                    currentInbox.setMessages(currentJsonInbox);
                                    currentInbox.saveInBackground();

                                    // Remove friend request sent from other users inbox
                                    Inbox userInbox = (Inbox) user.getParseObject(Inbox.KEY);
                                    JSONArray userJsonInbox = userInbox.getMessages();
                                    int indexOfSent = Inbox.indexOfFriendRequestSent(userJsonInbox, currentUserId);
                                    if (indexOfSent == -1) {
                                        return;
                                    }
                                    userJsonInbox.remove(indexOfSent);
                                    userInbox.setMessages(userJsonInbox);
                                    userInbox.saveInBackground();
                                }
                            });


                        }
                    });
                    messages.remove(position);
                    notifyItemRemoved(position);
                });
                // Don't make friends when clicked on reject, remove items from inbox
                btnReject.setOnClickListener(v -> {
                    ParseQuery<ParseUser> currentUserQuery = ParseQuery.getQuery(ParseUser.class);
                    currentUserQuery.include(Inbox.KEY);
                    currentUserQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser currentUser, ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Couldn't get current user data", e);
                            }
                            ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                            userQuery.include(Inbox.KEY);
                            userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser user, ParseException e1) {
                                    if (e1 != null) {
                                        Log.e(TAG, "Couldn't get current user data", e1);
                                    }
                                    String currentUserId = currentUser.getObjectId();
                                    Inbox currentInbox = (Inbox) currentUser.getParseObject(Inbox.KEY);
                                    JSONArray currentJsonInbox = currentInbox.getMessages();
                                    // Get the index of the friend request
                                    int index = Inbox.indexOfFriendRequest(currentJsonInbox, userId);
                                    // If the friend request isn't there, display message and return
                                    if (index == -1) {
                                        Log.i(TAG, "Friend request removed");
                                        Toasty.error(context, "Friend request has been removed", Toast.LENGTH_SHORT, true).show();
                                        return;
                                    }
                                    // Remove friend request from inbox
                                    currentJsonInbox.remove(index);
                                    currentInbox.setMessages(currentJsonInbox);
                                    currentInbox.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) {
                                                Log.e(TAG, "Trouble rejecting friend request", e);
                                                Toasty.error(context, "Couldn't reject friend request", Toast.LENGTH_SHORT, true).show();
                                            } else {
                                                Log.i(TAG, "Removed friend request");
                                                Toasty.info(context, "Friend request rejected", Toast.LENGTH_SHORT, true).show();
                                            }
                                        }
                                    });

                                    // Remove friend request sent from other users inbox
                                    Inbox userInbox = (Inbox) user.getParseObject(Inbox.KEY);
                                    JSONArray userJsonInbox = userInbox.getMessages();
                                    int indexOfSent = Inbox.indexOfFriendRequestSent(userJsonInbox, currentUserId);
                                    if (indexOfSent == -1) {
                                        return;
                                    }
                                    userJsonInbox.remove(indexOfSent);
                                    userInbox.setMessages(userJsonInbox);
                                    userInbox.saveInBackground();
                                }
                            });


                        }
                    });
                    messages.remove(position);
                    notifyItemRemoved(position);
                });
            }
        }
        public class FRSentViewHolder extends MessageViewHolder {

            TextView tvName;
            ImageView ivProfileImage;
            Button btnDelete;

            public FRSentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }

            @Override
            void bindMessage(JSONObject message, int position) {
                String name = "";
                String tempUserId = null;
                try {
                    name = message.getString("name");
                    tempUserId = message.getString("userId");
                } catch (JSONException e) {
                    Log.e(TAG, "Error with json data", e);
                }
                tvName.setText(name);
                ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                if (tempUserId == null) {
                    return;
                }
                final String userId = tempUserId;
                userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error loading user data");
                        }
                        String profileUrl = user.getParseFile("profileImage").getUrl();
                        Glide.with(context)
                                .load(profileUrl)
                                .circleCrop() // create an effect of a round profile picture
                                .into(ivProfileImage);
                    }
                });
                // Don't make friends when clicked on delete, remove items from inbox
                btnDelete.setOnClickListener(v -> {
                    ParseQuery<ParseUser> currentUserQuery = ParseQuery.getQuery(ParseUser.class);
                    currentUserQuery.include(Inbox.KEY);
                    currentUserQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser currentUser, ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Couldn't get current user data", e);
                            }
                            ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                            userQuery.include(Inbox.KEY);
                            userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser user, ParseException e1) {
                                    if (e1 != null) {
                                        Log.e(TAG, "Couldn't get current user data", e1);
                                    }
                                    String currentUserId = currentUser.getObjectId();
                                    Inbox currentInbox = (Inbox) currentUser.getParseObject(Inbox.KEY);
                                    JSONArray currentJsonInbox = currentInbox.getMessages();
                                    // Get the index of the friend request
                                    int index = Inbox.indexOfFriendRequestSent(currentJsonInbox, userId);
                                    // If the friend request isn't there, display message and return
                                    if (index == -1) {
                                        Log.i(TAG, "Couldn't find friend request");
                                        Toasty.error(context, "Friend request couldn't be found", Toast.LENGTH_SHORT, true).show();
                                        return;
                                    }
                                    // Remove friend request from inbox
                                    currentJsonInbox.remove(index);
                                    currentInbox.setMessages(currentJsonInbox);
                                    currentInbox.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) {
                                                Log.e(TAG, "Trouble deleting friend request", e);
                                                Toasty.error(context, "Couldn't delete friend request", Toast.LENGTH_SHORT, true).show();
                                            } else {
                                                Log.i(TAG, "Removed friend request");
                                                Toasty.info(context, "Friend request deleted", Toast.LENGTH_SHORT, true).show();
                                            }
                                        }
                                    });

                                    // Remove friend request sent from other users inbox
                                    Inbox userInbox = (Inbox) user.getParseObject(Inbox.KEY);
                                    JSONArray userJsonInbox = userInbox.getMessages();
                                    int indexOfSent = Inbox.indexOfFriendRequest(userJsonInbox, currentUserId);
                                    if (indexOfSent == -1) {
                                        return;
                                    }
                                    userJsonInbox.remove(indexOfSent);
                                    userInbox.setMessages(userJsonInbox);
                                    userInbox.saveInBackground();
                                }
                            });


                        }
                    });
                    messages.remove(position);
                    notifyItemRemoved(position);
                });
            }
        }
    }
