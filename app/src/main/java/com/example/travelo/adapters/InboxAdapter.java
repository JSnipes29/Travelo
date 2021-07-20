package com.example.travelo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.R;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

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
    public static final int DM_LENGTH = 5;
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
            View contactView = inflater.inflate(R.layout.message_incoming, parent, false);
            return new OutgoingMessageViewHolder(contactView);
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

        public RoomMessageViewHolder(View itemView) {
            super(itemView);
            tvRoomId = (TextView) itemView.findViewById(R.id.tvRoomId);
            rvUsers = (RecyclerView) itemView.findViewById(R.id.rvUsers);
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
                    while(iter.hasNext()) {
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
                }
            });
        }
    }

        public class OutgoingMessageViewHolder extends MessageViewHolder {
            ImageView imageMe;
            TextView body;

            public OutgoingMessageViewHolder(View itemView) {
                super(itemView);
                imageMe = (ImageView) itemView.findViewById(R.id.ivProfile);
                body = (TextView) itemView.findViewById(R.id.tvBody);
            }

            @Override
            public void bindMessage(JSONObject message) {
                String profileUrl = null;
                String text = null;
                try {
                    profileUrl = message.getString("profileImageUrl");
                    text = message.getString("body");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Glide.with(context)
                        .load(profileUrl)
                        .circleCrop() // create an effect of a round profile picture
                        .into(imageMe);
                body.setText(text);
            }
        }
    }
