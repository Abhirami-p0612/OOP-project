package com.project.lostandfound;

public class FoundItem {
    private int id;
    private String itemName;      // NEW - item name for found items
    private String description;
    private String location;
    private String contactInfo;
    private String dateFound;
    private String imagePath;

    public FoundItem() {}

    public FoundItem(int id, String itemName, String description, String location, String contactInfo, String dateFound, String imagePath) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.contactInfo = contactInfo;
        this.dateFound = dateFound;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getDateFound() { return dateFound; }
    public void setDateFound(String dateFound) { this.dateFound = dateFound; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
