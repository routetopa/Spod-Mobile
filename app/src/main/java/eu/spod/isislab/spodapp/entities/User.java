package eu.spod.isislab.spodapp.entities;

public class User {

    public static int COCREATION_STATUS_JOINED      = 0;
    public static int COCREATION_STATUS_PENDING     = 1;
    public static int COCREATION_STATUS_NOT_INVITED = 2;

    private String id;
    private String username;
    private String avatarImage;
    private String name;
    private String email;
    private int status;

    public User(String id, String username, String avatarImage, String name){
        this.id          = id;
        this.username    = username;
        this.avatarImage = avatarImage;
        this.name        = name;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarImage() {
        return avatarImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public int getStatus() { return status; }

    public void setStatus(int status) { this.status = status; }
}
