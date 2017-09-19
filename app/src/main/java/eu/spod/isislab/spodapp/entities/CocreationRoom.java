package eu.spod.isislab.spodapp.entities;

public class CocreationRoom {
    private String name;
    private String description;
    private String id;
    private String sheetId;
    private String ownerName;
    private String ownerImage;
    private String timestamp;

    public CocreationRoom(String name, String description, String id, String sheetId, String ownerName, String ownerImage, String timestamp) {
        this.name        = name;
        this.description = description;
        this.id          = id;
        this.sheetId     = sheetId;
        this.ownerName   = ownerName;
        this.ownerImage  = ownerImage;
        this.timestamp   = timestamp;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSheetId() {
        return sheetId;
    }

    public void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerImage() {
        return ownerImage;
    }

    public void setOwnerImage(String ownerImage) {
        this.ownerImage = ownerImage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
