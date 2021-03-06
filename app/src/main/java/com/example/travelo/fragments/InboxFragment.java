package com.example.travelo.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelo.R;
import com.example.travelo.activities.MainActivity;
import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.FragmentInboxBinding;
import com.example.travelo.models.Inbox;
import com.example.travelo.models.Messages;
import com.example.travelo.search.Corpus;
import com.example.travelo.search.Document;
import com.example.travelo.search.VectorSpaceModel;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class InboxFragment extends Fragment implements MainFragment{

    FragmentInboxBinding binding;
    InboxAdapter inboxAdapter;
    List<JSONObject> list;
    SearchView searchView;
    boolean allMessages;
    public static final String TAG = "InboxFragment";

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        list = new ArrayList<>();
        inboxAdapter = new InboxAdapter(list, getContext());
        binding.rvInbox.setAdapter(inboxAdapter);
        LinearLayoutManager inboxLayoutManager = new LinearLayoutManager(getContext());
        binding.rvInbox.setLayoutManager(inboxLayoutManager);
        queryInbox(0, null);
        Bundle bundle = getArguments();
        if (bundle != null) {
            allMessages = bundle.getBoolean("allMessages", false);
        } else {
            allMessages = false;
        }
        // Configure swipe to remove
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = null;
        if (!allMessages) {
            simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    Log.i(TAG, "On move");
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    //Remove swiped item from list and notify the RecyclerView
                    int position = viewHolder.getAdapterPosition();
                    JSONObject message = list.get(position);
                    list.remove(position);
                    inboxAdapter.notifyItemRemoved(position);
                    archiveMessage(message, position);

                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tomato))
                            .addActionIcon(R.drawable.ic_baseline_archive_24)
                            .create()
                            .decorate();
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
        } else if (allMessages) {
            binding.inboxBar.setVisibility(View.INVISIBLE);
            simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    Log.i(TAG, "On move");
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    //Remove swiped item from list and notify the RecyclerView
                    int position = viewHolder.getAdapterPosition();
                    JSONObject message = list.get(position);
                    list.remove(position);
                    inboxAdapter.notifyItemRemoved(position);
                    unArchive(message, position);

                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gold))
                            .addActionIcon(R.drawable.ic_baseline_unarchive_24)
                            .create()
                            .decorate();
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
        }
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvInbox);
        // Setup refresh listener which triggers new data loading
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String query = null;
                if (searchView != null) {
                    query = searchView.getQuery().toString();
                }
                queryInbox(2, query);
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        searchView = binding.searchView;
        // Setup search item
        setupSearch();

        // Set up the app bar
        if (getActivity() instanceof MainActivity) {
            binding.inboxBar.setOnMenuClickedListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open the draw when menu is clicked
                    if (binding != null) {
                        ((MainActivity) getActivity()).openDrawer();
                    }
                }
            });
        }
        return view;
    }

    // parameter 0 == starting
    public void queryInbox(int parameter, String query) {
        // Start shimmer effect
        if (parameter == 0 && binding != null) {
            binding.shimmerLayout.startShimmer();
        } else if (parameter == 1 && binding != null) {
            binding.rvInbox.setVisibility(View.GONE);
            binding.shimmerLayout.startShimmer();
        }
        // Get the inbox of the current user
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.include(Inbox.KEY);
        userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error loading inbox data", e);
                    return;
                }
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                jsonToList(jsonInbox, query, parameter);
                if (binding != null) {
                    binding.shimmerLayout.setVisibility(View.GONE);
                    binding.rvInbox.setVisibility(View.VISIBLE);
                    if (parameter == 0 && list.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    // Convert a json array to a list to use for the inbox adapter
    public void jsonToList(JSONArray array, String query, int parameter) {
        list.clear();
        inboxAdapter.notifyDataSetChanged();
        if (query == null || query.isEmpty()) {
            for (int i = array.length() - 1; i >= 0; i--) {
                JSONObject jsonMessage = null;
                try {
                    jsonMessage = array.getJSONObject(i);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
                if (!allMessages) {
                    try {
                        boolean archived = jsonMessage.getBoolean("archived");
                        if (archived) {
                            continue;
                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                }
                try {
                    list.add(array.getJSONObject(i));
                } catch (JSONException e) {
                    Log.e(TAG, "Error loading data to json", e);

                }
            }
            if (binding != null && parameter == 2) {
                binding.swipeContainer.setRefreshing(false);
            }
        } else {
            // Use vector space model to get the most relevant results from the query
            // Create a list of documents
            ArrayList<Document> documents = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject jsonMessage = array.getJSONObject(i);
                    String text;
                    int messageType = jsonMessage.getInt("id");
                    switch (messageType) {
                        case InboxAdapter.ROOM_ID:
                            text = jsonMessage.getString("roomId") + " room rooms";
                            break;
                        case InboxAdapter.DM_ID:
                            String messageId = jsonMessage.getString("messages");
                            ParseQuery<Messages> messagesQuery = ParseQuery.getQuery(Messages.class);
                            Messages messages = messagesQuery.get(messageId);
                            JSONArray jsonMessages = messages.getMessages();
                            StringBuilder textBuilder = new StringBuilder(jsonMessage.getString("username") + " ");
                            for (int k = 0; k < jsonMessages.length(); k++) {
                                JSONObject message = jsonMessages.getJSONObject(k);
                                String body = message.getString("body");
                                textBuilder.append(body).append(" ");
                            }
                            text = textBuilder.toString();
                            Log.i(TAG, "Body: " + text);
                            break;
                        case InboxAdapter.FR_ID:
                            text = jsonMessage.getString("name") + " friend request";
                            break;
                        case InboxAdapter.FR_SENT_ID:
                            text = jsonMessage.getString("name") + " friend request sent send";
                                break;
                        default:
                            text = jsonMessage.getString("username");
                            break;
                    }
                    Document doc = new Document(text);
                    documents.add(doc);
                } catch (JSONException | ParseException e) {
                    Log.e(TAG, "Error loading data to json", e);
                }
            }
            Document queryDoc = new Document(query);
            documents.add(queryDoc);
            double [] cosineSimiliarityValues = new double[documents.size() - 1];
            Corpus corpus = new Corpus(documents);
            VectorSpaceModel vectorSpaceModel = new VectorSpaceModel(corpus);
            // Get the cosine similarity value for each document against the query
            for (int j = 0; j < cosineSimiliarityValues.length; j++) {
                Document doc = documents.get(j);
                double value = vectorSpaceModel.cosineSimilarity(doc, queryDoc);
                cosineSimiliarityValues[j] = value;
            }
            try {
                // Sort the jsonArray and by the cosine similarity values
                for (int i = 1; i < cosineSimiliarityValues.length; i++) {
                    double current = cosineSimiliarityValues[i];
                    JSONObject currentObj = array.getJSONObject(i);
                    int j = i - 1;
                    while (j >= 0 && current > cosineSimiliarityValues[j]) {
                        cosineSimiliarityValues[j + 1] = cosineSimiliarityValues[j];
                        array.put(j + 1, array.getJSONObject(j));
                        j--;
                    }
                    // at this point we've exited, so j is either -1
                    // or it's at the first element where current >= a[j]
                    cosineSimiliarityValues[j + 1] = current;
                    array.put(j + 1, currentObj);
                }
            } catch (JSONException jsonException) {
                Log.e(TAG, "Couldn't read from json data", jsonException);
            }

            for (double x: cosineSimiliarityValues) {
                Log.i(TAG, String.valueOf(x));
            }
            // Convert json array to list, don't add if cosine similarity value is 0
            for (int i = 0; i < array.length(); i++) {
                if (cosineSimiliarityValues[i] == 0) {
                    break;
                }
                try {
                    list.add(array.getJSONObject(i));
                } catch (JSONException e) {
                    Log.e(TAG, "Error loading data to json", e);

                }
            }
        }
        inboxAdapter.notifyDataSetChanged();
        if (binding != null && parameter == 2) {
            binding.swipeContainer.setRefreshing(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupDrawer(R.menu.menu_inbox);
        }
    }

    public void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Search the inbox using the query and vector space model
                searchView.clearFocus();
                queryInbox(1, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Reset the elements of the inbox
                queryInbox(0, null);
                return false;
            }
        });
    }

    public void archiveMessage(JSONObject message, int position) {
        Context context = getContext();
        View parentView = binding.rl;
        // Get the inbox of the current user
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.include(Inbox.KEY);
        userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error loading inbox data", e);
                    return;
                }
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                int updatedPosition = -1;
                try {
                    updatedPosition = Constant.indexOfMessage(message, jsonInbox);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
                if (updatedPosition == -1) {
                    Toasty.error(context, "Error finding message", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                try {
                    message.put("archived", true);
                    jsonInbox.put(updatedPosition, message);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
                inbox.setMessages(jsonInbox);
                inbox.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toasty.error(context, "Error archiving message", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        if (allMessages) {
                            list.add(position, message);
                            inboxAdapter.notifyItemInserted(position);
                        } else {
                            Snackbar.make(parentView, R.string.archived, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, undoArchive(message, position))
                                    .show();
                        }

                    }
                });
            }
        });
    }

    public View.OnClickListener archive(JSONObject message, int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                archiveMessage(message, position);
            }
        };
    }

    public View.OnClickListener undoArchive(JSONObject message, int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unArchive(message, position);

            }
        };
    }

    public void unArchive(JSONObject message, int position) {
        Context context = getContext();
        View parentView = binding.rl;
        // Get the inbox of the current user
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.include(Inbox.KEY);
        userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error loading inbox data", e);
                    return;
                }
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                int updatedPosition = -1;
                try {
                    updatedPosition = Constant.indexOfMessage(message, jsonInbox);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
                if (updatedPosition == -1) {
                    Toasty.error(context, "Error finding message", Toast.LENGTH_SHORT, true).show();
                    return;
                }
                try {
                    message.put("archived", false);
                    jsonInbox.put(updatedPosition, message);
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
                inbox.setMessages(jsonInbox);
                inbox.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toasty.error(context, "Error undoing archive message", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                        if (!allMessages) {
                            list.add(position, message);
                            inboxAdapter.notifyItemInserted(position);
                        } else {
                            Snackbar.make(parentView, R.string.not_archived, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, archive(message, position))
                                    .show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void scrollToTop() {
        if (binding != null) {
            binding.rvInbox.smoothScrollToPosition(0);
        }
    }

}