package com.project.lostandfound;

public class FoundItem {
    private int id;
    private String itemName;
    private String description;
    private String location;

    // ADDED fields:
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String status;

    private String dateFound;
    private String imagePath;

    public FoundItem() {}

    // Note: kept a simple no-arg constructor and setters/getters for BeanPropertyRowMapper
    public FoundItem(int id, String itemName, String description, String location, String contactInfo, String dateFound, String imagePath) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
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


    public String getDateFound() { return dateFound; }
    public void setDateFound(String dateFound) { this.dateFound = dateFound; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
