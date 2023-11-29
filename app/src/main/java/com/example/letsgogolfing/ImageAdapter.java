package com.example.letsgogolfing;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private List<Uri> imageUris;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;

    public ImageAdapter(List<Uri> imageUris, Context context, OnDeleteClickListener onDeleteClickListener) {
        this.imageUris = imageUris;
        this.context = context;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void setImageUris(List<Uri> imageUris) {
        this.imageUris = imageUris;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ImageAdapter", "Creating view holder for item type " + viewType);
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        Log.d("ImageAdapter", "Binding view holder for item " + position + ": " + imageUri.toString());
        Glide.with(context).load(imageUri).into(holder.imageView);

        holder.deleteButton.setOnClickListener(v -> {
            onDeleteClickListener.onDeleteClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}