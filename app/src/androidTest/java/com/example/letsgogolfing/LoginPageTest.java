package com.example.letsgogolfing;

import androidx.test.espresso.Root;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static org.hamcrest.core.StringStartsWith.startsWith;

import android.os.IBinder;
import android.view.WindowManager;

@RunWith(AndroidJUnit4.class)
public class LoginPageTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);


    @Before
    public void setUp() {
        Intents.init();


    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testUserCanEnterUsername() {


        // Type a username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("newuser"), closeSoftKeyboard());
        // Check if the text is displayed in the EditText
        onView(withId(R.id.usernameInput)).check(matches(withText("newuser")));
    }

    @Test
    public void testUserCanClickLoginWithExistingUser() {
        // Type an existing username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("existinguser"), closeSoftKeyboard());
        // Click the login button
        onView(withId(R.id.loginButton)).perform(click());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // verify that main page opened
        intended(hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void testUserCanClickLoginWithNonExistingUser() {
        // Type a non-existing username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("nonexistinguser"), closeSoftKeyboard());
        // Click the login button
        onView(withId(R.id.loginButton)).perform(click());
    }

    @Test
    public void testUserCanClickSignUpWithNewUser() {
        // Type a new username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("newuser"), closeSoftKeyboard());
        // Click the sign-up button
        onView(withId(R.id.signUpButton)).perform(click());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testUserCanClickSignUpWithExistingUser() {
        // Type an existing username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("existinguser"), closeSoftKeyboard());
        // Click the sign-up button
        onView(withId(R.id.signUpButton)).perform(click());

    }




}
