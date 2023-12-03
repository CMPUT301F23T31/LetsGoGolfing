package com.example.letsgogolfing;

import androidx.test.core.app.ActivityScenario;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
        System.setProperty("isRunningEspressoTest", "true");
        animationUtils.disableAnimations();
        itemsFetched = false;
        tagsFetched = false;
        FirestoreRepository db = new FirestoreRepository("test");

        db.fetchItems(new FirestoreRepository.OnItemsFetchedListener() {
            @Override
            public void onItemsFetched(List<Item> items) {
                itemsFetched = true;
                myItems = (ArrayList<Item>)items;
            }

            @Override
            public void onError(Exception e) {

            }
        });
        db.fetchTags(new FirestoreRepository.OnTagsFetchedListener() {
            @Override
            public void onTagsFetched(List<String> tags) {
                tagsFetched = true;
                myTags = (ArrayList<String>) tags;
            }

            @Override
            public void onError(Exception e) {

            }
        });
        while (!itemsFetched || !tagsFetched) { // Directly accessing attribute from class???
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @After
    public void cleanup() {
        animationUtils.enableAnimations();
    }
    private boolean itemsFetched;
    private boolean tagsFetched;
    private ArrayList<Item> myItems;
    private ArrayList<String> myTags;

    @Test
    public void testApplyTagsToSelectedItems() {

       Random random = new Random();
       if(myItems.size() == 0 || myTags.size() == 0) // If there aren't any items to test...
           return;

       int [] itemIndices = generateUniqueRandomIntArray(random.nextInt(myItems.size())+1, myItems.size());


        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.itemGrid))
                .atPosition(itemIndices[0])
                .perform(ViewActions.longClick());

        for(int i = 1; i < itemIndices.length; ++i)
            Espresso.onData(Matchers.anything())
                    .inAdapterView(ViewMatchers.withId(R.id.itemGrid))
                    .atPosition(itemIndices[i])
                    .perform(click());

        onView(withId(R.id.manage_tags_button)).perform(click());

        int [] tagIndices = generateUniqueRandomIntArray(random.nextInt(myTags.size())+1, myTags.size());
        for(int tagIndex : tagIndices)
            onView(ViewMatchers.withText(myTags.get(tagIndex))).perform(click());

        onView(ViewMatchers.withText("OK")).perform(click());

//        for(int item : itemIndices) {
//            Espresso.onData(Matchers.anything())
//                    .inAdapterView(ViewMatchers.withId(R.id.itemGrid))
//                    .atPosition(item)
//                    .perform(ViewActions.click());
//            for(int tagIndex : tagIndices)
//                onView(ViewMatchers.withText(myTags.get(tagIndex))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
//        }
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < itemIndices.length; ++i) {
            Espresso.onData(Matchers.anything())
                    .inAdapterView(ViewMatchers.withId(R.id.itemGrid))
                    .atPosition(itemIndices[i])
                    .perform(click());
            for(int tagIndex : tagIndices)
                onView(ViewMatchers.withText(myTags.get(tagIndex))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }

    }

    // max is exclusive
    private static int[] generateUniqueRandomIntArray(int size, int max) {
        if (size <= 0) {
            throw new IllegalArgumentException("Array size must be greater than zero.");
        }

        int[] array = new int[size];
        Set<Integer> uniqueSet = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < size; ) {
            int randomNum = random.nextInt(max); // Generates random integers
            // If you want to limit the range, you can use:
            // int randomNum = random.nextInt(max - min) + min;

            if (uniqueSet.add(randomNum)) {
                // If the number is unique, add it to the array
                array[i] = randomNum;
                i++;
            }
        }

        return array;
    }
}
