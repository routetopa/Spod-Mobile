package eu.spod.isislab.spodapp.entities;

public class NewsfeedLike {

    private int userId;
    private String displayUserName;
    private String avatarUrl;
    private long timestamp;

    public NewsfeedLike(int userId, String displayUserName, String avatarUrl, long timestamp) {
        this.userId = userId;
        this.displayUserName = displayUserName;
        this.avatarUrl = avatarUrl;
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return userId;
    }

    public String getDisplayUserName() {
        return displayUserName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
