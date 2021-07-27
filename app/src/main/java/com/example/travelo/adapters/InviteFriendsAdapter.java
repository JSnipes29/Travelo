package com.example.travelo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelo.R;
import com.example.travelo.constants.Constant;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.ViewHolder> {

    public static final String TAG = "InviteFriendsAdapter";

    DialogFragment fragment;
    Context context;
    List<String> users;
    String roomObjectId;

    public InviteFriendsAdapter(Context context, List<String> users, String roomObjectId, DialogFragment fragment) {
        this.context = context;
        this.users = users;
        this.fragment = fragment;
        this.roomObjectId = roomObjectId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_invite_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteFriendsAdapter.ViewHolder holder, int position) {
        String userId = users.get(position);
        holder.bind(userId, roomObjectId);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        ImageView ivProfileImage;
        Button btnInvite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            btnInvite = itemView.findViewById(R.id.btnInvite);
        }

        public void bind(String userId, String roomObjectId) {
            ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
            userQuery.getInBackground(userId, new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    String username = user.getUsername();
                    tvName.setText(username);
                    String profileUrl = user.getParseFile("profileImage").getUrl();
                    if (profileUrl != null) {
                        Glide.with(context)
                                .load(profileUrl)
                                .circleCrop()
                                .into(ivProfileImage);
                    }
                }
            });
            // Set up button
            btnInvite.setClickable(false);
            ParseQuery<Room> roomQuery = ParseQuery.getQuery(Room.class);
            roomQuery.getInBackground(roomObjectId, new GetCallback<Room>() {
                @Override
                public void done(Room room, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Trouble getting room data", e);
                        Toasty.error(context, "Trouble getting room data", Toast.LENGTH_SHORT, true).show();
                        btnInvite.setClickable(true);
                        return;
                    }
                    btnInvite.setOnClickListener(v -> Constant.invite(context, room, userId, TAG, fragment));
                    btnInvite.setClickable(true);
                }
            });
        }
    }
}
