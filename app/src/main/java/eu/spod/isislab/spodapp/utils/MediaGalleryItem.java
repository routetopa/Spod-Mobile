package eu.spod.isislab.spodapp.utils;

import android.location.Location;

/**
 * Created by Utente on 04/07/2017.
 */
public class MediaGalleryItem {

    private String title;
    private String description;
    private String image;
    private String date;
    private Location location;

    public MediaGalleryItem(String title, String description, String image, Location location, String date) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.location = location;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
