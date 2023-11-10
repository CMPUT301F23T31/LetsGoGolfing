package com.example.letsgogolfing;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainPageTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testNavigationToAddTags() {
        // Navigate to the Manage Tags page
        onView(withId(R.id.manage_tags_button)).perform(click());
        onView(withId(R.id.activity_manage_tags)).check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testNavigationToAddItem() {
        // Navigate to the Add Item page
        onView(withId(R.id.addItemButton)).perform(click());
        onView(withId(R.id.add_item_page)).check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testNavigationToItemDetails() {
        // Assuming you have a RecyclerView or similar view to click on items
        onView(withId(R.id.itemGrid)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.view_details)).check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testSelectAndDeleteItems() {
        // Enter select mode
        onView(withId(R.id.select_button)).perform(click());
        // Long click an item to select it
        onView(withId(R.id.itemGrid)).perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
        // Click delete button
        onView(withId(R.id.delete_button)).perform(click());
        // Check if total count is updated (assuming you have a method to get the count from the adapter)
        // onView(withId(R.id.totalValue)).check(matches(withText("Updated Count")));
    }

    @Test
    public void testProfileButtonNavigation() {
        // Navigate to the Profile page
        onView(withId(R.id.profileButton)).perform(click());
        onView(withId(R.id.profile_page)).check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testTotalCountDisplay() {
        // Check that the total count is displayed correctly
        // This assumes the TextView for total count has id totalValue
        onView(withId(R.id.totalValue)).check(matches(withText("Correct Total Count")));
    }
}
