package com.example.letsgogolfing;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.content.Intent;
import android.text.TextUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.Date;
import androidx.test.core.app.ApplicationProvider;

import static com.example.letsgogolfing.utils.Formatters.decimalFormat;
import static com.example.letsgogolfing.utils.Formatters.dateFormat;

@RunWith(AndroidJUnit4.class)
public class ViewDetailsActivityTest {

    // Sample item to be used in tests

    private static final Item TEST_ITEM = new Item("Sample Name", "Sample Description", new Date(), "Sample Make", "Sample Model", "1234567890", 123.45, "Sample Comment", new ArrayList<>());
    private ActivityScenario<ViewDetailsActivity> scenario;

    @Before
    public void setUp() {
        // Create an intent with the item as an extra
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ViewDetailsActivity.class);
        intent.putExtra("ITEM", TEST_ITEM);

        // Launch the activity with the intent
        scenario = ActivityScenario.launch(intent);
    }
    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void itemDetails_DisplayedInUi() {

        Intent startIntent = new Intent(ApplicationProvider.getApplicationContext(), ViewDetailsActivity.class);
        startIntent.putExtra("ITEM", TEST_ITEM);

        // Launch the activity with the intent
        try (ActivityScenario<ViewDetailsActivity> scenario = ActivityScenario.launch(startIntent)) {
            // Check if the name is displayed
            onView(withId(R.id.name_edit_text)).check(matches(withText(TEST_ITEM.getName())));

            // Check if the name is displayed
            onView(withId(R.id.name_edit_text)).check(matches(withText(TEST_ITEM.getName())));

            // Check if the description is displayed
            onView(withId(R.id.description_edit_text)).check(matches(withText(TEST_ITEM.getDescription())));

            // ... Perform other checks for each field
            onView(withId(R.id.date_edit_text)).check(matches(withText(dateFormat.format(TEST_ITEM.getDateOfPurchase()))));

            onView(withId(R.id.comment_edit_text)).check(matches(withText(TEST_ITEM.getComment())));

            onView(withId(R.id.serial_edit_text)).check(matches(withText(TEST_ITEM.getSerialNumber())));

            onView(withId(R.id.model_edit_text)).check(matches(withText(TEST_ITEM.getModel())));

            onView(withId(R.id.make_edit_text)).check(matches(withText(TEST_ITEM.getMake())));

            onView(withId(R.id.value_edit_text)).check(matches(withText(decimalFormat.format(TEST_ITEM.getEstimatedValue()))));

            onView(withId(R.id.tagsField)).check(matches(withText(TextUtils.join(", ", TEST_ITEM.getTags()))));
        }
    }

    @Test
    public void editButton_EnablesFields() {
        // Click on the edit button
        onView(withId(R.id.edit_item_button)).perform(click());

        // Check if the name field is enabled
        onView(withId(R.id.name_edit_text)).check(matches(isEnabled()));

        // Check if the description field is enabled
        onView(withId(R.id.description_edit_text)).check(matches(isEnabled()));

        // Check if the make field is enabled
        onView(withId(R.id.make_edit_text)).check(matches(isEnabled()));

        // Check if the model field is enabled
        onView(withId(R.id.model_edit_text)).check(matches(isEnabled()));

        // Check if the serial number field is enabled
        onView(withId(R.id.serial_edit_text)).check(matches(isEnabled()));

        // Check if the comment field is enabled
        onView(withId(R.id.comment_edit_text)).check(matches(isEnabled()));

        // Check if the date field is enabled
        onView(withId(R.id.date_edit_text)).check(matches(isEnabled()));

        // Check if the value field is enabled
        onView(withId(R.id.value_edit_text)).check(matches(isEnabled()));

        // Check if the tags field is enabled
        onView(withId(R.id.tagsField)).check(matches(isEnabled()));
    }

    @Test
    public void editButton_EnablesSaveButton() {
        onView(withId(R.id.edit_item_button)).perform(click());

        onView(withId(R.id.addItemButton)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void fields_AreNotEnabled_AfterSaveButtonClicked() {
        // Click the edit button to enable the fields
        onView(withId(R.id.edit_item_button)).perform(click());

        // Click the save button to save the changes and disable the fields
        onView(withId(R.id.save_button)).perform(click());

        // Check that the fields are not enabled
        onView(withId(R.id.name_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.description_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.make_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.model_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.serial_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.comment_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.date_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.value_edit_text)).check(matches(not(isEnabled())));
        onView(withId(R.id.tagsField)).check(matches(not(isEnabled())));
    }

    // ... Add more tests as needed
}
