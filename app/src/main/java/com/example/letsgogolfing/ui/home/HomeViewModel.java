package com.example.letsgogolfing.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.letsgogolfing.model.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Date;

/**
 * This class manages the data displayed by HomeFragment
 */
public class HomeViewModel extends ViewModel {

    //private float totalEstimatedValue;
    private final MutableLiveData<List<Item>> items;

    /**
     * Initializes the displayed data with sample items
     */
    public HomeViewModel() {
        items = new MutableLiveData<>();

        List<Item> sampleItems = Arrays.asList(
            new Item("1",  "Laptop", new Date(), "Dell", "XPS 15", "123456", 1000.0, "My laptop", Arrays.asList("Electronics", "Office")),
            new Item("2",  "Phone", new Date(), "Apple", "iPhone 12", "789012", 800.0, "My phone", Arrays.asList("Electronics"))
        );

        items.setValue(sampleItems);
    }

    /**
     * Returns the List of Items
     * @return
     */
    public LiveData<List<Item>> getItems() {
        return items;
    }

    /**
     * Sets total estimated value of all the items
     */
    public float getTotalEstimatedValue() {
        int value = 0;
        for (Item i : items.getValue()) {
            //totalEstimatedValue += i.getEstimatedValue();
            value += i.getEstimatedValue();
        }
        return value;
    }

}