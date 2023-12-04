package com.example.letsgogolfing;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.letsgogolfing.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

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
    public void testManageTagsButton() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click the manage tags button
        onView(withId(R.id.manage_tags_button)).perform(click());

        // Add assertions or intended actions to validate the manage tags functionality
        intended(hasComponent(ManageTagsActivity.class.getName()));

    }



}
