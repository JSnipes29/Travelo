package com.example.travelo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
    Context context;
    JSONArray comments;

    public CommentAdapter(Context c, JSONArray arr) {
        this.context = c;
        this.comments = arr;

    }

    public void update(JSONArray arr) {
        this.comments = arr;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {

        try {
            JSONObject comment = comments.getJSONObject(position);
            holder.bind(comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return comments.length();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvComment;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvComment = itemView.findViewById(R.id.tvComment);
        }

        public void bind(JSONObject comment) throws JSONException {
            tvName.setText(comment.getString("username"));
            tvComment.setText(comment.getString("comment"));
        }
    }
}
