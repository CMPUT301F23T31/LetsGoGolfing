package com.example.letsgogolfing;


//import com.google.firebase.firestore.Exclude;
import java.util.Date;
import java.util.List;


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


    // Firebase cannot figure out on its own what your constructor does,
    // so that's why you need an empty constructor: to allow Firebase to create a
    // new instance of the object, which it then proceeds to fill in using reflection. - Anonymous on stackoverflow
    // ** Below is the Empty constructor for FireStore **
    public Item() {}


    // Full constructor for creating new Item instances
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Below are getters and setters for all fields - VT
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public Date getDateOfPurchase() {
        return dateOfPurchase;
    }


    public void setDateOfPurchase(Date dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }


    public String getMake() {
        return make;
    }


    public void setMake(String make) {
        this.make = make;
    }


    public String getModel() {
        return model;
    }


    public void setModel(String model) {
        this.model = model;
    }


    public String getSerialNumber() {
        return serialNumber;
    }


    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }


    public double getEstimatedValue() {
        return estimatedValue;
    }


    public void setEstimatedValue(double estimatedValue) {
        this.estimatedValue = estimatedValue;
    }


    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public List<String> getTags() {
        return tags;
    }


    public void setTags(List<String> tags) {
        this.tags = tags;
    }


    // toString() below is for debugging and logging purposes for the future - VT
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


    @Override
    public int compareTo(Item item) {
        return this.getName().compareTo(item.getName());
    }


}
