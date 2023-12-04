package com.example.letsgogolfing;

import androidx.test.espresso.Root;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.espresso.Root;

import android.os.IBinder;
import android.view.Window;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.core.util.Predicate.not;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.CharMatcher.is;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;


import static org.hamcrest.CoreMatchers.anything;

import android.content.Context;
import android.view.Window;

@RunWith(AndroidJUnit4.class)
public class AddItemActivityTest{

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);


    private Context instrumentationContext;

    @Before
    public void setUp() {
        Intents.init();

        // Perform the login action
        onView(withId(R.id.usernameInput)).perform(typeText("beta"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.addItemButton)).perform(click());


        // Wait for MainActivity to start
    }

    @After
    public void tearDown() {
        Intents.release();
    }



    @Test
    public void testSuccessfulItemAddition() {
        onView(withId(R.id.nameField)).perform(typeText("Golf Club"), closeSoftKeyboard());
        onView(withId(R.id.descriptionField)).perform(typeText("Brand new golf club"), closeSoftKeyboard());
        onView(withId(R.id.valueField)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.dateField)).perform(typeText("2002-02-02"), closeSoftKeyboard());
        onView(withId(R.id.serialField)).perform(typeText("Golf Course"), closeSoftKeyboard());
        onView(withId(R.id.commentField)).perform(typeText("Golf Course"), closeSoftKeyboard());
        onView(withId(R.id.makeField)).perform(typeText("Golf Course"), closeSoftKeyboard());
        onView(withId(R.id.modelField)).perform(typeText("Golf Course"), closeSoftKeyboard());


        onView(withId(R.id.confirmBtn)).perform(click());

        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testAddTagsButton() {
        // Click the 'Add Tags' button to open the dialog
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.add_tags_button)).perform(click());

        // Wait for the dialog to be displayed
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Since we cannot directly access the activity to get the decor view,
        // we use a different approach to handle the dialog interaction
        // Select a tag from the dialog - replace "TagName" with the actual tag name

        onView(withText("OK"))
                .inRoot(isDialog()) // Uses the corrected custom matcher
                .check(matches(isDisplayed()))
                .perform(click());
        // Click the 'OK' button in the dialog

        // Optionally, assert if the tag was added to the view
        // This depends on how the tags are displayed in your UI
    }

    public static Matcher<Root> isDialog() {
        return new TypeSafeMatcher<Root>() {
            @Override
            protected boolean matchesSafely(Root root) {
                // Get the Window token and Application window token
                IBinder windowToken = root.getDecorView().getWindowToken();
                IBinder appWindowToken = root.getDecorView().getApplicationWindowToken();

                // Check if the window token is not the same as the application window token, and it's not null
                return windowToken != appWindowToken && windowToken != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is dialog");
            }
        };
    }



    @Test
    public void testAddPhotoButton() {
        onView(withId(R.id.addPhotoBtn)).perform(click());
    }

    @Test
    public void testCancelButton() {

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.cancel_button_add_item)).perform(click());
    }






}
