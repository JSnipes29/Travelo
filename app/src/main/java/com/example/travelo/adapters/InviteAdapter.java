package com.example.travelo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelo.R;
import com.example.travelo.models.Inbox;
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

import es.dmoral.toasty.Toasty;

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.ViewHolder> {

    Context context;
    List<Room> rooms;
    String userId;
    DialogFragment fragment;
    public static final String TAG = "InboxAdapter";

    public InviteAdapter(Context context, List<Room> rooms, String userId, DialogFragment fragment) {
        this.context = context;
        this.rooms = rooms;
        this.userId = userId;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invite_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteAdapter.ViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RecyclerView rvUsers;
        TextView tvRoomId;
        Button btnInvite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomId = (TextView) itemView.findViewById(R.id.tvRoomId);
            rvUsers = (RecyclerView) itemView.findViewById(R.id.rvUsers);
            btnInvite = (Button) itemView.findViewById(R.id.btnInvite);
        }

        public void bind(Room room) {
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
                    Log.e("InviteAdapter", "Error getting users", error);
                }
            }
            UsersAdapter userAdapter = new UsersAdapter(context, users);
            rvUsers.setAdapter(userAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rvUsers.setLayoutManager(linearLayoutManager);
            btnInvite.setOnClickListener(v -> invite(room, userId));
        }

        public void invite(Room room, String userId) {
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
            fragment.dismiss();
        }
    }
}
