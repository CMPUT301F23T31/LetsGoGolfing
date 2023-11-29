package com.example.letsgogolfing;

import java.util.Comparator;
public class ItemComparator implements Comparator<Item>{
    private String sortField;
    private boolean ascending;

    public ItemComparator(String sortField, boolean ascending) {
        this.sortField = sortField;
        this.ascending = ascending;
    }

    @Override
    public int compare(Item item1, Item item2) {
        int comparisonResult = 0;
        switch (sortField) {
            case "name":
                comparisonResult = item1.getName().compareTo(item2.getName());
                break;

            case "date":
                comparisonResult = item1.getDateOfPurchase().compareTo(item2.getDateOfPurchase());
                break;

            case "estimated value":
                comparisonResult = item1.getEstimatedValue().compareTo(item2.getEstimatedValue());
                break;

            case "make":
                comparisonResult = item1.getMake().compareTo(item2.getMake());
                break;

            case "description":
                comparisonResult = item1.getDescription().compareTo(item2.getDescription());
                break;
        }

        return ascending ? comparisonResult : -comparisonResult;

    }
}
