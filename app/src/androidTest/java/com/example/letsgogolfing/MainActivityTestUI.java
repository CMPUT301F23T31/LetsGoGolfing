package com.example.letsgogolfing;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.letsgogolfing.controllers.AddItemActivity;
import com.example.letsgogolfing.controllers.LoginActivity;
import com.example.letsgogolfing.controllers.ViewProfileActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTestUI {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);


    private Context instrumentationContext;

    @Before
    public void setUp() {
        Intents.init();

        // Perform the login action
        onView(withId(R.id.usernameInput)).perform(typeText("beta"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());


        // Wait for MainActivity to start
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testAddItemButton() {
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Click the add item button
        onView(withId(R.id.addItemButton)).perform(click());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        intended(hasComponent(AddItemActivity.class.getName()));
        // Check that the AddItemActivity was launched
    }

    @Test
    public void testProfileButton() {
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Click the profile button
        onView(withId(R.id.profileButton)).perform(click());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        intended(hasComponent(ViewProfileActivity.class.getName()));
        // Check that the ProfileActivity was launched
    }

    @Test
    public void testScanButton() {
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Click the scan button
        onView(withId(R.id.scan_item_button)).perform(click());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testTagButton() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Simulate item selection - replace R.id.your_item_in_grid with the actual ID of an item in your grid
        onView(withId(R.id.itemGrid)).perform(longClick());

        // Now check if the manage tags button is visible
        onView(withId(R.id.manage_tags_button)).check(matches(isDisplayed()));
        // NOTE: unfortunately we cant test dialogs using espresso, so the best way to test the methods of the tags-
        // -is to use unit tests, which i have implemented in the unittest folder.
    }

}
