package com.example.letsgogolfing;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.Before;

@LargeTest
public class LoginActivityTest {
    private FirebaseFirestore db;
    @Before
    public void setUp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Delete all documents in the "usersTestOnly" collection
        db.collection("usersTestOnly")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    } else {
                        // Handle the error
                    }
                });
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testSignUp() {
        // Type a unique username
        Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
                .perform(ViewActions.typeText("uniqueUser"), ViewActions.closeSoftKeyboard());

        // Click the sign-up button
        Espresso.onView(ViewMatchers.withId(R.id.signUpButton))
                .perform(ViewActions.click());

        // Check if the main activity is displayed (replace MainActivity::class.java with your actual MainActivity)
        Espresso.onView(ViewMatchers.withId(R.id.loginPage))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testLogin() {
        // Type a valid username
        Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
                .perform(ViewActions.typeText("testLogin"), ViewActions.closeSoftKeyboard());

        // Click the sign-up button
        Espresso.onView(ViewMatchers.withId(R.id.signUpButton))
                .perform(ViewActions.click());

        // Click the login button
        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
                .perform(ViewActions.click());

        // Check if the main activity is displayed (replace MainActivity::class.java with your actual MainActivity)
        Espresso.onView(ViewMatchers.withId(R.id.loginPage)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testLoginFail() {
        // Type a valid username
        Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
                .perform(ViewActions.typeText("Ooga"), ViewActions.closeSoftKeyboard());

        // Click the login button
        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
                .perform(ViewActions.click());

        // Check if the main activity is displayed (replace MainActivity::class.java with your actual MainActivity)
        Espresso.onView(ViewMatchers.withId(R.id.loginPage)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}