package com.example.letsgogolfing;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

@RunWith(AndroidJUnit4.class)
public class ViewDetailsUITest {

    // Replace with the actual ID of the item you want to test
    private static final String ITEM_ID_FOR_TEST = "testItemId";

    @Rule
    public ActivityScenarioRule<ViewDetailsActivity> activityRule =
            new ActivityScenarioRule<>(ViewDetailsActivity.class);

    @Test
    public void testEditItemDetails() {
        // Set up the activity with a test item
        Intent intent = new Intent();
        Item testItem = new Item(); // Populate this with test data
        testItem.setId(ITEM_ID_FOR_TEST);
        intent.putExtra("ITEM", testItem);
        activityRule.getScenario().onActivity(activity -> activity.setIntent(intent));

        // Click the edit button
        onView(withId(R.id.editInfoBtn)).perform(click());

        // Check if fields are enabled
        onView(withId(R.id.nameField)).check(matches(isEnabled()));
        onView(withId(R.id.descriptionField)).check(matches(isEnabled()));
        onView(withId(R.id.add_tags_button_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testAddTags() {
        // Click the add tags button
        onView(withId(R.id.add_tags_button_view)).perform(click());

        // Verify tag selection dialog is displayed
        // Note: You will need to find a way to reference the dialog's UI elements
        // This might involve setting IDs for the dialog's views or using other matchers
    }

    @Test
    public void testSaveItemDetails() {
        // Make changes to the item details

        // Click the save button
        onView(withId(R.id.saveBtn)).perform(click());

        // Verify updated details are displayed
        // Use matches to check the text in the views
    }

    @Test
    public void testCancelEdit() {
        // Make changes to the item details

        // Click the cancel button
        onView(withId(R.id.cancel_edit_button)).perform(click());

        // Verify that the original details are displayed
        // Use matches to check the text in the views
    }

    // Additional tests can be implemented following similar patterns for different parts of the activity
}
