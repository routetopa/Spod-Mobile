package eu.spod.isislab.spodapp.utils;

import eu.spod.isislab.spodapp.entities.User;

public class UserManager {

    private User user;

    private static UserManager ourInstance = new UserManager();

    private UserManager() {}

    public void init(String id, String username, String avatarImage, String name){
        this.user = new User(id, username, avatarImage, name);
    }

    public static UserManager getInstance() {
        return ourInstance;
    }

    public String getId() {
        return user.getId();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getAvatarImage() {
        return user.getAvatarImage();
    }

    public String getName() {
        return user.getName();
    }

    public void setName(String name) { user.setName(name); }
}
