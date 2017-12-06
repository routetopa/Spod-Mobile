package eu.spod.isislab.spodapp.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static eu.spod.isislab.spodapp.NewsfeedJSONHelper.*;
import static eu.spod.isislab.spodapp.NewsfeedJSONHelper.PHOTO_ID;
import static eu.spod.isislab.spodapp.NewsfeedJSONHelper.PHOTO_PREVIEW_DOMENSIONS;
import static eu.spod.isislab.spodapp.NewsfeedJSONHelper.PHOTO_PREVIEW_URL;
import static eu.spod.isislab.spodapp.NewsfeedJSONHelper.PHOTO_TITLE;
import static eu.spod.isislab.spodapp.NewsfeedJSONHelper.convertToIntArray;

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

    public ImageListPost(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds, JSONArray imageJsonArray, int count, int[] idList) throws JSONException {
        super(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);

        this.images = createImageList(imageJsonArray);
        this.count = count;
        this.idList = idList;
    }

    public List<JsonImage> createImageList(JSONArray array) throws JSONException {
        List<JsonImage> toReturn = new ArrayList<>(array.length());

        //JSONObject[] objectsArray = NewsfeedJSONHelper.convertToJsonObjectArray(array);

        for (int i = 0; i<array.length(); i++) {
            JSONObject imageObject = array.getJSONObject(i);

            int photoId = imageObject.getInt(PHOTO_ID);
            String title = imageObject.getString(PHOTO_TITLE);
            int[] dimensions = convertToIntArray(imageObject.getJSONArray(PHOTO_PREVIEW_DOMENSIONS));
            String previewUrl = imageObject.getString(PHOTO_PREVIEW_URL);

            toReturn.add(new JsonImage(photoId, title, dimensions, previewUrl));
        }

        return toReturn;
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
}
