package com.example.letsgogolfing;


//import com.google.firebase.firestore.Exclude;
import java.util.Date;
import java.util.List;


/**
 * Represents an item with detailed information. This class is used within an inventory management context
 * to hold and manipulate data related to individual items.
 */
public class Item implements Comparable<Item>, java.io.Serializable {


    // ** private String id; ** we may or may not need this? depends if we want to deal with item id's separately
    // from documentID from the FireStore database, if that sounds confusing we can discuss bout it later. - VT
    private String id;

    private String name;
    private String description;
    private Date dateOfPurchase; // im assuming its "of purchase"?
    private String make;
    private String model;
    private String serialNumber;
    private double estimatedValue;
    private String comment;
    private List<String> tags;


    /**
     * Default constructor for Firebase to create a new instance of an Item.
     * This is required because Firebase uses reflection to fill in fields.
     */
    public Item() {}

    /**
     * Full constructor for creating new instances of Item with all details provided.
     *
     * @param name          The name of the item.
     * @param description   The description of the item.
     * @param dateOfPurchase The purchase date of the item.
     * @param make          The make or brand of the item.
     * @param model         The model of the item.
     * @param serialNumber  The serial number of the item.
     * @param estimatedValue The estimated value of the item.
     * @param comment       Any additional comments about the item.
     * @param tags          A list of tags for categorizing the item.
     */
    public Item(String name, String description, Date dateOfPurchase, String make,
                String model, String serialNumber, double estimatedValue,
                String comment, List<String> tags) {
        this.name = name;
        this.description = description;
        this.dateOfPurchase = dateOfPurchase;
        this.make = make;
        this.model = model;
        this.serialNumber = serialNumber;
        this.estimatedValue = estimatedValue;
        this.comment = comment;
        this.tags = tags;
    }

    /**
     * Retrieves the unique identifier for this item.
     *
     * @return The unique identifier of the item.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this item.
     *
     * @param id The unique identifier to set for the item.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the name of this item.
     *
     * @return The name of the item.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name for this item.
     *
     * @param name The name to set for the item.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the description of this item.
     *
     * @return The description of the item.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this item.
     *
     * @param description The description to set for the item.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retrieves the purchase date of this item.
     *
     * @return The date of purchase of the item.
     */
    public Date getDateOfPurchase() {
        return dateOfPurchase;
    }

    /**
     * Sets the purchase date for this item.
     *
     * @param dateOfPurchase The date of purchase to set for the item.
     */
    public void setDateOfPurchase(Date dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }

    /**
     * Retrieves the make (brand) of this item.
     *
     * @return The make (brand) of the item.
     */
    public String getMake() {
        return make;
    }

    /**
     * Sets the make (brand) for this item.
     *
     * @param make The make (brand) to set for the item.
     */
    public void setMake(String make) {
        this.make = make;
    }

    /**
     * Retrieves the model of this item.
     *
     * @return The model of the item.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model for this item.
     *
     * @param model The model to set for the item.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Retrieves the serial number of this item.
     *
     * @return The serial number of the item.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the serial number for this item.
     *
     * @param serialNumber The serial number to set for the item.
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Retrieves the estimated value of this item.
     *
     * @return The estimated value of the item.
     */
    public double getEstimatedValue() {
        return estimatedValue;
    }

    /**
     * Sets the estimated value for this item.
     *
     * @param estimatedValue The estimated value to set for the item.
     */
    public void setEstimatedValue(double estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    /**
     * Retrieves the comment associated with this item.
     *
     * @return The comment associated with the item.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment for this item.
     *
     * @param comment The comment to set for the item.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Retrieves the list of tags associated with this item.
     *
     * @return The list of tags associated with the item.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the list of tags for this item.
     *
     * @param tags The list of tags to set for the item.
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }


    /**
     * Generates a string representation of the Item for debugging and logging purposes.
     *
     * @return A string representation of the Item.
     */
    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", dateOfPurchase=" + dateOfPurchase +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", estimatedValue=" + estimatedValue +
                ", comment='" + comment + '\'' +
                ", tags=" + tags +
                '}';
    }


    /**
     * Compares this Item to another Item based on their names.
     *
     * @param item The Item to compare to.
     * @return An integer representing the result of the comparison.
     */
    @Override
    public int compareTo(Item item) {
        return this.getName().compareTo(item.getName());
    }



}
