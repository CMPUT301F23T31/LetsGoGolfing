package com.example.letsgogolfing.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.letsgogolfing.R;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    private List<String> tagsList;
    private Context context;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onEditClick(int position, String editedTag);
        void onDeleteClick(int position);
    }

    public TagAdapter(Context context, List<String> tagsList) {
        this.context = context;
        this.tagsList = tagsList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String tag = tagsList.get(position);
        holder.tagTextView.setText(tag);

        // Handle editing or removing a tag
        holder.editButton.setOnClickListener(v -> {
            editTag(position, holder);
        });

        holder.deleteButton.setOnClickListener(v -> {
            itemClickListener.onDeleteClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return tagsList.size();
    }

    public void addTag(String tag) {
        tagsList.add(tag);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tagTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagTextView = itemView.findViewById(R.id.tagTextView);
            editButton = itemView.findViewById(R.id.editTagButton);
            deleteButton = itemView.findViewById(R.id.deleteTagButton);
        }
    }

    private void editTag(int position, ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Tag");

        // Create an EditText view to allow tag editing
        final EditText input = new EditText(context);
        input.setText(tagsList.get(position)); // Set the initial text
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String editedTag = input.getText().toString();
            tagsList.set(position, editedTag);
            notifyItemChanged(position); // Notify the adapter that the item has changed
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel(); // Cancel the editing process
        });

        builder.show();
    }
    private void removeTag(int position) {
        tagsList.remove(position);
        notifyItemRemoved(position);
    }
}








