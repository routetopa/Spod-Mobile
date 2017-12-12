package eu.spod.isislab.spodapp.entities;

import java.util.Iterator;
import java.util.List;

public class ImageListPost extends Post {

    private List<JsonImage> images;
    private int count;
    private int[] idList;

    public ImageListPost(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds, List<JsonImage> images, int count, int[] idList) {
        super(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);

        this.images = images;
        this.count = count;
        this.idList = idList;
    }

    public List<JsonImage> getImages() {
        return images;
    }

    public Iterator<JsonImage> getImageIterator() {
        return images.iterator();
    }

    public int getImagesCount(){
        return count;
    }

    public int[] getIdList() {
        return idList;
    }

    public JsonImage getImageById(String selectedImageId) {
        for(JsonImage image : images) {
            if(image.getId().equals(selectedImageId)) {
                return image;
            }
        }
        return null;
    }
}
