package com.example.letsgogolfing;
import static org.junit.Assert.*;

import com.example.letsgogolfing.models.Item;

import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ItemTest {

    private Item testItem;
    private Date purchaseDate;
    private List<String> testTags;

    @Before
    public void setUp() {
        purchaseDate = new Date();
        testTags = Arrays.asList("electronics", "office");
        testItem = new Item("Laptop", "Gaming Laptop", purchaseDate, "BrandX", "ModelY", "12345", 1500.00, "Bought for gaming", testTags);
        testItem.setId("abc123");
    }

    @Test
    public void testGetters() {
        assertEquals("Laptop", testItem.getName());
        assertEquals("Gaming Laptop", testItem.getDescription());
        assertEquals(purchaseDate, testItem.getDateOfPurchase());
        assertEquals("BrandX", testItem.getMake());
        assertEquals("ModelY", testItem.getModel());
        assertEquals("12345", testItem.getSerialNumber());
        assertEquals(1500.00, testItem.getEstimatedValue(), 0.001);
        assertEquals("Bought for gaming", testItem.getComment());
        assertEquals(testTags, testItem.getTags());
        assertEquals("abc123", testItem.getId());
    }

    @Test
    public void testSetters() {
        testItem.setName("Desktop");
        assertEquals("Desktop", testItem.getName());

        testItem.setDescription("Office Desktop");
        assertEquals("Office Desktop", testItem.getDescription());

        Date newPurchaseDate = new Date(purchaseDate.getTime() + 100000);
        testItem.setDateOfPurchase(newPurchaseDate);
        assertEquals(newPurchaseDate, testItem.getDateOfPurchase());

        testItem.setMake("BrandZ");
        assertEquals("BrandZ", testItem.getMake());

        testItem.setModel("ModelX");
        assertEquals("ModelX", testItem.getModel());

        testItem.setSerialNumber("54321");
        assertEquals("54321", testItem.getSerialNumber());

        testItem.setEstimatedValue(1000.00);
        assertEquals(1000.00, testItem.getEstimatedValue(), 0.001);

        testItem.setComment("Bought for office work");
        assertEquals("Bought for office work", testItem.getComment());

        List<String> newTags = Arrays.asList("computer", "hardware");
        testItem.setTags(newTags);
        assertEquals(newTags, testItem.getTags());

        testItem.setId("xyz789");
        assertEquals("xyz789", testItem.getId());
    }

    @Test
    public void testToString() {
        String expectedString = "Item{name='Laptop', description='Gaming Laptop', dateOfPurchase=" + purchaseDate +
                ", make='BrandX', model='ModelY', serialNumber='12345', estimatedValue=1500.0, " +
                "comment='Bought for gaming', tags=[electronics, office]}";
        assertEquals(expectedString, testItem.toString());
    }

    @Test
    public void testCompareTo() {
        Item otherItem = new Item("Keyboard", "Mechanical Keyboard", new Date(), "BrandK", "ModelK", "SN67890", 250.00, "Bought for typing", Arrays.asList("peripheral", "input"));
        assertTrue(testItem.compareTo(otherItem) > 0);
        assertTrue(otherItem.compareTo(testItem) < 0);

        Item sameNameItem = new Item("Laptop", "Old Laptop", new Date(), "BrandO", "ModelO", "SN09876", 300.00, "Old but functional", Arrays.asList("used", "electronics"));
        assertEquals(0, testItem.compareTo(sameNameItem));
    }
    @Test
    public void testViewListOfItems(){
        testItem.setName("Desktop");
        assertEquals("Desktop", testItem.getName());

        testItem.setDescription("Office Desktop");
        assertEquals("Office Desktop", testItem.getDescription());

        Date newPurchaseDate = new Date(purchaseDate.getTime() + 100000);
        testItem.setDateOfPurchase(newPurchaseDate);
        assertEquals(newPurchaseDate, testItem.getDateOfPurchase());

        testItem.setMake("BrandZ");
        assertEquals("BrandZ", testItem.getMake());

        testItem.setModel("ModelX");
        assertEquals("ModelX", testItem.getModel());

        testItem.setSerialNumber("54321");
        assertEquals("54321", testItem.getSerialNumber());

        testItem.setEstimatedValue(1000.00);
        assertEquals(1000.00, testItem.getEstimatedValue(), 0.001);

        testItem.setComment("Bought for office work");
        assertEquals("Bought for office work", testItem.getComment());

        List<String> newTags = Arrays.asList("computer", "hardware");
        testItem.setTags(newTags);
        assertEquals(newTags, testItem.getTags());

        testItem.setId("xyz789");
        assertEquals("xyz789", testItem.getId());

    }
}

