package com.example.letsgogolfing;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class TagsUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testAddingNewTagFromMain() {
        // Navigate to the part of the UI where add_tags_button is present
        // For example, if it's in a menu, you might need to open the menu first
        // onView(withId(R.id.menu)).perform(click());

        onView(withId(R.id.manage_tags_button)).perform(click());
        onView(withId(R.id.newTagEditText)).perform(typeText("kitchenware"), closeSoftKeyboard());
        onView(withId(R.id.addTagButton)).perform(click());
        // Additional checks for the toast message or updated UI
    }

    // This test needs to be in a separate test class or manage activity lifecycle within the method
    @Test
    public void testSelectingTagsInAddItemActivity() {
        // Implementation for testing in AddItemActivity
    }

    // This test needs to be in a separate test class or manage activity lifecycle within the method
    @Test
    public void testEditingTagsInViewDetailsActivity() {
        // Implementation for testing in ViewDetailsActivity
    }
}
