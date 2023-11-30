package com.example.letsgogolfing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28})
public class AddItemActivityTest {

    private AddItemActivity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(AddItemActivity.class)
                .create()
                .resume()
                .get();
    }

    @Test
    public void isValidDate_correctDate_ReturnsTrue() {
        assertTrue(activity.isValidDate("2021-12-31"));
    }

    @Test
    public void isValidDate_incorrectDate_ReturnsFalse() {
        assertFalse(activity.isValidDate("2021-13-01")); // Invalid month
    }

    @Test
    public void isValidDate_emptyDate_ReturnsFalse() {
        assertFalse(activity.isValidDate("")); // Empty String
    }

    @Test
    public void isValidDate_leapYear_ReturnsTrue() {
        assertTrue(activity.isValidDate("2020-02-29")); // Leap year
    }

    @Test
    public void isValidDate_nonLeapYear_ReturnsFalse() {
        assertFalse(activity.isValidDate("2019-02-29")); // Non-leap year
    }

    // Add more tests as needed
    // Example: Test saving an item, handling camera intents, etc.
}
