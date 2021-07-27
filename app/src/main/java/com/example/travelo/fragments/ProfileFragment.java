package com.example.travelo.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travelo.listeners.EndlessRecyclerViewScrollListener;
import com.example.travelo.activities.MessagesActivity;
import com.example.travelo.R;
import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.adapters.PostAdapter;
import com.example.travelo.databinding.FragmentProfileBinding;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Messages;
import com.example.travelo.models.Post;
import com.example.travelo.models.Room;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    FragmentProfileBinding binding;
    ParseUser user;
    PostAdapter postAdapter;
    List<Post> posts;
    EndlessRecyclerViewScrollListener scrollListener;
    public static final int GALLERY_REQUEST = 12;
    public static final int LIMIT = 10;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        user = (ParseUser) Parcels.unwrap(getArguments().getParcelable("user"));
        binding.btnMessage.setVisibility(View.GONE);
        binding.btnFollow.setVisibility(View.GONE);
        binding.btnInvite.setVisibility(View.GONE);
        binding.btnFriend.setVisibility(View.GONE);
        if (user == null) {
            ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
            query.include("followers");
            query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    user = object;
                    if (binding != null) {
                        binding.ivProfileImage.setOnClickListener(v -> setProfileImage());
                        setUpProfileFragment();
                    }
                }
            });
        } else {
            setUpProfileFragment();
        }
        return view;
    }


    public void setUpProfileFragment() {
        binding.tvName.setText(user.getUsername());
        Glide.with(getContext())
                .load(user.getParseFile("profileImage").getUrl())
                .circleCrop()
                .into(binding.ivProfileImage);
        List<String> friends = jsonToList(user.getParseObject("followers").getJSONArray("friends"));
        List<String> following = jsonToList(user.getJSONArray("following"));
        List<String> followers = jsonToList(user.getParseObject("followers").getJSONArray("followers"));
        binding.tvFollowersCount.setText(String.valueOf(followers.size()));
        binding.tvFollowingCount.setText(String.valueOf(following.size()));
        // Show the following, message, etc button if the user is not the current user
        String currentUserId = ParseUser.getCurrentUser().getObjectId();
        if (!user.getObjectId().equals(currentUserId)) {
            binding.btnFollow.setVisibility(View.VISIBLE);
            binding.btnMessage.setVisibility(View.VISIBLE);
            binding.btnInvite.setVisibility(View.VISIBLE);
            binding.btnFriend.setVisibility(View.VISIBLE);
            binding.btnMessage.setOnClickListener(v -> goToMessages());
            binding.btnInvite.setOnClickListener(v -> invite());
            if (followers.contains(currentUserId)) {
                Log.i(TAG, "Following");
                binding.btnFollow.setText(R.string.following);
                binding.btnFollow.setOnClickListener(v -> unFollow());
            } else {
                binding.btnFollow.setText(R.string.follow);
                binding.btnFollow.setOnClickListener(v -> follow());
            }
            if (friends.contains(currentUserId)) {
                Log.i(TAG, "Friends");
                binding.btnFriend.setText(R.string.unfriend);
                binding.btnFriend.setOnClickListener(v -> unFriend());
            } else {
                binding.btnFriend.setText(R.string.friend);
                binding.btnFriend.setOnClickListener(v -> friend());
            }
        }
        // Bind posts to recycler view
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, (AppCompatActivity)getActivity());
        binding.rvPosts.setAdapter(postAdapter);
        LinearLayoutManager postLayoutManager = new LinearLayoutManager(getContext());
        binding.rvPosts.setLayoutManager(postLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(postLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                queryPosts(1);
            }
        };
        binding.rvPosts.addOnScrollListener(scrollListener);
        queryPosts(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void follow() {
        binding.tvFollowersCount.setText(String.valueOf(1 + Integer.parseInt(binding.tvFollowersCount.getText().toString())));
        // Button now unfollows on click
        binding.btnFollow.setText(R.string.following);
        binding.btnFollow.setOnClickListener(v -> unFollow());
        binding.btnFollow.setClickable(false);
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser currentUser, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Couldn't find current user", e);
                }
                ParseQuery<ParseUser> updateUser = ParseQuery.getQuery(ParseUser.class);
                updateUser.getInBackground(user.getObjectId(), new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser updatedUser, ParseException e) {
                        // Update user
                        user = updatedUser;
                        // Add current user to users followers
                        ParseObject followersObject = user.getParseObject("followers");
                        JSONArray followers = followersObject.getJSONArray("followers");
                        followers.put(currentUser.getObjectId());
                        user.getParseObject("followers").put("followers", followers);
                        // Add user to current users following
                        JSONArray following = currentUser.getJSONArray("following");
                        following.put(user.getObjectId());
                        currentUser.put("following", following);
                        // Save both to the Parse server
                        followersObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Couldn't update followers");
                                } else {
                                    Log.i(TAG, "Updated followers");
                                }
                            }
                        });
                        currentUser.saveInBackground();

                        // Button is clickable again
                        if (binding != null) {
                            binding.btnFollow.setClickable(true);
                        }
                    }
                });


            }
        });

    }

    public void unFollow() {
        // Button now follows on click
        binding.btnFollow.setText(R.string.follow);
        binding.btnFollow.setOnClickListener(v -> follow());
        binding.tvFollowersCount.setText(String.valueOf(Integer.parseInt(binding.tvFollowersCount.getText().toString()) - 1));
        binding.btnFollow.setClickable(false);
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser currentUser, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Couldn't find current user", e);
                    Toasty.error(getContext(), "Error finding user", Toasty.LENGTH_SHORT, true).show();
                    return;
                }
                ParseQuery<ParseUser> updateUser = ParseQuery.getQuery(ParseUser.class);
                updateUser.getInBackground(user.getObjectId(), new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser updatedUser, ParseException e) {
                        // Update user
                        user = updatedUser;
                        // Delete current user from users followers
                        ParseObject followersObject = user.getParseObject("followers");
                        JSONArray followers = followersObject.getJSONArray("followers");
                        followers = jsonDelete(followers, (currentUser.getObjectId()));
                        user.getParseObject("followers").put("followers", followers);
                        // Delete user from current users following
                        JSONArray following = currentUser.getJSONArray("following");
                        following = jsonDelete(following, user.getObjectId());
                        currentUser.put("following", following);
                        // Save both to the Parse server
                        followersObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Couldn't update followers");
                                } else {
                                    Log.i(TAG, "Updated followers");
                                }
                            }
                        });
                        currentUser.saveInBackground();

                        // Button is clickable again
                        if (binding != null) {
                            binding.btnFollow.setClickable(true);
                        }
                    }
                });


            }
        });

    }

    public void friend() {
        binding.btnFriend.setClickable(false);
        ParseQuery<ParseUser> userParseQuery = ParseQuery.getQuery(ParseUser.class);
        userParseQuery.include(Inbox.KEY);
        userParseQuery.getInBackground(user.getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Problem loading user data from server", e);
                    if (binding != null) {
                        binding.btnFriend.setClickable(true);
                    }
                    return;
                }
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                String currentUserId = ParseUser.getCurrentUser().getObjectId();
                String currentUsername = ParseUser.getCurrentUser().getUsername();
                int index = Inbox.indexOfFriendRequest(jsonInbox, currentUserId);
                // If the user already has an invite, return
                if (index != -1) {
                    Toasty.info(getContext(), "Friend request already sent", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                JSONObject friendRequest = new JSONObject();
                try {
                    friendRequest.put("userId", currentUserId);
                    friendRequest.put("name", currentUsername);
                    friendRequest.put("id", InboxAdapter.FR_ID);
                } catch (JSONException jsonException) {
                    Log.e(TAG, "Couldn't edit json data", jsonException);
                }
                jsonInbox.put(friendRequest);
                inbox.setMessages(jsonInbox);
                inbox.saveInBackground(exception -> {
                    if (exception != null) {
                        Log.e(TAG, "Couldn't save friend request message in inbox", exception);
                    } else {
                        Log.i(TAG, "Friend request saved in inbox");
                        if (getContext() != null) {
                            Toasty.success(getContext(), "Friend Request Sent", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
                if (binding != null) {
                    binding.btnFriend.setClickable(true);
                }
            }
        });
    }

    public void unFriend() {
        // Button now follows on click
        binding.btnFriend.setText(R.string.friend);
        binding.btnFriend.setOnClickListener(v -> friend());
        binding.btnFriend.setClickable(false);
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.include("followers");
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            public void done(ParseUser currentUser, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Couldn't find current user", e);
                }
                ParseQuery<ParseUser> updateUser = ParseQuery.getQuery(ParseUser.class);
                updateUser.include("followers");
                updateUser.getInBackground(user.getObjectId(), new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser updatedUser, ParseException e) {
                        // Update user
                        user = updatedUser;
                        // Remove current user from users friends
                        ParseObject followersObject = user.getParseObject("followers");
                        JSONArray friends = followersObject.getJSONArray("friends");
                        friends = jsonDelete(friends, (currentUser.getObjectId()));
                        user.getParseObject("followers").put("friends", friends);
                        // Remove user from current users friends
                        ParseObject currentFriendsObject = currentUser.getParseObject("followers");
                        JSONArray currentFriends = currentFriendsObject.getJSONArray("friends");
                        currentFriends = jsonDelete(currentFriends, user.getObjectId());
                        currentFriendsObject.put("friends", currentFriends);
                        // Save both to the Parse server
                        followersObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Couldn't update followers");
                                } else {
                                    Log.i(TAG, "Updated followers");
                                }
                            }
                        });
                        currentFriendsObject.saveInBackground();

                        // Button is clickable again
                        if (binding != null) {
                            binding.btnFollow.setClickable(true);
                        }
                    }
                });


            }
        });
    }

    private void queryPosts(int parameter) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_OWNER);
        query.setLimit(LIMIT);
        query.whereEqualTo("userArray", user.getUsername());
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        final int start;
        if (parameter == 1) {
            start = posts.size();
            query.whereLessThan(Post.KEY_CREATED_AT, posts.get(posts.size() - 1).getCreatedAt());
        } else {
            start = 0;
        }
        if (parameter == 0 && binding != null) {
            binding.shimmerLayout.startShimmer();
        }
        query.findInBackground((posts, e) -> {
            if (e != null) {
                Log.e(TAG, "Couldn't get post", e);
                return;
            }
            for (Post post: posts) {
                Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getOwner().getUsername());
            }
            if (parameter == 1) {
                Log.i(TAG, "Endless scrolling in effect");
            }
            this.posts.addAll(posts);
            if (parameter == 1) {
                postAdapter.notifyItemRangeInserted(start, posts.size());
            } else {
                postAdapter.notifyDataSetChanged();
            }
            if (parameter == 0 && binding != null) {
                binding.shimmerLayout.setVisibility(View.GONE);
                binding.rvPosts.setVisibility(View.VISIBLE);
            }
            scrollListener.resetState();
        });
    }

    // Go to the messages view so user can type new messages
    public void goToMessages() {
        Log.i(TAG, "Clicked on messages button");
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.include(Inbox.KEY);
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser currentUser, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error loading current user data from server", e);
                    return;
                }
                Inbox currentUserInbox = (Inbox)currentUser.getParseObject(Inbox.KEY);
                JSONArray currentUserMessages = currentUserInbox.getMessages();
                ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
                userQuery.include(Inbox.KEY);
                userQuery.getInBackground(user.getObjectId(), new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser user, ParseException exception) {
                        if (exception != null) {
                            Log.e(TAG, "Error loading user data from server", exception);
                        }
                        Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                        JSONArray userMessages = inbox.getMessages();
                        int currentUserIndex = indexOfDm(currentUserMessages, user.getObjectId());
                        int userIndex = indexOfDm(userMessages, currentUser.getObjectId());
                        String messageId = null;
                        // If they are both -1 create new message objects for both user
                        if (currentUserIndex == -1 && userIndex == -1) {
                            Log.i(TAG, "Both users message inbox not setup");
                            Messages messages = new Messages();
                            messages.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    String messageId = messages.getObjectId();
                                    setupInbox(currentUserInbox, inbox, currentUserMessages, userMessages,
                                            currentUser, user, messages.getObjectId());
                                    inbox.saveInBackground();
                                    messages.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            currentUserInbox.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    startMessagesActivity(getContext(), messages.getObjectId());
                                                }
                                            });
                                        }
                                    });
                                }
                            });


                        } else if (currentUserIndex == -1) {
                            // userIndex != -1 (The current user has the message deleted)
                            Log.i(TAG, "Only other user message inbox setup");
                            try {
                                messageId = userMessages.getJSONObject(userIndex).getString("messages");
                            } catch (JSONException jsonException) {
                                Log.e(TAG, "Trouble loading user inbox data");
                            }
                            setupInbox(currentUserInbox, currentUserMessages,
                                    user.getObjectId(), user.getUsername(), user.getParseFile("profileImage").getUrl(), messageId);
                            final String messageIdCopy = messageId;
                            currentUserInbox.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    startMessagesActivity(getContext(), messageIdCopy);
                                }
                            });
                        } else if (userIndex == -1) {
                            // currenterUserIndex != -1 && userIndex == -1 (other user has message deleted)
                            Log.i(TAG, "Only current user inbox setup");
                            try {
                                messageId = currentUserMessages.getJSONObject(currentUserIndex).getString("messages");
                            } catch (JSONException jsonException) {
                                Log.e(TAG, "Trouble loading user inbox data");
                            }
                            setupInbox(inbox, userMessages,
                                    currentUser.getObjectId(), currentUser.getUsername(), currentUser.getParseFile("profileImage").getUrl(), messageId);
                            inbox.saveInBackground();
                            startMessagesActivity(getContext(),messageId);
                        } else {
                            // currentUserIndex != -1 && userIndex != -1 Both users have the messages saved
                            Log.i(TAG, "Both inboxes setup");
                            try {
                                messageId = currentUserMessages.getJSONObject(currentUserIndex).getString("messages");
                            } catch (JSONException jsonException) {
                                jsonException.printStackTrace();
                                Log.e(TAG, "Error loading inbox json data", jsonException);
                            }
                            startMessagesActivity(getContext(), messageId);

                        }

                    }
                });

            }
        });

    }

    public static void startMessagesActivity(Context context, String messagesId) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("messagesId", messagesId);
        context.startActivity(intent);
    }

    private static void setupInbox(Inbox inbox, JSONArray jsonInbox, String userId, String username, String profileImage, String messageId) {
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("userId", userId);
            jsonMessage.put("username", username);
            jsonMessage.put("profileImage", profileImage);
            jsonMessage.put("messages", messageId);
            jsonMessage.put("id", InboxAdapter.DM_ID);
        } catch (JSONException e) {
            Log.e(TAG, "Trouble setting up inbox", e);
        }
        jsonInbox.put(jsonMessage);
        inbox.setMessages(jsonInbox);
    }

    private static void setupInbox(Inbox currentUserInbox, Inbox userInbox, JSONArray jsonCurrentInbox, JSONArray jsonUserInbox, ParseUser currentUser, ParseUser user, String messageId) {
        JSONObject jsonCurrentMessage = new JSONObject();
        JSONObject jsonUserMessage = new JSONObject();
        try {
            jsonCurrentMessage.put("userId", user.getObjectId());
            jsonCurrentMessage.put("username", user.getUsername());
            jsonCurrentMessage.put("profileImage", user.getParseFile("profileImage").getUrl());
            jsonCurrentMessage.put("messages", messageId);
            jsonCurrentMessage.put("id", InboxAdapter.DM_ID);
            jsonUserMessage.put("userId", currentUser.getObjectId());
            jsonUserMessage.put("username", currentUser.getUsername());
            jsonUserMessage.put("profileImage", currentUser.getParseFile("profileImage").getUrl());
            jsonUserMessage.put("messages", messageId);
            jsonUserMessage.put("id", InboxAdapter.DM_ID);
        } catch (JSONException e) {
            Log.e(TAG, "Trouble setting up inbox", e);
        }
        jsonUserInbox.put(jsonUserMessage);
        userInbox.setMessages(jsonUserInbox);

        jsonCurrentInbox.put(jsonCurrentMessage);
        currentUserInbox.setMessages(jsonCurrentInbox);
    }

    private static int indexOfDm(JSONArray array, String userId) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject message = array.getJSONObject(i);
                // If the message isn't a dm (a room message), continue
                if (message.getInt("id") != InboxAdapter.DM_ID) {
                    continue;
                }
                // If the message contains the user id return the index
                String jsonUserId = message.getString("userId");
                if (jsonUserId.equals(userId)) {
                    return i;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error reading json data", e);
            }
        }
        return -1;
    }

    private JSONArray jsonDelete(JSONArray jsonArray, String query) {
        JSONArray res = jsonArray;
        for (int i = 0; i < res.length(); i++) {
            try {
                String q = res.getString(i);
                if (q.equals(query)) {
                    res.remove(i);
                    return res;
                }
            } catch (JSONException e) {
                Log.e(TAG,"Error deleting from json array" ,e);
            }
        }
        return res;
    }

    public List<String> jsonToList(JSONArray array) {
        List<String> res = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++){
                try {
                    res.add(array.getString(i));
                } catch (JSONException e) {
                    Log.e(TAG, "Error converting json array to list", e);
                }

            }
        }
        return res;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        // Compress image to lower quality scale 1 - 100
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] image = stream.toByteArray();
                        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
                        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                ParseFile parseFile = new ParseFile(user.getUsername() + "_pic.jpeg", image);
                                user.put("profileImage", parseFile);
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.e(TAG, "Trouble updating profile pic");
                                            return;
                                        }
                                        Toast.makeText(getContext(), "You have updated your profile image", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        });

                    } catch (IOException e) {
                        Log.e(TAG, "Problem with uploading profile image",e);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // Start profile image setup by going to the phones built in image gallery
    public void setProfileImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
        Log.i(TAG, "activity started");
    }

    public void invite() {
        FragmentManager fm = getParentFragmentManager();
        InviteFragment inviteFragment = new InviteFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getObjectId());
        inviteFragment.setArguments(bundle);
        inviteFragment.show(fm, "Invite");
    }

}