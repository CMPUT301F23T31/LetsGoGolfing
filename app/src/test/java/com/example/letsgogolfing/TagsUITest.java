package com.example.letsgogolfing;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import com.example.letsgogolfing.AddItemActivity;
import com.example.letsgogolfing.MainActivity;

@RunWith(AndroidJUnit.class)
public class TagsUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testAddingNewTagFromMain() {
        // Click on the Add Tags button
        Espresso.onView(ViewMatchers.withId(R.id.add_tags_button)).perform(ViewActions.click());

        // Type the new tag
        Espresso.onView(ViewMatchers.withId(R.id.newTagEditText)).perform(ViewActions.typeText("kitchenware"));

        // Click the Add button to submit the new tag
        Espresso.onView(ViewMatchers.withId(R.id.addTagButton)).perform(ViewActions.click());

        // Optionally check for a Toast message or a new tag added to the list
        // ... (Toast checking code or list item checking code)
    }

    @Test
    public void testSelectingTagsInAddItemActivity() {
        // Start AddItemActivity directly
        ActivityScenarioRule<AddItemActivity> addItemActivityRule = new ActivityScenarioRule<>(AddItemActivity.class);

        // Fill in the item details
        Espresso.onView(ViewMatchers.withId(R.id.nameField)).perform(ViewActions.typeText("Sample Item"));
        Espresso.onView(ViewMatchers.withId(R.id.descriptionField)).perform(ViewActions.typeText("Sample Description"));
        Espresso.onView(ViewMatchers.withId(R.id.dateField)).perform(ViewActions.typeText("2023-01-01"));
        Espresso.onView(ViewMatchers.withId(R.id.modelField)).perform(ViewActions.typeText("Sample Model"));
        Espresso.onView(ViewMatchers.withId(R.id.makeField)).perform(ViewActions.typeText("Sample Make"));
        Espresso.onView(ViewMatchers.withId(R.id.valueField)).perform(ViewActions.typeText("100"));

        // Click on the Add Tags button
        Espresso.onView(ViewMatchers.withId(R.id.add_tags_button)).perform(ViewActions.click());

        // Assume a tag "kitchenware" is already present from the previous test
        // Select the tag "kitchenware" from the dialog
        Espresso.onView(ViewMatchers.withText("kitchenware")).perform(ViewActions.click());

        // Confirm the tag selection
        Espresso.pressBack(); // To close the dialog

        // Click the Confirm button to save the item
        Espresso.onView(ViewMatchers.withId(R.id.confirmBtn)).perform(ViewActions.click());

        // Optionally, check the item's detail view or database to see if the item has been saved with the tag
        // ... (detail view checking code or database checking code)
    }

    @Test
    public void testEditingTagsInViewDetailsActivity() {
        // Start ViewDetailsActivity with an existing item
        // This will require you to create an intent that has an item with ID and tags already set
        // Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ViewDetailsActivity.class);
        // intent.putExtra("ITEM_ID", "existing_item_id");
        // ViewDetailsActivity viewDetailsActivity = launchActivity(intent);

        // Click on the Edit Info button
        Espresso.onView(ViewMatchers.withId(R.id.editInfoBtn)).perform(ViewActions.click());

        // Click on the Add Tags button
        Espresso.onView(ViewMatchers.withId(R.id.add_tags_button_view)).perform(ViewActions.click());

        // Select or deselect tags in the dialog
        // ... (selecting/deselecting tags code)
        Espresso.pressBack(); // To close the dialog

        // Click the Save button to save the changes
        Espresso.onView(ViewMatchers.withId(R.id.saveBtn)).perform(ViewActions.click());

        // Optionally, check the item's detail view or database to see if the item has been updated with the new tags
        // ... (detail view checking code or database checking code)
    }
}
