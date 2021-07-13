package com.example.travelo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private JSONArray messages;
    private Context context;
    private String username;

    private static final int MESSAGE_OUTGOING = 123;
    private static final int MESSAGE_INCOMING = 321;

    public ChatAdapter(JSONArray arr, Context c, String username) {
        messages = arr;
        context = c;
        this.username = username;
    }

    @Override
    public int getItemViewType(int position) {
        if (isMe(position)) {
            return MESSAGE_OUTGOING;
        } else {
            return MESSAGE_INCOMING;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // If incoming message, inflate income message layout
        // else inflate outgoing message layout
        if (viewType == MESSAGE_INCOMING) {
            View contactView = inflater.inflate(R.layout.message_incoming, parent, false);
            return new IncomingMessageViewHolder(contactView);
        } else if (viewType == MESSAGE_OUTGOING) {
            View contactView = inflater.inflate(R.layout.message_outgoing, parent, false);
            return new OutgoingMessageViewHolder(contactView);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MessageViewHolder holder, int position) {
        JSONObject obj = new JSONObject();
        try {
            obj = messages.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.bindMessage(obj);
    }

    @Override
    public int getItemCount() {
        return messages.length();
    }

    private boolean isMe(int position) {
        JSONObject message = null;
        try {
            message = messages.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (message == null) {
            return false;
        }
        boolean res = false;
        try {
            res = message.getString("username").equals(username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }
    public abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(JSONObject message);
    }

    public class IncomingMessageViewHolder extends MessageViewHolder {
        ImageView imageOther;
        TextView body;
        TextView name;

        public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            imageOther = (ImageView)itemView.findViewById(R.id.ivProfileOther);
            body = (TextView)itemView.findViewById(R.id.tvBody);
            name = (TextView)itemView.findViewById(R.id.tvName);
        }

        @Override
        public void bindMessage(JSONObject message) {
            String username = null;
            String profileUrl = null;
            String text = null;
            try {
                username = message.getString("username");
                profileUrl = message.getString("profileImageUrl");
                text = message.getString("body");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Glide.with(context)
                    .load(profileUrl)
                    .circleCrop() // create an effect of a round profile picture
                    .into(imageOther);
            body.setText(text);
            name.setText(username); // in addition to message show user ID
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
