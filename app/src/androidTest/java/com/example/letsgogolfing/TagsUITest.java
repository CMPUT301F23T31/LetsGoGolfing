package com.example.letsgogolfing;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;

import static java.util.EnumSet.allOf;
import static java.util.regex.Pattern.matches;

import java.util.ArrayList;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class TagsUITest {
    private AnimationUtils animationUtils = new AnimationUtils();
    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testAddingNewTagFromMain() {
        // Navigate to the part of the UI where add_tags_button is present
        // For example, if it's in a menu, you might need to open the menu first
        // onView(withId(R.id.menu)).perform(click());

        onView(withId(R.id.manage_tags_button)).perform(click());
        onView(withId(R.id.newTagEditText)).perform(typeText("kitchenware"), closeSoftKeyboard());
        onView(withId(R.id.addTagButton)).perform(click());
        // Additional checks for the toast message or updated UI
    }

    // This test needs to be in a separate test class or manage activity lifecycle within the method
    @Test
    public void testSelectingTagsInAddItemActivity() {
        // Implementation for testing in AddItemActivity
    }

    // This test needs to be in a separate test class or manage activity lifecycle within the method
    @Test
    public void testEditingTagsInViewDetailsActivity() {
        // Implementation for testing in ViewDetailsActivity
    }

    @Before
    public void setup() {
        animationUtils.disableAnimations();
    }

    @After
    public void cleanup() {
        animationUtils.enableAnimations();
    }


    @Test
    public void testApplyTagsToSelectedItems() {
        int itemPosition = 0;

        // wait for items to get fetched first...
        while (!MainActivity.itemsFetched) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.itemGrid))
                .atPosition(itemPosition)
                .perform(ViewActions.longClick());
        onView(withId(R.id.manage_tags_button)).perform(click());

        String checkboxText = "bathroom";

        onView(ViewMatchers.withText(checkboxText)).perform(click());
        onView(ViewMatchers.withText("OK")).perform(click());
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.itemGrid))
                .atPosition(itemPosition)
                .perform(ViewActions.click());
        onView(ViewMatchers.withText(checkboxText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }
}
