package com.example.letsgogolfing;

import static com.example.letsgogolfing.utils.Formatters.decimalFormat;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Filter;
import android.widget.TextView;
import com.example.letsgogolfing.FilterDialogFragment.FilterType;
import java.util.Date;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;

public class ItemAdapter extends ArrayAdapter<Item>{

    private Context context;
    public FilterType currentFilterType;
    private List<Item> originalItems;
    private List<Item> filteredItems;
    private LayoutInflater inflater;

    private ItemFilter itemFilter;
    private boolean isSelectModeEnabled = false;
    private Set<Integer> selectedItems = new HashSet<>();

    long startDate;
    long endDate;
    public Set<Integer> getSelectedPositions() {
        return new HashSet<>(selectedItems);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    // generate javadocs

    /**
     * Constructor for the ItemAdapter class.
     *
     * @param context The context of the activity that is using the adapter.
     * @param items   The list of items to be displayed.
     */
    public ItemAdapter(Context context, List<Item> items) {
        super(context, R.layout.grid_item, items);
        this.context = context;
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    // generate javadocs for setSelectModeEnabled

    /**
     * Sets whether or not the adapter should be in select mode.
     *
     * @param enabled True if the adapter should be in select mode, false otherwise.
     */
    public void setSelectModeEnabled(boolean enabled) {
        isSelectModeEnabled = enabled;
        selectedItems.clear(); // Clear selections when toggling mode
        notifyDataSetChanged();
    }
    public boolean isSelectionEmpty(){
        return selectedItems.isEmpty();
    }

    public void setFilterField(FilterType filterType) {
        this.currentFilterType = filterType;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = originalItems;
                    results.count = originalItems.size();
                } else {
                    String searchStr = constraint.toString().toLowerCase();
                    List<Item> matchValues = new ArrayList<>();

                    for (Item item : originalItems) {
                        if (item.matchesCriteria(searchStr, currentFilterType)) {
                            matchValues.add(item);
                        }
                    }

                    results.values = matchValues;
                    results.count = matchValues.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredItems = (ArrayList<Item>) results.values;
                notifyDataSetChanged();
            }
        };
    }
    public void setDateFilterRange(long startDate, long endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        Log.d("Filter", "Date range set: Start = " + new Date(startDate) + ", End = " + new Date(endDate));
    }
    // generate javadocs for toggleSelection
    /**
     * Toggles the selection of the item at the given position.
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

    public void setCurrentFilterType(FilterType filterType) {
        this.currentFilterType = filterType;
    }




    // get all items
    /**
     * Retrieves the list of items.
     *
     * @return The list of items.
     */
    public List<Item> getItems() {
        return filteredItems;
    }

    /**
     * Get count of items.
     *
     */
    @Override
    public int getCount() {
        return filteredItems.size();
    }

    /**
     * Get item at position.
     *
     * @param position The position of the item to retrieve.
     */
    @Override
    public Item getItem(int position) {
        return filteredItems.get(position);
    }

    /**
     * Get item id at position.
     *
     * @param position The position of the item to retrieve the id of.
     */
    @Override
    public long getItemId(int position) {
        return position; // Assuming items don't have unique IDs
    }

    /**
     * Get view at position.
     * @param position
     * @param convertView
     * @param parent
     * @return
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
            holder.imageView = convertView.findViewById(R.id.itemImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        holder.nameTextView.setText(item.getName());
        holder.descriptionTextView.setText(item.getDescription());

        holder.valueTextView.setText(context.getString(R.string.item_value, decimalFormat.format(item.getEstimatedValue())));

        // Set other properties to the holder's views as needed

        if (item.getImageUris() != null && !item.getImageUris().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUris().get(0)) // Load the first image URI
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.default_image); // Set a default image
        }

        // Change background color if selected
        if (selectedItems.contains(position)) {
            convertView.setBackgroundColor(Color.parseColor("#5E716A")); // color for selected items
        } else {
            convertView.setBackgroundColor(Color.parseColor("#88CEB4")); // Original background color
        }

        return convertView;
    }


    /**
     * Update items.
     *
     * @param newItems The new list of items to update the adapter with.
     */
    public void updateItems(List<Item> newItems) {
        this.originalItems.clear();
        this.filteredItems.clear();
        this.originalItems.addAll(newItems);
        this.filteredItems.addAll(newItems);
        notifyDataSetChanged();
    }


    /**
     * Remove item at position.
     *
     * @param position The position of the item to remove.
     */
    public void removeItem(int position) {
        if (position >= 0 && position < filteredItems.size()) {
            filteredItems.remove(position);
        }
    }

    public ArrayList<Item> getSelectedItems(){
        ArrayList<Item> itemList = new ArrayList<>();
        for(Integer i : selectedItems)
            itemList.add(getItem(i));
        return itemList;
    }

    // ViewHolder pattern to optimize performance
    private static class ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView valueTextView;
        ImageView imageView;
        // Add more views as needed
    }

    @Override
    public void clear() {
        originalItems.clear();
        filteredItems.clear();
        super.notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends Item> collection) {
        if (collection != null) {
            originalItems.addAll(collection);
            filteredItems.addAll(collection);
        }
        super.notifyDataSetChanged();
    }

    public void clearFilter() {
        filteredItems.clear();
        filteredItems.addAll(originalItems);
        notifyDataSetChanged();
    }


}
