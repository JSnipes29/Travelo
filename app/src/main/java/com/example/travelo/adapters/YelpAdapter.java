package com.example.travelo.adapters;

import android.content.Context;
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

import java.util.List;

public class YelpAdapter extends RecyclerView.Adapter<YelpAdapter.ViewHolder> {

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
        holder.bind(business);
    }

    @Override
    public int getItemCount() {
        return businesses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final ImageView ivImage;
        private final RatingBar ratingBar;
        private final TextView tvNumReviews;
        private final TextView tvUrl;
        private final Button btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBusinessName);
            ivImage = itemView.findViewById(R.id.ivBusinessImage);
            ratingBar = itemView.findViewById(R.id.rbRatings);
            tvNumReviews = itemView.findViewById(R.id.tvNumReviews);
            tvUrl = itemView.findViewById(R.id.tvBusinessUrl);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }

        public void bind(final YelpBusinesses business) {
            tvName.setText(business.getName());
            tvNumReviews.setText(String.valueOf(business.getReviewCount()));
            //tvUrl.setText(business.getUrl());
            ratingBar.setRating((float)business.getRating());
            String imageUrl = business.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context).load(imageUrl)
                        .centerCrop()
                        .transform(new RoundedCorners(32))
                        .into(ivImage);
            }
            if (business.getAdded()) {
                btnAdd.setText(R.string.remove);
                TypedValue typedValue = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
                btnAdd.setBackgroundColor(typedValue.data);
                context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                btnAdd.setTextColor(typedValue.data);
            }
            btnAdd.setOnClickListener(v -> {
                if(business.getAdded()) {
                    btnAdd.setText(R.string.add);
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                    btnAdd.setBackgroundColor(typedValue.data);
                    context.getTheme().resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
                    btnAdd.setTextColor(typedValue.data);
                    business.setAdded(false);
                } else {
                    btnAdd.setText(R.string.remove);
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
                    btnAdd.setBackgroundColor(typedValue.data);
                    context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
                    btnAdd.setTextColor(typedValue.data);
                    business.setAdded(true);

                }
            });
        }
    }
}
