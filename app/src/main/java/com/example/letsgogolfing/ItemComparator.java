package com.example.letsgogolfing;

import java.util.Comparator;
import java.util.List;

/**
 * A comparator for sorting instances of {@code Item} based on various fields.
 * This comparator supports sorting by different fields such as name, date of purchase,
 * estimated value, make, and description. It allows for both ascending and descending
 * sorting orders.
 */
public class ItemComparator implements Comparator<Item>{
    private String sortField;
    private boolean ascending;

    /**
     * Constructs a new {@code ItemComparator} with the specified sorting field and order.
     *
     * @param sortField The field of {@code Item} by which to sort.
     *                  Valid fields are "name", "date", "estimated value", "make", and "description".
     * @param ascending A boolean where {@code true} indicates ascending order,
     *                  and {@code false} indicates descending order.
     */
    public ItemComparator(String sortField, boolean ascending) {
        this.sortField = sortField;
        this.ascending = ascending;
    }

    /**
     * Compares two {@code Item} objects based on the specified sorting field and order.
     *
     * @param item1 The first {@code Item} to be compared.
     * @param item2 The second {@code Item} to be compared.
     * @return A negative integer, zero, or a positive integer as the first argument
     *         is less than, equal to, or greater than the second, in the context of the
     *         specified sorting preferences.
     */
    @Override
    public int compare(Item item1, Item item2) {
        int comparisonResult = 0;
        switch (sortField) {
            case "name":
                comparisonResult = item1.getName().toLowerCase().compareTo(item2.getName().toLowerCase());
                break;

            case "date":
                comparisonResult = item1.getDateOfPurchase().compareTo(item2.getDateOfPurchase());
                break;

            case "estimated value":
                comparisonResult = item1.getEstimatedValue().compareTo(item2.getEstimatedValue());
                break;

            case "make":
                comparisonResult = item1.getMake().toLowerCase().compareTo(item2.getMake().toLowerCase());
                break;

            case "description":
                comparisonResult = item1.getDescription().toLowerCase().compareTo(item2.getDescription().toLowerCase());
                break;

            case "tags":
                comparisonResult = compareTags(item1, item2);
        }
        return ascending ? comparisonResult : -comparisonResult;
    }

    /**
     * Compares the tags of two {@code Item} objects.
     *
     * @param item1 The first {@code Item} to be compared.
     * @param item2 The second {@code Item} to be compared.
     * @return A negative integer, zero, or a positive integer as the first argument
     *         is less than, equal to, or greater than the second, in the context of the
     *         specified sorting preferences.
     */
    public int compareTags(Item item1, Item item2) {
        List<String> tags1 = item1.getTags();
        List<String> tags2 = item2.getTags();

        if ((tags1 == null || tags1.isEmpty()) && (tags2 == null || tags2.isEmpty())) {
            return 0;
        } else if (tags1 == null || tags1.isEmpty()) {
            return 1;
        } else if (tags2 == null || tags2.isEmpty()) {
            return -1;
        } else {
            String firstTag1 = tags1.get(0).toLowerCase();
            String firstTag2 = tags2.get(0).toLowerCase();
            return firstTag1.compareTo(firstTag2);
        }

    }
}
