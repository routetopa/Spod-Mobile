package eu.spod.isislab.spodapp;

/**
 * Created by vinnun on 01/12/2017.
 */

public class NewsfeedImageInfo {
    private int id;
    private String description;
    private long time;
    private String userName;
    private int userId;
    private String albumName;
    private int albumId;
    private String url;

    public NewsfeedImageInfo(int id, String description, long time, String userName, int userId, String albumName, int albumId, String url) {
        this.id = id;
        this.description = description;
        this.time = time;
        this.userName = userName;
        this.userId = userId;
        this.albumName = albumName;
        this.albumId = albumId;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getTime() {
        return time*1000;
    }

    public String getUserName() {
        return userName;
    }

    public int getUserId() {
        return userId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public int getAlbumId() {
        return albumId;
    }

    public String getUrl() {
        return url;
    }
}
