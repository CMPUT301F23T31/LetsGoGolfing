package com.example.letsgogolfing;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.util.Log;

@RunWith(AndroidJUnit4.class)
public class ViewDetailsUITest {

    // Replace with the actual ID of the item you want to test
    private static final String ITEM_ID_FOR_TEST = "testItemId";

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {

        Intents.init();
        // Perform the login action
        onView(withId(R.id.usernameInput)).perform(typeText("beta"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for MainActivity to start
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testEditItemDetails() {

        // click the first item in the grid
        onView(withId(R.id.itemGrid)).perform(click());

        // click the edit button
        onView(withId(R.id.edit_item_button)).perform(click());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Verify that the edit item activity is displayed
        intended(hasComponent(EditItemActivity.class.getName()));
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
