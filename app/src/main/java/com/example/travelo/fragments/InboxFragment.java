package com.example.travelo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.travelo.R;
import com.example.travelo.adapters.InboxAdapter;
import com.example.travelo.databinding.FragmentInboxBinding;
import com.example.travelo.models.Inbox;
import com.example.travelo.search.Corpus;
import com.example.travelo.search.Document;
import com.example.travelo.search.VectorSpaceModel;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class InboxFragment extends Fragment {

    FragmentInboxBinding binding;
    InboxAdapter inboxAdapter;
    List<JSONObject> list;
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
        ((AppCompatActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        list = new ArrayList<>();
        inboxAdapter = new InboxAdapter(list, getContext());
        binding.rvInbox.setAdapter(inboxAdapter);
        LinearLayoutManager inboxLayoutManager = new LinearLayoutManager(getContext());
        binding.rvInbox.setLayoutManager(inboxLayoutManager);
        queryInbox(0, null);
        return view;
    }

    public void queryInbox(int parameter, String query) {
        ParseQuery<ParseUser> userQuery = ParseQuery.getQuery(ParseUser.class);
        userQuery.include(Inbox.KEY);
        userQuery.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser user, ParseException e) {
                Inbox inbox = (Inbox) user.getParseObject(Inbox.KEY);
                JSONArray jsonInbox = inbox.getMessages();
                jsonToList(jsonInbox, query, parameter);
            }
        });

    }

    public void jsonToList(JSONArray array, String query, int parameter) {
        list.clear();
        inboxAdapter.notifyDataSetChanged();
        if (query == null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    list.add(array.getJSONObject(i));
                } catch (JSONException e) {
                    Log.e(TAG, "Error loading data to json", e);

                }
            }
        } else {
            Map<Double, JSONObject> similarityText = new TreeMap<Double, JSONObject>();
            ArrayList<Document> documents = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject jsonMessage = array.getJSONObject(i);
                    String text = null;
                    if (jsonMessage.length() == InboxAdapter.ROOM_LENGTH) {
                        text = jsonMessage.getString(jsonMessage.keys().next());
                    } else {
                        text = jsonMessage.getString("username");
                    }
                    Document doc = new Document(text);
                    documents.add(doc);
                } catch (JSONException e) {
                    Log.e(TAG, "Error loading data to json", e);
                }
            }
            Document queryDoc = new Document(query);
            documents.add(queryDoc);
            double [] cosineSimiliarityValues = new double[documents.size() - 1];
            Corpus corpus = new Corpus(documents);
            VectorSpaceModel vectorSpaceModel = new VectorSpaceModel(corpus);
            for (int j = 0; j < cosineSimiliarityValues.length; j++) {
                Document doc = documents.get(j);
                double value = vectorSpaceModel.cosineSimilarity(doc, queryDoc);
                cosineSimiliarityValues[j] = value;
            }
            try {
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.inbox_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                ArrayList<Document> documents = new ArrayList<>();
                Corpus corpus = new Corpus(documents);
                VectorSpaceModel model = new VectorSpaceModel(corpus);
                queryInbox(1, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchClickListener(v -> binding.tvAppName.setVisibility(View.GONE));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                binding.tvAppName.setVisibility(View.VISIBLE);
                queryInbox(0, null);
                return false;
            }
        });
    }
}