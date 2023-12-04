package com.example.letsgogolfing;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import com.example.letsgogolfing.controllers.AddItemActivity;
import com.example.letsgogolfing.controllers.LoginActivity;
import com.example.letsgogolfing.views.DatePickerEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class AddItemActivityTest{

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);
    private AddItemActivity activity;


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



    @Test
    public void testSuccessfulItemAddition() {

        onView(withId(R.id.nameField)).perform(typeText("Golf Club"), closeSoftKeyboard());
        onView(withId(R.id.descriptionField)).perform(typeText("Brand new golf club"), closeSoftKeyboard());
        onView(withId(R.id.valueField)).perform(typeText("100"), closeSoftKeyboard());
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

    @After
    public void tearDown() {
        Intents.release();
    }

}
