package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.decimalFormat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.text.DecimalFormat;

import java.util.HashSet;

import java.util.List;
import java.util.Set;

/**
 * Custom adapter for displaying and managing a list of items in a GridView.
 */
public class ItemAdapter extends BaseAdapter {

    private Context context;
    private List<Item> items;
    private LayoutInflater inflater;

    private boolean isSelectModeEnabled = false;
    private Set<Integer> selectedItems = new HashSet<>();

    /**
     * Retrieves the positions of the selected items.
     *
     * @return A set containing the positions of selected items.
     */
    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedItems);
    }

    /**
     * Clears the selection and notifies the adapter to update the view.
     */
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Constructs an ItemAdapter with the given context and list of items.
     *
     * @param context The context of the calling activity.
     * @param items   The list of items to be displayed.
     */
    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Sets the selection mode for the adapter.
     *
     * @param enabled True if selection mode should be enabled, false otherwise.
     */
    public void setSelectModeEnabled(boolean enabled) {
        isSelectModeEnabled = enabled;
        selectedItems.clear(); // Clear selections when toggling mode
        notifyDataSetChanged();
    }

    /**
     * Toggles the selection state of an item at the specified position.
     *
     * @param position The position of the item to toggle.
     */
    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyDataSetChanged();
    }

    /**
     * Retrieves the list of items
     *
     * @return A list of items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Retrieves the total number of items in the list.
     *
     * @return The total number of items in the list.
     */
    @Override
    public int getCount() {
        return items.size();
    }

    /**
     * Retrieves the item at the specified position.
     *
     * @param position The position of the item to retrieve.
     * @return The item at the specified position.
     */
    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    /**
     * Retrieves the ID of the item at the specified position.
     *
     * @param position The position of the item to retrieve.
     * @return The ID of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position; // Assuming items don't have unique IDs
    }

    /**
     * Retrieves the view for the item at the specified position.
     *
     * @param position    The position of the item to retrieve.
     * @param convertView The view to be converted.
     * @param parent      The parent view.
     * @return The view for the item at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.nameTextView = convertView.findViewById(R.id.itemName);
            holder.descriptionTextView = convertView.findViewById(R.id.itemDescription);
            holder.valueTextView = convertView.findViewById(R.id.itemValue);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        holder.nameTextView.setText(item.getName());
        holder.descriptionTextView.setText(item.getDescription());

        holder.valueTextView.setText(context.getString(R.string.item_value, decimalFormat.format(item.getEstimatedValue())));

        // Set other properties to the holder's views as needed

        // Change background color if selected
        if (selectedItems.contains(position)) {
            convertView.setBackgroundColor(Color.parseColor("#5E716A")); // color for selected items
        } else {
            convertView.setBackgroundColor(Color.parseColor("#88CEB4")); // Original background color
        }


        return convertView;
    }


    /**
     * Updates the list of items and notifies the adapter to update the view.
     *
     * @param newItems The new list of items.
     */
    public void updateItems(List<Item> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Removes an item at the specified position from the list and notifies the adapter to update the view.
     *
     * @param position The position of the item to be removed.
     */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
        }
    }

    /**
     * ViewHolder pattern to optimize performance by caching references to views for efficient reuse.
     * This class holds references to views related to an individual item in the ItemAdapter's GridView.
     */
    private static class ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView valueTextView;
        // Add more views as needed
    }
}



