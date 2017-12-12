package eu.spod.isislab.spodapp.entities;

public class ImagePost extends Post {

    private JsonImage image;

    public ImagePost(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds, JsonImage image) {
        super(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);

        this.image = image;
    }

    public JsonImage getImage() {
        return image;
    }


    //Bridge methods to the image

    public String getImageId() {
        return image.getId();
    }

    public String getImageTitle() {
        return image.getDescription();
    }

    public int getImagePreviewWidth() {
        return image.getPreviewWidth();
    }

    public int getImagePreviewHeight() {
        return image.getPreviewHeight();
    }

    public String getImagePreviewUrl() {
        return image.getPreviewUrl();
    }
}
