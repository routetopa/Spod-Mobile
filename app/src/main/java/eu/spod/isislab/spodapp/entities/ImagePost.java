package eu.spod.isislab.spodapp.entities;

import org.json.JSONException;
import org.json.JSONObject;

import static eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper.PHOTO_ID;
import static eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper.PHOTO_PREVIEW_DOMENSIONS;
import static eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper.PHOTO_PREVIEW_URL;
import static eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper.PHOTO_TITLE;
import static eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper.convertToIntArray;

public class ImagePost extends Post {

    private JsonImage image;

    public ImagePost(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds, JsonImage image) {
        super(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);

        this.image = image;
    }

    public ImagePost(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds, JSONObject jsonImage) throws JSONException {
        super(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);

        this.image = convertToImage(jsonImage);
    }

    public JsonImage getImage() {
        return image;
    }


    public JsonImage convertToImage(JSONObject imageObject) throws JSONException {

        try{
            int photoId = imageObject.getInt(PHOTO_ID);
            String title = imageObject.getString(PHOTO_TITLE);
            int[] dimensions = convertToIntArray(imageObject.getJSONArray(PHOTO_PREVIEW_DOMENSIONS));
            String previewUrl = imageObject.getString(PHOTO_PREVIEW_URL);

            return new JsonImage(photoId, title, dimensions, previewUrl);
        }catch (JSONException e){
            throw new JSONException("The json object cannot be cast to JsonImage. " + e.getMessage());
        }

    }

    //Bridge methods to the image

    public int getImageId() {
        return image.getId();
    }

    public String getImageTitle() {
        return image.getTitle();
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
