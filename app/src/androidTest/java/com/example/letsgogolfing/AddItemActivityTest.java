package com.example.letsgogolfing;

import android.widget.EditText;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import com.example.letsgogolfing.AddItemActivity;
import com.example.letsgogolfing.R;
import com.example.letsgogolfing.ToastMatcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
public class AddItemActivityTest {

    // Custom sleep method
    private static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Rule
    public ActivityScenarioRule<AddItemActivity> activityRule = new ActivityScenarioRule<>(AddItemActivity.class);

//    @Test
//    public void testSaveItemWithValidData() {
//        // Enter valid data
//        Espresso.onView(ViewMatchers.withId(R.id.nameField)).perform(ViewActions.typeText("Test Item"));
//        Espresso.onView(ViewMatchers.withId(R.id.descriptionField)).perform(ViewActions.typeText("Test Description"));
//        // Add more data entry as needed
//
//        // Perform click on the confirm button
//        Espresso.onView(ViewMatchers.withId(R.id.confirmBtn)).perform(ViewActions.click());
//
//        // Verify that the activity finishes successfully
//        Espresso.onView(ViewMatchers.withId(R.id.add_item_layout)).check(ViewAssertions.doesNotExist());
//    }

    @Test
    public void testSaveItemWithInvalidDate() {
        // Enter invalid date
        Espresso.onView(ViewMatchers.withId(R.id.dateField)).perform(ViewActions.typeText("123123"), ViewActions.closeSoftKeyboard());

        // Scroll to the Confirm Button
        Espresso.onView(ViewMatchers.withId(R.id.confirmBtn)).perform(ViewActions.scrollTo());

        // Sleep to wait for animation (adjust the duration based on your needs)
        sleep(500); // 0.5 seconds

        Espresso.onView(ViewMatchers.withId(R.id.commentField)).perform(ViewActions.typeText("Test date failed"), ViewActions.closeSoftKeyboard());

        sleep(500); // 0.5 seconds

        // Wait for the view to be displayed
        Espresso.onView(ViewMatchers.withId(R.id.confirmBtn)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Perform click after waiting for the UI to settle
        Espresso.onView(ViewMatchers.withId(R.id.confirmBtn)).perform(ViewActions.click());

        // Sleep to wait for animation (adjust the duration based on your needs)
        sleep(1000); // 1 seconds

        // Verify that a toast with an error message is displayed
        // Toast detection not working
        Espresso.onView(ViewMatchers.withText("Failed to parse date")).inRoot(ToastMatcher.isToast())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        // Check if the text in the commentField matches the expected value
        Espresso.onView(ViewMatchers.withId(R.id.commentField)).check(ViewAssertions.matches(ViewMatchers.withText("Test date failed")));
    }

    // Add more test cases as needed
}
