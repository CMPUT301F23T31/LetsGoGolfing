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
public class AddItemActivityTest{

    @Rule
    public ActivityScenarioRule<AddItemActivity> mainActivityRule = new ActivityScenarioRule<>(AddItemActivity.class);

    @Test
    public void testAddingNewItemFromMain() {
        // Type in the name
        onView(withId(R.id.nameField)).perform(typeText("New Item"), closeSoftKeyboard());

        // Type in the description
        onView(withId(R.id.descriptionField)).perform(typeText("Description of the item"), closeSoftKeyboard());

        // Type in the date
        onView(withId(R.id.dateField)).perform(typeText("2023-01-01"), closeSoftKeyboard());

        // Type in the model
        onView(withId(R.id.modelField)).perform(typeText("Model123"), closeSoftKeyboard());

        // Type in the make
        onView(withId(R.id.makeField)).perform(typeText("MakeABC"), closeSoftKeyboard());

        // Type in the value, if needed
        onView(withId(R.id.valueField)).perform(typeText("100"), closeSoftKeyboard());

        // Type in a comment, if needed
        onView(withId(R.id.commentField)).perform(typeText("Some comments"), closeSoftKeyboard());

        // Type in a serial number, if needed
        onView(withId(R.id.serialField)).perform(typeText("123456789"), closeSoftKeyboard());

        // Optionally, you can also test adding tags and photos, if the app supports these features

        // Click the confirm button to submit the item
        onView(withId(R.id.confirmBtn)).perform(click());
    }



}
