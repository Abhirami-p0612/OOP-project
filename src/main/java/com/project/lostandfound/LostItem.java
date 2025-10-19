package com.project.lostandfound;

public class LostItem {

    private int id;
    private String itemName;
    private String description;
    private String location;
    private String contactName;   // new
    private String contactPhone;  // new
    private String contactEmail;  // new
    private String dateLost;
    private String imagePath;
    private String status;        // optional, to track AWAITING_DELETION / DELETED etc.

    public LostItem() {}

    public LostItem(int id, String itemName, String description, String location,
                    String contactName, String contactPhone, String contactEmail,
                    String dateLost, String imagePath, String status) {
        this.id = id;
        this.itemName = itemName;
        this.description = description;
        this.location = location;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.dateLost = dateLost;
        this.imagePath = imagePath;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getDateLost() { return dateLost; }
    public void setDateLost(String dateLost) { this.dateLost = dateLost; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
