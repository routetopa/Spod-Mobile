package eu.spod.isislab.spodapp.entities;

public class User {

    private String id;
    private String username;
    private String avatarImage;
    private String name;
    private String email;

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
}
