package com.example.letsgogolfing;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
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
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(AndroidJUnit4.class)
public class TagsUITest {
    private AnimationUtils animationUtils = new AnimationUtils();
//    @Rule
//    public ActivityScenarioRule<MainActivity> mainActivityRule = new ActivityScenarioRule<>(MainActivity.class);
    @Rule
public ActivityScenarioRule<LoginActivity> activityRule = new ActivityScenarioRule<>(LoginActivity.class);

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
    private FirestoreRepository db;
    @Before
    public void setup() {
        Intents.init();

        // Perform the login action
        String newUser = generateRandomString(10);
        // does not check in case the user already exists...

        onView(withId(R.id.usernameInput)).perform(typeText(newUser), closeSoftKeyboard());
        onView(withId(R.id.signUpButton)).perform(click());

        //System.setProperty("isRunningEspressoTest", "true");
        animationUtils.disableAnimations();
        itemsFetched = false;
        tagsFetched = false;

        db = new FirestoreRepository(newUser);
        itemsCreated = 0;
        int itemsToMake = random.nextInt(10)+1;
        createSampleItems(itemsToMake);

        while(itemsCreated != itemsToMake) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


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

    Random random = new Random();

    @Test
    public void testApplyTagsToSelectedItems() {

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

        int temp = random.nextInt(myTags.size())+1;
        int [] tagIndices = generateUniqueRandomIntArray(temp > 5 ? 5 : temp, myTags.size());
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

        for(int item: itemIndices) {
            Espresso.onData(Matchers.anything())
                    .inAdapterView(ViewMatchers.withId(R.id.itemGrid))
                    .atPosition(item)
                    .perform(click());
            for(int tagIndex : tagIndices)
                onView(ViewMatchers.withText(myTags.get(tagIndex))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
           onView(ViewMatchers.withId(R.id.back_button)).perform(click());
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
    private int itemsCreated;
    private void createSampleItems(int itemsToMake){
        final int maxItems = 10;
        final int randomStringMaxLength = 15;
        for(int i = 0; i < itemsToMake; ++i)
            db.addItem(new Item(generateRandomString(random.nextInt(randomStringMaxLength) + 1), null, generateRandomDate(), null, null, null, random.nextDouble(), null, null),
                    new FirestoreRepository.OnItemAddedListener() {
                        @Override
                        public void onItemAdded(String itemId) {
                            ++itemsCreated;


                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });

    }

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
     private String generateRandomString(int length) {
        StringBuilder randomString = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHANUMERIC_CHARS.length());
            char randomChar = ALPHANUMERIC_CHARS.charAt(randomIndex);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }
    public static Date generateRandomDate() {
        // Define a date range (adjust as needed)
        LocalDate startDate = LocalDate.of(1970, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);

        // Generate a random number of days between the start and end dates
        long randomDays = ThreadLocalRandom.current().nextLong(0, endDate.toEpochDay() - startDate.toEpochDay());

        // Create a random date within the specified range
        LocalDate randomDate = startDate.plusDays(randomDays);

        // Convert LocalDate to Date
        Instant instant = randomDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);

        return date;
    }

}
