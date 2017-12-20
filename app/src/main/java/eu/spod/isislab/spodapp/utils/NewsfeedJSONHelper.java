package eu.spod.isislab.spodapp.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.spod.isislab.spodapp.entities.ContentPost;
import eu.spod.isislab.spodapp.entities.ContextActionMenuItem;
import eu.spod.isislab.spodapp.entities.DataletPost;
import eu.spod.isislab.spodapp.entities.ImageListPost;
import eu.spod.isislab.spodapp.entities.ImagePost;
import eu.spod.isislab.spodapp.entities.JsonImage;
import eu.spod.isislab.spodapp.entities.NewsfeedComment;
import eu.spod.isislab.spodapp.entities.NewsfeedImageInfo;
import eu.spod.isislab.spodapp.entities.NewsfeedLike;
import eu.spod.isislab.spodapp.entities.Post;

public class NewsfeedJSONHelper {

    /*POST KEYS*/
    //Base keys
    public static final String ID                           = "id";
    public static final String ENTITY_ID                    = "entityId";
    public static final String ENTITY_TYPE                  = "entityType";
    public static final String PLUGIN_KEY                   = "pluginKey";
    public static final String TIME                         = "time";
    public static final String FORMAT                       = "format";
    public static final String CONTENT                      = "content";
    public static final String CONTEXT_ACTION_MENU          = "contextActionMenu";
    public static final String STRING                       = "string";
    public static final String LINE                         = "line";
    public static final String DATA_EXTRAS                  = "dataExtras";
    public static final String LAST_ACTIVITY_RESPOND        = "lastActivityRespond";
    public static final String USERS_INFO                   = "usersInfo";
    public static final String CONTEXT                      = "context";

    //common keys
    public static final String SRC                          = "src";
    public static final String LABEL                        = "label";
    public static final String URL                          = "url";
    public static final String ROUTE_NAME                   = "routeName";
    public static final String VARS                         = "vars";
    public static final String CONTENT_STATUS               = "status";

    //User keys
    public static final String USER_ID                      = "userId";
    public static final String USER_IDS                     = "userIds";
    public static final String LABEL_COLOR                  = "labelColor";

    //format 'image_content' keys
    public static final String IMAGE                        = "image";
    public static final String THUMBNAIL                    = "thumbnail";
    public static final String TITLE                        = "title";
    public static final String DESCRIPTION                  = "description";
    public static final String URL_ENCODED                  = "urlEncoded";

    //format 'image' and 'image_list' keys
    public static final String PHOTO_ID                     = "photoId";
    public static final String PHOTO_DESCRIPTION            = "photoDescription";
    public static final String PHOTO_PREVIEW_DOMENSIONS     = "photoPreviewDimensions";
    public static final String PHOTO_URL                    = "photoUrl";
    public static final String PHOTO_PREVIEW_URL            = "photoPreviewUrl";
    private static final String PHOTO_LIST                  = "list";
    private static final String ID_LIST                     = "idList";

    //format 'datalet' keys
    private static final String DATALET_ID                  = "dataletId";
    private static final String PREVIEW_IMAGE               = "previewImage";

    //features keys
    public static final String FEATURES                     = "features";
    public static final String COMMENTS                     = "comments";
    public static final String LIKES                        = "likes";
    public static final String ALLOW                        = "allow";
    public static final String COUNT                        = "count";
    public static final String SELF_LIKE                    = "selfLike";

    //Like/Unlike response keys
    public static final String LIKE_COUNT                   = "likeCount";


    //ActionMenu keys
    public static final String ACTION_TYPE                  = "actionType";
    public static final String PARAMS                       = "params";

    /*COMMENT KEYS*/
    public static final String COMMENT_ENTITY_ID            = "commentEntityId";
    public static final String CREATESTAMP                  = "createStamp";
    public static final java.lang.String ATTACHMENT         = "attachment";
    public static final String USER_DISPLAY_NAME            = "userDisplayName";
    public static final String AVATAR_URL                   = "avatarUrl";

    /*ERROR HANDLING KEYS*/
    public static final String RESULT                       = "result";
    public static final String MESSAGE                      = "message";
    public static final String STATUS                       = "status";
    public static final String ERROR_MESSAGE                = "error_message";
    public static final String KO                           = "ko";

    /*OTHER KEYS*/
    public static final String TIMESTAMP                    = "timestamp";
    private static final String ALBUM_NAME                  = "albumName";
    private static final String ALBUM_ID                    = "albumId";

    /*AUTHORIZATION KEYS*/
    public static final String WRITE                        = "write";
    public static final String VIEW                         = "view";

    /*CONTENT LINK KEYS*/
    public static final String TYPE                         = "type";
    public static final String PHOTO                        = "photo";
    public static final String HREF                         = "href";
    public static final String LINK                         = "link" ;
    public static final String THUMBNAIL_URL                = "thumbnail_url";
    public static final String ALL_IMAGES                   = "allImages" ;

    public static class Formats {

        public static final String FORMAT_EMPTY = "empty";
        public static final String FORMAT_TEXT = "text";
        public static final String FORMAT_CONTENT = "content";
        public static final String FORMAT_CONTENT_IMAGE = "image_content";
        public static final String FORMAT_IMAGE = "image";
        public static final String FORMAT_IMAGE_LIST = "image_list";
        public static final String FORMAT_DATALET = "datalet";
    }
    public static Post createPost(JSONObject jsonPost) throws JSONException {
        int id = jsonPost.getInt(ID);
        int entityId = jsonPost.getInt(ENTITY_ID);
        String entityType = jsonPost.getString(ENTITY_TYPE);
        String pluginKey = jsonPost.getString(PLUGIN_KEY);
        long timestamp = jsonPost.getLong(TIME);
        String format = jsonPost.getString(FORMAT);
        int userId = jsonPost.getInt(USER_ID);
        JSONArray usersArray = jsonPost.getJSONArray(USER_IDS);

        int[] userIds = convertToIntArray(usersArray);

        JSONObject lastActivityRespond = jsonPost.optJSONObject(LAST_ACTIVITY_RESPOND);

        JSONObject usersInfo = jsonPost.optJSONObject(USERS_INFO);

        JSONObject context = jsonPost.optJSONObject(CONTEXT);

        JSONObject features = jsonPost.optJSONObject(FEATURES);
        String string = optString(jsonPost, STRING);
        String line = optString(jsonPost, LINE);
        JSONObject extras = jsonPost.optJSONObject(DATA_EXTRAS);


        JSONObject content = jsonPost.optJSONObject(CONTENT);
        Post p;

        switch (format) {
            case Formats.FORMAT_IMAGE:
                JsonImage image = createJsonImage(content);
                p = new ImagePost(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds, image);
                break;
            case Formats.FORMAT_IMAGE_LIST:
                JSONArray imagesJsonArray = content.getJSONArray(PHOTO_LIST);
                List<JsonImage> imageList = createJsonImageList(imagesJsonArray);
                int count = content.getInt(COUNT);
                int[] ids = convertToIntArray(content.getJSONArray(ID_LIST));
                p = new ImageListPost(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds, imageList, count, ids);
                break;
            case Formats.FORMAT_CONTENT_IMAGE:
            case Formats.FORMAT_CONTENT:
                String title = content.getString(TITLE);
                String description = optString(content, DESCRIPTION);
                String url = content.getString(URL);
                JSONObject encodedUrl = content.optJSONObject(URL_ENCODED);
                String thumbnailUrl = content.optString(THUMBNAIL);
                String imageUrl = content.optString(IMAGE);

                ContentPost contentPost = new ContentPost(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds, title, description, url);
                contentPost.setRoutingInfo(encodedUrl);
                contentPost.setImageUrl(imageUrl);
                contentPost.setThumbnailUrl(thumbnailUrl);
                p = contentPost;
                break;
            case Formats.FORMAT_DATALET:
                String dataletUrl = content.getString(URL);
                String dataletPreviewImage = content.getString(PREVIEW_IMAGE);
                int dataletId = content.getInt(DATALET_ID);
                p = new DataletPost(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds, dataletId, dataletUrl, dataletPreviewImage);
                break;
            default:
                p = new Post(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);  //'text' format is included in default because the status is set in all cases outside the switch
                break;
        }

        JSONArray contextActionMenu = jsonPost.optJSONArray(CONTEXT_ACTION_MENU);

        List<ContextActionMenuItem> contextActionMenuItems = generateContextActionMenu(contextActionMenu);
        p.setContextActionMenu(contextActionMenuItems);

        //Set all the optional fields
        if (content != null) {
            String status = optString(content, CONTENT_STATUS);
            p.setStatus(status);
        }

        p.setLastActivityRespond(lastActivityRespond);
        p.setContext(context);
        p.setFeatures(features);
        p.setString(string);
        p.setLine(line);
        p.setExtras(extras);
        p.setUsersInfo(usersInfo);

        return p;
    }

    private static JsonImage createJsonImage(JSONObject imageObject) throws JSONException {

            String photoId = optString(imageObject, PHOTO_ID);
            String title = optString(imageObject, PHOTO_DESCRIPTION);
            int[] dimensions = convertToIntArray(imageObject.getJSONArray(PHOTO_PREVIEW_DOMENSIONS));
            String previewUrl = imageObject.getString(PHOTO_PREVIEW_URL);

            return new JsonImage(photoId, title, dimensions, previewUrl);

    }

    public static List<JsonImage> createJsonImageList(JSONArray array) throws JSONException {
        List<JsonImage> toReturn = new ArrayList<>(array.length());

        for (int i = 0; i<array.length(); i++) {
            JSONObject imageObject = array.getJSONObject(i);
            toReturn.add(createJsonImage(imageObject));
        }

        return toReturn;
    }

    public static NewsfeedComment createComment(JSONObject comment) throws JSONException {
        int id = comment.getInt(ID);
        int commentEntityId = comment.getInt(COMMENT_ENTITY_ID);
        int userId = comment.getInt(USER_ID);
        long time = comment.getLong(CREATESTAMP);
        String message = comment.getString(MESSAGE);
        String attachmentString = optString(comment, ATTACHMENT);
        String userName = comment.getString(USER_DISPLAY_NAME);
        String avatarUrl = comment.getString(AVATAR_URL);

        NewsfeedComment c = new NewsfeedComment(id, commentEntityId, userName, avatarUrl, userId, time, message);

        if(attachmentString != null) {
            c.setAttachment(convertToMap(new JSONObject(attachmentString)));
        }

        JSONArray contextActionMenu = comment.optJSONArray(CONTEXT_ACTION_MENU);

        List<ContextActionMenuItem> contextActionMenuItems = generateContextActionMenu(contextActionMenu);
        c.setContextActionMenu(contextActionMenuItems);

        return c;
    }

    private static List<ContextActionMenuItem> generateContextActionMenu(JSONArray contextActionMenu) throws JSONException {
        List<ContextActionMenuItem> items = new ArrayList<>(contextActionMenu.length());

        for (int i=0; i<contextActionMenu.length(); i++) {
            JSONObject item = contextActionMenu.getJSONObject(i);
            String label = item.getString(LABEL);
            String actionType = item.getString(ACTION_TYPE);
            Map<String, String> params = convertToMap(item.getJSONObject(PARAMS));

            ContextActionMenuItem menuItem = new ContextActionMenuItem(actionType, label, params);
            items.add(menuItem);
        }

        return items;
    }


    public static ArrayList<NewsfeedComment> createCommentsList(JSONArray comments) throws JSONException {
        int length = comments.length();
        ArrayList<NewsfeedComment> commentsList = new ArrayList<>(length);

        for (int i = 0; i < length; i++){
            commentsList.add(createComment(comments.getJSONObject(i)));
        }

        return commentsList;
    }

    public static NewsfeedLike createLike(JSONObject like) throws JSONException {
        String displayName = like.getString(USER_DISPLAY_NAME);
        String avatarUrl = like.getString(AVATAR_URL);
        long timestamp = like.getLong(TIMESTAMP);
        int userId = like.getInt(USER_ID);

        return new NewsfeedLike(userId, displayName, avatarUrl, timestamp);
    }

    public static ArrayList<NewsfeedLike> createLikesList(JSONArray likes) throws JSONException {
        int length = likes.length();
        ArrayList<NewsfeedLike> commentsList = new ArrayList<>(length);

        for (int i = 0; i < length; i++){
            commentsList.add(createLike(likes.getJSONObject(i)));
        }

        return commentsList;
    }

    public static NewsfeedImageInfo createImageInfo(JSONObject jsonImage) throws JSONException {
        String id = optString(jsonImage, ID);
        String description = optString(jsonImage, DESCRIPTION);
        long time = jsonImage.getLong(TIME);
        String userName = optString(jsonImage, USER_DISPLAY_NAME);
        int userId = jsonImage.optInt(USER_ID);
        String albumName = optString(jsonImage, ALBUM_NAME);
        int albumId = jsonImage.optInt(ALBUM_ID);
        String url = jsonImage.getString(URL);

        return new NewsfeedImageInfo(id, description, time, userName, userId, albumName, albumId, url);
    }

    public static List<NewsfeedImageInfo> createImageInfoList(JSONArray imageArray) throws JSONException {
        int length = imageArray.length();
        ArrayList<NewsfeedImageInfo> imageList = new ArrayList<>(length);

        for (int i = 0; i < length; i++){
            imageList.add(createImageInfo(imageArray.getJSONObject(i)));
        }

        return imageList;
    }

    public static Map<String, String> convertToMap(JSONObject object) throws JSONException {
        Map<String, String> toReturn = new HashMap<>(object.length());
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = object.isNull(key) ? null : String.valueOf(object.get(key));
            toReturn.put(key, value);
        }
        return toReturn;
    }

    public static int[] convertToIntArray(JSONArray array) throws JSONException {
        int[] toReturn = new int[array.length()];

        for(int i = 0; i<array.length(); i++) {
            toReturn[i] = array.getInt(i);
        }

        return toReturn;
    }


    public static String[] convertToStringArray(JSONArray array) throws JSONException {
        String[] toReturn = new String[array.length()];

        for(int i = 0; i<array.length(); i++) {
            toReturn[i] = array.getString(i);
        }

        return toReturn;
    }

    /** Return the value mapped by the given key, or {@code null} if not present or null. */
    private static String optString(JSONObject json, String key)
    {
        // http://code.google.com/p/android/issues/detail?id=13830
        if (json.isNull(key))
            return null;
        else
            return json.optString(key, null);
    }

    /**
     * Check if input string is a valid json object and have an error message
     * @param jsonObjectString a json encoded string
     * @return Error message if exists otherwise return null
     */
    public static String getErrorMessage(String jsonObjectString) {
        try {
            JSONObject object = new JSONObject(jsonObjectString);
            if(object.has(RESULT)) {
                boolean result = object.getBoolean(RESULT);

                if (!result) {
                    return object.getString(MESSAGE);
                }
            }

            if(object.has(STATUS)) {
                String status = optString(object, STATUS);

                if(KO.equals(status)) {
                    return optString(object, ERROR_MESSAGE);
                }
            }

            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    public static boolean isAuthorized(JSONObject authObject, String authKey) throws JSONException {
        JSONObject auth = null;
        try {
            auth = authObject.getJSONObject(authKey);
            return auth.getBoolean(RESULT);
        } catch (JSONException e) {
            throw new JSONException("Authorization object invalid");
        }
    }
}
