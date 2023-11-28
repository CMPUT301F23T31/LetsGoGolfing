package com.example.letsgogolfing;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class ViewProfileUITest {

    @Rule
    public ActivityScenarioRule<ViewProfileActivity> activityRule = new ActivityScenarioRule<>(ViewProfileActivity.class);

    @Test
    public void profileDataIsDisplayed() {
        // Assuming the SharedPreferences and Firestore data are set up before this test runs
        // Check if the username is displayed
        onView(withId(R.id.nameLabel)).check(matches(withText("Expected Username")));

        // Check if the total items count is displayed
        onView(withId(R.id.totalItemCount)).check(matches(withText("Expected Total Items Count")));

        // Check if the total cost is displayed
        onView(withId(R.id.totalItemValue)).check(matches(withText("Expected Total Cost")));
    }

    @Test
    public void navigateToMainActivityWhenHomeButtonClicked() {
        // Click the home button
        onView(withId(R.id.homeButton)).perform(click());

        // TODO: Verify that the MainActivity is launched
        // This typically requires Espresso Intents or monitoring the Activity lifecycle
    }

    // Additional tests can be written to further verify the UI and interactions
}
