package com.example.travelo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.travelo.R;
import com.example.travelo.models.YelpBusinesses;
import com.example.travelo.models.YelpCategory;
import com.example.travelo.models.YelpLocation;

import java.util.List;

public class YelpAdapter extends RecyclerView.Adapter<YelpAdapter.ViewHolder> {

    public static final String TAG = "YelpAdapter";

    private Context context;
    private List<YelpBusinesses> businesses;

    public YelpAdapter(Context context, List<YelpBusinesses> businesses) {
        this.context = context;
        this.businesses = businesses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_yelp_business, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YelpAdapter.ViewHolder holder, int position) {
        YelpBusinesses business = businesses.get(position);
        holder.bind(business, position);
    }

    @Override
    public int getItemCount() {
        return businesses.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final ImageView ivImage;
        private final RatingBar ratingBar;
        private final TextView tvNumReviews;
        private final Button btnAdd;
        private final TextView tvPrice;
        private final TextView tvDistance;
        private final TextView tvAddress;
        private final TextView tvCategory;
        private final TextView tvLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBusinessName);
            ivImage = itemView.findViewById(R.id.ivBusinessImage);
            ratingBar = itemView.findViewById(R.id.rbRatings);
            tvNumReviews = itemView.findViewById(R.id.tvNumReviews);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }

        public void bind(final YelpBusinesses business, final int position) {
            tvName.setText(business.getName());
            tvNumReviews.setText(String.valueOf(business.getReviewCount()));
            ratingBar.setRating((float)business.getRating());
            tvPrice.setText(business.getPrice());
            String distance = String.valueOf(YelpBusinesses.metersToMiles(business.getDistanceMeters())) + " mi";
            tvDistance.setText(distance);
            YelpLocation location = business.getLocation();
            tvAddress.setText(location.getAddress());
            tvLocation.setText(YelpLocation.formatAddress(location.getCity(), location.getState(), location.getCountry()));
            YelpCategory category = business.getCategories().get(0);
            tvCategory.setText(category.getTitle());
            String imageUrl = business.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context).load(imageUrl)
                        .centerCrop()
                        .transform(new RoundedCorners(32))
                        .into(ivImage);
            }

            if (!business.getButton()) {
                btnAdd.setClickable(false);
                btnAdd.setVisibility(View.GONE);
                return;
            }
            if (business.getAdded()) {
                btnAdd.setText(R.string.remove);
                btnAdd.setBackgroundColor(Color.BLACK);
                btnAdd.setTextColor(Color.WHITE);
            } else {
                btnAdd.setText(R.string.add);
                btnAdd.setBackgroundColor(Color.WHITE);
                btnAdd.setTextColor(Color.BLACK);
            }
            btnAdd.setOnClickListener(v -> {
                business.setAdded(!business.getAdded());
                Log.i(TAG, "Item changed: " + position);
                notifyItemChanged(position);
            });
        }
    }
}
