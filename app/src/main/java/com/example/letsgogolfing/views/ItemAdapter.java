package com.example.letsgogolfing.views;

import static com.example.letsgogolfing.utils.Formatters.dateFormat;

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
import com.example.letsgogolfing.controllers.dialogs.FilterDialogFragment.FilterType;
import com.bumptech.glide.Glide;
import com.example.letsgogolfing.utils.ItemComparator;

import com.example.letsgogolfing.R;
import com.example.letsgogolfing.models.Item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;

/**
 * Adapter for the GridView in the MainActivity.
 */
public class ItemAdapter extends ArrayAdapter<Item>{

    private Context context;
    private FilterType currentFilterType = FilterType.BY_NAME;
    private String currentSortOption = "name";
    private boolean currentSortDirection = true;
    private CharSequence currentFilterConstraint = "";

    private List<Item> originalItems;
    private List<Item> filteredItems;
    private LayoutInflater inflater;
    private boolean isSelectModeEnabled = false;
    private Set<Integer> selectedItems = new HashSet<>();
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    currentFilterConstraint = constraint;
                    results.values = originalItems;
                    results.count = originalItems.size();
                } else {
                    currentFilterConstraint = constraint;
                    List<Item> filteredList = new ArrayList<>();
                    String filterString = constraint.toString().toLowerCase();

                    for (Item item : originalItems) {
                        if (item.matchesCriteria(filterString, currentFilterType)) {
                            filteredList.add(item);
                        }
                    }

                    results.values = filteredList;
                    results.count = filteredList.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                try {
                    filteredItems = new ArrayList<>((List<Item>) results.values);
                } catch (Exception e) {
                    Log.w("ItemAdapter", "Failed to apply results of filter to array", e);
                }
                sortItems();
                notifyDataSetChanged();
            }
        };
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

    public void setFilterType(FilterType filterType) {
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
            holder.makeTextView = convertView.findViewById(R.id.itemMake);
            holder.dateTextView = convertView.findViewById(R.id.itemDate);
            holder.tagsTextView = convertView.findViewById(R.id.itemTags); // Initialize the tags TextView
            holder.imageView = convertView.findViewById(R.id.itemImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = getItem(position);
        holder.nameTextView.setText(item.getName());
        holder.descriptionTextView.setText(item.getDescription());
        holder.makeTextView.setText(item.getMake());
        holder.valueTextView.setText(context.getString(R.string.item_price, (item.getEstimatedValue())));
        holder.dateTextView.setText(dateFormat.format(item.getDateOfPurchase()));

        // Format and set the tags
        StringBuilder tagsStringBuilder = new StringBuilder();
        for(String tag : item.getTags()) {
            tagsStringBuilder.append("#").append(tag).append(" ");
        }
        holder.tagsTextView.setText(tagsStringBuilder.toString().trim());

        // Set the image using Glide
        if (item.getImageUris() != null && !item.getImageUris().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUris().get(0)) // Load the first image URI
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.default_image); // Set a default image
        }

        // Change background color if selected
        if (selectedItems.contains(position)) {
            convertView.setBackgroundColor(Color.parseColor("#2D4B41")); // Color for selected items
        } else {
            convertView.setBackgroundColor(Color.parseColor("#5a786e")); // Original background color
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



    private static class ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView valueTextView;
        TextView tagsTextView;
        ImageView imageView;
        TextView makeTextView;
        TextView dateTextView;
    }


    /**
     * Clears all data from the adapter. This includes clearing both the original and filtered item lists.
     * After clearing the data, it notifies any registered observers that the data set has changed.
     */
    @Override
    public void clear() {
        originalItems.clear();
        filteredItems.clear();
        super.notifyDataSetChanged();
    }

    /**
     * Adds all of the items in the specified collection to the adapter.
     * This operation adds items to both the original and filtered item lists.
     * After adding the items, it notifies any registered observers that the data set has changed.
     *
     * @param collection The collection of items to be added. If this is null, the operation is skipped.
     */
    @Override
    public void addAll(Collection<? extends Item> collection) {
        if (collection != null) {
            originalItems.addAll(collection);
            filteredItems.addAll(collection);
        }
        super.notifyDataSetChanged();
    }

    /**
     * Clears the current filter applied to the adapter. This resets the filtered items list to the original items list.
     * It also resets the current filter constraint and filter type to their default states.
     * After clearing the filter, it applies the existing sorting criteria.
     */
    public void clearFilter() {
        filteredItems = new ArrayList<>(originalItems);
        currentFilterConstraint = "";
        currentFilterType = FilterType.BY_NAME;
        sortItems();
    }

    /**
     * Sorts the filtered items list using the current sorting criteria.
     * The sorting is based on the current sort option and direction.
     * After sorting the items, it notifies any registered observers that the data set has changed.
     */
    private void sortItems() {
        ItemComparator comparator = new ItemComparator(currentSortOption, currentSortDirection);
        filteredItems.sort(comparator);
        notifyDataSetChanged();
    }

    /**
     * Sets the sorting criteria for the adapter and applies the sort immediately.
     * This updates the current sort option and direction, and then sorts the items using these criteria.
     *
     * @param sortOption     The sort option to use (e.g., 'name', 'date').
     * @param sortDirection  The direction of the sort; true for ascending, false for descending.
     */
    public void setSortCriteria(String sortOption, boolean sortDirection) {
        this.currentSortOption = sortOption;
        this.currentSortDirection = sortDirection;
        sortItems();
    }

    /**
     * Reapplies the current filter and sorting criteria.
     * This method first applies the current filter constraint and then applies the sorting.
     * After reapplying the filter and sort, it notifies any registered observers that the data set has changed.
     */
    public void reapplyFilterAndSort() {
        getFilter().filter(currentFilterConstraint);
        sortItems();
        notifyDataSetChanged();
    }
}
