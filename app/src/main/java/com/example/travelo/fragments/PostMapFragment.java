package com.example.travelo.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.travelo.activities.MainActivity;
import com.example.travelo.R;
import com.example.travelo.adapters.CustomWindowAdapter;
import com.example.travelo.constants.Constant;
import com.example.travelo.databinding.FragmentPostMapBinding;
import com.example.travelo.models.MarkerTag;
import com.example.travelo.models.Post;
import com.example.travelo.models.Room;
import com.example.travelo.models.YelpBusinesses;
import com.example.travelo.models.YelpLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


public class PostMapFragment extends Fragment {


    public static final String TAG = "PostMapFragment";
    FragmentPostMapBinding binding;
    MapView mapView;
    GoogleMap map;
    Room room;
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 24;
    private ParseFile parsePhotoFile;
    private File photoFile;
    public static final String FILE_MAP_NAME = "map_screenshot.png";
    public String photoFileName = "map_photo.jpg";
    public static final boolean DELETE_ROOM = true;

    public PostMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostMapBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        room = (Room) Parcels.unwrap(getArguments().getParcelable("room"));
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        if (mapView != null) {
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    map.setInfoWindowAdapter(new CustomWindowAdapter(getLayoutInflater(), getContext()));
                    populateMap();
                }
            });
        } else {
            Toasty.error(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT, true).show();
        }
        // If the user isn't the owner, they can't add a description or photo
        // and proceeding just goes to main activity
        if (!ParseUser.getCurrentUser().getObjectId().equals(room.getOwner().getObjectId())) {
            binding.etDescription.setVisibility(View.GONE);
            binding.btnAddPhoto.setVisibility(View.GONE);
            binding.btnProceed.setText(R.string.done);
            binding.btnProceed.setOnClickListener(v -> {
                getActivity().finish();
            });
        } else {
            // Else the user can add a description and post to server
            binding.btnAddPhoto.setOnClickListener(v -> launchCamera());
            binding.btnProceed.setOnClickListener(v -> {
                if (binding != null) {
                    binding.btnProceed.setClickable(false);
                }
                // If there is no photo, take a screenshot of the map and post it as the photo
                if (photoFile == null) {
                    map.snapshot(new GoogleMap.SnapshotReadyCallback() {
                        @Override
                        public void onSnapshotReady(@Nullable Bitmap bitmap) {
                            parsePhotoFile = bitmapToFile(bitmap);
                            postMap();
                        }
                    });
                } else {
                    postMap();
                }
            });
        }

        // Set up spinner to change map type
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (map != null) {
                    int type = 1;
                    switch (position) {
                        case 0:
                            type = GoogleMap.MAP_TYPE_NORMAL;
                            break;
                        case 1:
                            type = GoogleMap.MAP_TYPE_SATELLITE;
                            break;
                        case 2:
                            type = GoogleMap.MAP_TYPE_TERRAIN;
                            break;
                        case 3:
                            type = GoogleMap.MAP_TYPE_HYBRID;
                            break;
                        default:
                            break;
                    }
                    map.setMapType(type);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    public void postMap() {
        String description = binding.etDescription.getText().toString();
        Post post;
        if (parsePhotoFile != null) {
            post = Post.createPost(room.getMap(), description, room.getProfileImages(), parsePhotoFile);
        } else {
            post = Post.createPost(room.getMap(), description, room.getProfileImages(), photoFile);
        }

        post.saveInBackground(e -> {
            if (e == null) {
                Toasty.info(getContext(), "Successfully posted map", Toast.LENGTH_SHORT, true).show();
                // Delete the room after posting the map
                if (DELETE_ROOM) {
                    room.deleteInBackground(e1 -> {
                        if (e1 != null) {
                            Log.e(TAG, "Error deleting room", e1);
                            return;
                        } else {
                            Log.i(TAG, "Room has been deleted");
                        }
                        getActivity().finish();
                    });
                }
            } else {
                Log.e(TAG, "Error posting map", e);
                getActivity().finish();
            }
        });

    }
    // Get the marker data from the Parse server and add it to the map
    public void populateMap() {
        JSONObject jsonMap = room.getMap();
        List<LatLng> markerLocations = new ArrayList<>();
        try {
            JSONArray markers = jsonMap.getJSONArray("markers");
            for (int i = 0; i < markers.length(); i++) {
                JSONObject jsonMarker = markers.getJSONObject(i);
                List<YelpBusinesses> businesses = new ArrayList<>();
                double latitude = jsonMarker.getDouble("latitude");
                double longitude = jsonMarker.getDouble("longitude");
                String color = jsonMarker.getString("color");
                String user = jsonMarker.getString("user");
                JSONArray places = jsonMarker.getJSONArray("places");
                for (int j = 0; j < places.length(); j++) {
                    JSONObject place = places.getJSONObject(j);
                    String name = place.getString("name");
                    double rating = place.getDouble("rating");
                    int numRatings = place.getInt("num_ratings");
                    String imageUrl = place.getString("image_url");
                    String price = place.getString("price");
                    double distanceMeters = place.getDouble("distance");
                    String category = place.getString("category");
                    String address = place.getString("address");
                    String city = place.getString("city");
                    String state = place.getString("state");
                    String country = place.getString("country");
                    YelpLocation location = YelpLocation.makeLocation(address, city, state, country);
                    YelpBusinesses business = YelpBusinesses.makeBusiness(name, rating, numRatings, imageUrl, price, distanceMeters, location, category);
                    businesses.add(business);
                }
                // Define color of marker icon
                BitmapDescriptor defaultMarker = MarkerTag.colorMarker(color);
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .icon(defaultMarker));
                marker.setTag(businesses);
                marker.setSnippet(user);
                markerLocations.add(new LatLng(latitude, longitude));
            }
        } catch (JSONException e) {
            Log.e(TAG,"Error getting markers from server", e);
        }
        Constant.centerMap(getContext(), map, markerLocations);
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toasty.success(getContext(), "Map was loaded properly!", Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.error(getContext(), "Error - Map was null!!", Toast.LENGTH_SHORT, true).show();
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
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.Travelo", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
        // Start the image capture intent to take photo
        Log.i(TAG, "Launching camera intent");
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    // Returns the File for a photo stored on disk given the fileName
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                // Show message
                Toasty.success(getContext(), "Picture was taken", Toast.LENGTH_SHORT, true).show();
            } else { // Result was a failure
                Toasty.info(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT, true).show();
                photoFile = null;
            }
        }
    }

    public static ParseFile bitmapToFile(Bitmap bitmap) {
        //create a file to write bitmap data
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile(FILE_MAP_NAME,imageByte);
        return parseFile;
    }
}