package com.example.letsgogolfing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import android.content.Context;

@RunWith(MockitoJUnitRunner.class)
public class ItemAdapterTest {

    @Mock
    private Context mockContext;

    private ItemAdapter itemAdapter;
    private List<Item> mockItemList;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockItemList = new ArrayList<>();
        mockItemList.add(new Item("Laptop", "High-performance laptop for development",
                new Date(),"Dell", "XPS 15", "12345", 1500.00, "Bought for development",
                Arrays.asList("Electronics", "Personal", "Laptop")));
        mockItemList.add(new Item("Smartphone", "Flagship smartphone with great camera",
                new Date(), "Samsung", "Galaxy S21", "67890", 1000.00, "Upgraded my phone",
                Arrays.asList("Electronics", "Personal", "Mobile")));
        mockItemList.add(new Item("Camera", "Professional DSLR camera for photography",
                new Date(), "Canon", "EOS 5D Mark IV", "ABCDE", 2500.00, "Photography equipment",
                Arrays.asList("Electronics", "Hobbies", "Photography")));


        itemAdapter = new ItemAdapter(mockContext, mockItemList);
    }

    @Test
    public void testGetCount() {
        assertEquals(mockItemList.size(), itemAdapter.getCount());
    }

    @Test
    public void testGetItem() {
        assertEquals(mockItemList.get(0), itemAdapter.getItem(0));
    }

    @Test
    public void testGetItemId() {
        assertEquals(0, itemAdapter.getItemId(0));
    }

    /*  Need Mock UI adapter to test subsequent methods
    @Test
    public void testToggleSelection() {
        assertFalse(itemAdapter.getSelectedPositions().contains(0));

        itemAdapter.toggleSelection(0);

        assertTrue(itemAdapter.getSelectedPositions().contains(0));
    }

    @Test
    public void testClearSelection() {
        itemAdapter.toggleSelection(0);
        itemAdapter.toggleSelection(1);

        assertFalse(itemAdapter.getSelectedPositions().isEmpty());

        itemAdapter.clearSelection();

        assertTrue(itemAdapter.getSelectedPositions().isEmpty());
    }

    @Test
    public void testUpdateItems() {
        List<Item> newItems = new ArrayList<>();
        newItems.add(new Item("Monitor", "High-resolution monitor for productivity",
                new Date(), "Dell", "U2720Q", "FGHIJ", 500.00, "Bought for work",
                Arrays.asList("Electronics", "Office", "Monitor")));

        itemAdapter.updateItems(newItems);

        assertEquals(newItems, itemAdapter.getItems());
    }
*/

}