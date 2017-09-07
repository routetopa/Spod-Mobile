package eu.spod.isislab.spodapp.utils;

/**
 * Created by Utente on 14/07/2017.
 */
public class User {

    private String id;
    private String username;
    private String avatarImage;
    private String name;

    private static User ourInstance = new User();

    private User() {}

    public static User getInstance() {
        return ourInstance;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void init(String id, String username, String avatarImage, String name){
        this.id          = id;
        this.username    = username;
        this.avatarImage = avatarImage;
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
}
