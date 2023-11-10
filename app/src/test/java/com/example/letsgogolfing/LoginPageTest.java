package com.example.letsgogolfing;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
public class LoginPageTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

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
        // TODO: Verify that the MainActivity is opened
    }

    @Test
    public void testUserCanClickLoginWithNonExistingUser() {
        // Type a non-existing username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("nonexistinguser"), closeSoftKeyboard());
        // Click the login button
        onView(withId(R.id.loginButton)).perform(click());
        // TODO: Verify that the user is prompted to sign up
    }

    @Test
    public void testUserCanClickSignUpWithNewUser() {
        // Type a new username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("newuser"), closeSoftKeyboard());
        // Click the sign-up button
        onView(withId(R.id.signUpButton)).perform(click());
        // TODO: Verify that the MainActivity is opened after sign up
    }

    @Test
    public void testUserCanClickSignUpWithExistingUser() {
        // Type an existing username into the username input field
        onView(withId(R.id.usernameInput)).perform(typeText("existinguser"), closeSoftKeyboard());
        // Click the sign-up button
        onView(withId(R.id.signUpButton)).perform(click());
        // TODO: Verify that the user is prompted to login
    }
}
