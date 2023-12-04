package com.example.letsgogolfing.utils;

import android.util.Log;
import android.widget.Filter;

import com.example.letsgogolfing.models.Item;
import com.example.letsgogolfing.views.ItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

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
                        // Logic to filter by tags
                        for (String tag : item.getTags()) {
                            if (tag.toLowerCase().contains(filterPattern)) {
                                filteredList.add(item);
                                break; // Break to avoid adding the same item multiple times
                            }
                        }
                        break;
                    case BY_MAKE:
                        // Add logic to filter by make
                        if (item.getMake() != null && item.getMake().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                        break;
                    case BY_DATE:
                        if (item.getDateOfPurchase() != null) {
                            long itemDateMillis = item.getDateOfPurchase().getTime();
                            Log.d("Filter", "Item Date: " + item.getDateOfPurchase() + ", Filter Start: " + new Date(adapter.startDate) + ", Filter End: " + new Date(adapter.endDate));
                            if (itemDateMillis >= adapter.startDate && itemDateMillis < adapter.endDate) {
                                filteredList.add(item);
                            }
                        }
                        break;
                }
            }
        }

        if (filteredList.isEmpty()) {
            results.values = new ArrayList<Item>(); // return an empty list instead of null
        } else {
            results.values = filteredList;
        }
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
        if (results.values != null) {
            adapter.addAll((ArrayList<Item>) results.values);
        }
        adapter.notifyDataSetChanged();
    }
}

