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
import com.example.travelo.MessagesActivity;
import com.example.travelo.R;
import com.example.travelo.RoomActivity;
import com.example.travelo.models.Messages;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.MessageViewHolder> {
    private List<JSONObject> messages;
    private Context context;
    private String username;

    public static final String TAG = "InboxAdapter";
    private static final int MESSAGE_DM = 629;
    private static final int MESSAGE_ROOM = 1229;
    public static final int DM_LENGTH = 4;
    public static final int ROOM_LENGTH = 1;

    public InboxAdapter(List<JSONObject> list, Context c) {
        messages = list;
        context = c;
    }

    @Override
    public int getItemViewType(int position) {
        int type = typeOfMessage(position);
        switch (type) {
            case DM_LENGTH:
                return MESSAGE_DM;
            case ROOM_LENGTH:
                return MESSAGE_ROOM;
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
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        JSONObject obj = messages.get(position);
        holder.bindMessage(obj);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private int typeOfMessage(int position) {
        JSONObject message = messages.get(position);
        return message.length();

    }

    public abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(JSONObject message);
    }

    public class RoomMessageViewHolder extends MessageViewHolder {
        RecyclerView rvUsers;
        TextView tvRoomId;
        Button btnJoin;

        public RoomMessageViewHolder(View itemView) {
            super(itemView);
            tvRoomId = (TextView) itemView.findViewById(R.id.tvRoomId);
            rvUsers = (RecyclerView) itemView.findViewById(R.id.rvUsers);
            btnJoin = (Button) itemView.findViewById(R.id.btnJoin);


        }

        @Override
        public void bindMessage(JSONObject message) {
            String roomObjectId = message.keys().next();
            ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
            query.getInBackground(roomObjectId, new GetCallback<Room>() {
                @Override
                public void done(Room room, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Couldn't bind room message", e);
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

                }
            });
        }

        public void goToRoom(String roomObjectId) {
            Log.i(TAG, "Going to room");
            // Specify which class to query
            ParseQuery<Room> query = ParseQuery.getQuery(Room.class);
            // Find the room asynchronously
            query.getInBackground(roomObjectId, new GetCallback<Room>() {
                @Override
                public void done(Room room, ParseException error) {
                    if (error != null) {
                        Log.e(TAG, "Error joining room", error);
                        return;
                    }

                    if (room == null) {
                        Toast.makeText(context, "This room is no longer available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!room.getJoinable()) {
                        if (!room.getOwner().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            Toast.makeText(context, "This is room is no longer joinable", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Intent intent = new Intent(context, RoomActivity.class);
                    intent.putExtra("room", roomObjectId);
                    context.startActivity(intent);
                }

            });
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
            public void bindMessage(JSONObject dm) {
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
    }
