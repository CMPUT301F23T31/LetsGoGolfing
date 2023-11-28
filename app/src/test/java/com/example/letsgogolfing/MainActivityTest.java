package com.example.letsgogolfing;

import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = Robolectric.setupActivity(MainActivity.class);
        // Initialize mocks here if needed
    }

    @Test
    public void testUpdateTotalValue() {
        // Arrange
        List<Item> items = Arrays.asList(
                new Item("Item 1", 100.0),
                new Item("Item 2", 200.0)
        );

        // Act
        mainActivity.updateTotalValue(items);

        // Assert
        TextView totalValueTextView = mainActivity.findViewById(R.id.totalValue);
        String expectedText = "Total Value: $300.00"; // Adjust according to your formatting
        assertEquals(expectedText, totalValueTextView.getText().toString());
    }

    // Additional tests go here

}

