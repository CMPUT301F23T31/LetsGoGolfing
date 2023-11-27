package com.example.letsgogolfing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
public class AddItemActivityTest {

    @Test
    public void isValidDate_correctDate_ReturnsTrue() {
        AddItemActivity activity = new AddItemActivity();
        assertTrue(activity.isValidDate("2021-12-31"));
    }

    @Test
    public void isValidDate_incorrectDate_ReturnsFalse() {
        AddItemActivity activity = new AddItemActivity();
        assertFalse(activity.isValidDate("2021-13-01")); // Invalid month
    }

    @Test
    public void isValidDate_emptyDate_ReturnsFalse() {
        AddItemActivity activity = new AddItemActivity();
        assertFalse(activity.isValidDate("")); // Empty String
    }
}
