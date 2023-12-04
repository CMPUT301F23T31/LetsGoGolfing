package com.example.letsgogolfing;

import android.widget.Filter;

import com.example.letsgogolfing.ItemAdapter;
import com.example.letsgogolfing.Item;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter for the GridView in the MainActivity.
 */
public class ItemFilter extends Filter {
    private final ItemAdapter adapter;
    private final List<Item> originalList;
    private final List<Item> filteredList;

    /**
     * Constructor for the ItemFilter class.
     *
     * @param adapter The adapter for the GridView.
     * @param originalList The list of items to be filtered.
     */
    public ItemFilter(ItemAdapter adapter, List<Item> originalList) {
        this.adapter = adapter;
        this.originalList = new ArrayList<>(originalList);
        this.filteredList = new ArrayList<>();
    }

    /**
     * Filters the list of items based on the specified constraint.
     *
     * @param constraint The constraint to filter by.
     * @return The filtered list of items.
     */
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        filteredList.clear();
        final FilterResults results = new FilterResults();

        if (constraint == null || constraint.length() == 0) {
            filteredList.addAll(originalList);
        } else {
            final String filterPattern = constraint.toString().toLowerCase().trim();

            for (final Item item : originalList) {
                switch (adapter.currentFilterType) {
                    case BY_DESCRIPTOR:
                        if (item.getDescription().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                        break;
                    case BY_TAGS:
                        // Add logic to filter by tags
                    case BY_MAKE:
                        // Add logic to filter by make
                        break;
                    case BY_DATE:
                        // Add logic to filter by date
                        break;
                }
            }
        }

        results.values = filteredList;
        results.count = filteredList.size();
        return results;
    }



    /**
     * Publishes the filtered results onto the adapter.
     *
     * @param constraint The constraint used to filter the results.
     * @param results The results of the filtering operation.
     */
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.clear();
        adapter.addAll((ArrayList<Item>) results.values);
        adapter.notifyDataSetChanged();
    }
}

