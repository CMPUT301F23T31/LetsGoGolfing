package com.example.letsgogolfing;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.letsgogolfing.utils.FirestoreHelper;
import static com.example.letsgogolfing.utils.FirestoreHelper.db;
import static org.junit.Assert.assertTrue;

import static java.lang.Thread.sleep;

import org.junit.Before;

@LargeTest
public class LoginActivityTest {
    private static String[] namesTest = {"testLogin", "nonUniqueUser"};
    private static boolean isFirestoreSetupExecuted = false;

    //Just need to find a way to properly call this rule/set up UserTestOnly collection correctly so
    //that it functions as expected (having only namesTest at beginning of each test)
    @ClassRule
    public static SetUpRule firestoreSetupRule = new SetUpRule();

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

        // Wait for a view with a certain text to be displayed
        Espresso.onView(ViewMatchers.withText("Select"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Example: Check if a view from the new activity is present
        Espresso.onView(ViewMatchers.withId(R.id.select_button)).check(matches(ViewMatchers.isDisplayed()));
    }

//    @Test
//    public void testSignUpFail() {
//        // Type a unique username
//        Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
//                .perform(ViewActions.typeText("nonUniqueUser"), ViewActions.closeSoftKeyboard());
//
//        // Click the sign-up button
//        Espresso.onView(ViewMatchers.withId(R.id.signUpButton))
//                .perform(ViewActions.click());
//
////        // Check if error toast is displayed
////        Espresso.onView(ViewMatchers.withText("Username already exists, please login"))
////                .inRoot(new ToastMatcher())
////                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//
//        // Check if error tracking variable is updated
//        Espresso.onView(ViewMatchers.withId(R.id.usernameInput)).perform(ViewActions.clearText());
//        ActivityScenario<LoginActivity> scenario = activityScenarioRule.getScenario();
//        scenario.onActivity(currentActivity -> {
//            Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
//                    .perform(ViewActions.typeText(currentActivity.toastSucks), ViewActions.closeSoftKeyboard());
//        });
//        Espresso.onView(ViewMatchers.withId(R.id.usernameInput)).check(matches(withText("Username already exists, please login")));
//    }

    @Test
    public void testLogin() {
        // Type a valid username
        Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
                .perform(ViewActions.typeText("testLogin"), ViewActions.closeSoftKeyboard());

        // Click the login button
        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
                .perform(ViewActions.click());

        // Wait for a view with a certain text to be displayed
        Espresso.onView(ViewMatchers.withText("Select"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Example: Check if a view from the new activity is present
        Espresso.onView(ViewMatchers.withId(R.id.select_button)).check(matches(ViewMatchers.isDisplayed()));
    }

//    @Test
//    public void testLoginFail() {
//        // Type a valid username
//        Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
//                .perform(ViewActions.typeText("TestLoginFail"), ViewActions.closeSoftKeyboard());
//
//        // Click the login button
//        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
//                .perform(ViewActions.click());
//
//        //        // Check if error toast is displayed
////        Espresso.onView(ViewMatchers.withText("User does not exist, please sign up"))
////                .inRoot(new ToastMatcher())
////                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//
//
//        // Check if error tracking variable is updated
//        Espresso.onView(ViewMatchers.withId(R.id.usernameInput)).perform(ViewActions.clearText());
//        ActivityScenario<LoginActivity> scenario = activityScenarioRule.getScenario();
//        scenario.onActivity(currentActivity -> {
//            Espresso.onView(ViewMatchers.withId(R.id.usernameInput))
//                    .perform(ViewActions.typeText(currentActivity.toastSucks), ViewActions.closeSoftKeyboard());
//        });
//        Espresso.onView(ViewMatchers.withId(R.id.usernameInput)).check(matches(withText("User does not exist, please sign up")));
//    }
}