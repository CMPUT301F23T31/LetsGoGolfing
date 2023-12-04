package com.example.letsgogolfing;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.action.ViewActions.click;



import android.content.Context;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;



@RunWith(AndroidJUnit4.class)
public class ViewProfileUITest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        Intents.init();

        // Perform the login action
        onView(withId(R.id.usernameInput)).perform(typeText("beta"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait for LoginActivity to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.profileButton)).perform(click());
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testProfileDisplay() {

        try {
            Thread.sleep(2000); // Wait for ProfileActivity to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the profile title is displayed
        onView(withId(R.id.profileTitle)).check(matches(withText("User Profile")));

        // Check if the user name is displayed
        onView(withId(R.id.nameLabel)).check(matches(withText("beta"))); // Assuming "beta" is the username


        // Check if the logout button is displayed
        onView(withId(R.id.logout_button)).check(matches(withText("Logout")));
    }

    @Test
    public void testLogoutFunctionality() {
        // Click the logout button
        onView(withId(R.id.logout_button)).perform(click());

        // Check if LoginActivity is launched after logout
        intended(hasComponent(LoginActivity.class.getName()));
    }
}


