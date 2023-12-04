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


/**
 * Adapter for the RecyclerView in the AddItemActivity.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Uri> imageUris;
    private Context context;

    public ImageAdapter(List<Uri> imageUris, Context context) {
        this.context = context;
    }

    public void setImageUris(List<Uri> imageUris) {
        this.imageUris = imageUris;
    }

    /**
     * Creates a new ImageViewHolder for the given item type.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ImageViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ImageAdapter", "Creating view holder for item type " + viewType);
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds the data at the specified position to the corresponding view holder.
     *
     * @param holder   The view holder to bind.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        Log.d("ImageAdapter", "Binding view holder for item " + position + ": " + imageUri.toString());
        Glide.with(context).load(imageUri).into(holder.imageView);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return imageUris.size();
    }


    /**
     * Interface for the callback to be invoked when an image is deleted.
     */
    public interface OnImageDeleteListener {
        void onDeleteImage(Uri imageUri);
    }

    /**
     * Listener for the callback to be invoked when an image is deleted.
     */
    private OnImageDeleteListener deleteListener;

    /**
     * Sets the listener for the callback to be invoked when an image is deleted.
     *
     * @param listener The listener to be invoked when an image is deleted.
     */
    public void setOnImageDeleteListener(OnImageDeleteListener listener) {
        this.deleteListener = listener;
    }

    /**
     * ViewHolder for the ImageAdapter.
     */
    public class ImageViewHolder extends RecyclerView.ViewHolder {
        // Other views
        ImageView imageView;
        ImageButton deleteButton;

        /**
         * Constructor for the ImageViewHolder.
         *
         * @param itemView The view that the ImageViewHolder will hold.
         */
        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            deleteButton = itemView.findViewById(R.id.close_button);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Uri imageUri = imageUris.get(position);
                    if (deleteListener != null) {
                        deleteListener.onDeleteImage(imageUri);
                    }
                }
            });
        }
    }
}