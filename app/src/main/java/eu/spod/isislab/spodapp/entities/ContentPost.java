package eu.spod.isislab.spodapp.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;

public class ContentPost extends Post {

    private final static String ROUTE_NAME  = "routeName";
    private final static String VARS_PREFIX = "var#";

    private String title;
    private String description;
    private String url;
    private Map<String, String> routingInfo;

    private String imageUrl;
    private String thumbnailUrl;

    public ContentPost(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds, String title, String description, String url) {
        super(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);

        this.title = title;
        this.description = description;
        this.url = url;

        this.routingInfo = new HashMap<>();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRoutingInfo(Map<String, String> routingInfo) {
        this.routingInfo = routingInfo;
    }

    public void setRoutingInfo(JSONObject routingObject) throws JSONException {

        if(routingObject == null){
            if(routingInfo.size() > 0) {
                routingInfo.clear();
            }

            return;
        }

        String routeName = routingObject.getString(NewsfeedJSONHelper.ROUTE_NAME);
        JSONObject vars = routingObject.getJSONObject(NewsfeedJSONHelper.VARS);

        routingInfo.put(ROUTE_NAME, routeName);

        Iterator<String> varKeys = vars.keys();

        while (varKeys.hasNext()) {
            String varKey = varKeys.next();
            String varEntry = VARS_PREFIX + varKey;

            String varValue = vars.getString(varKey);

            routingInfo.put(varEntry, varValue);
        }
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean hasImage(){
        if(!hasValidThumbnailUrl() && !hasValidImageUrl()) {
            return false;
        }
        return true;
    }

    public boolean hasValidThumbnailUrl() {
        if(thumbnailUrl == null || thumbnailUrl.trim().isEmpty() || thumbnailUrl.contains("no-picture")) {
            return false;
        }
        return true;
    }

    public boolean hasValidImageUrl() {
        if(imageUrl == null || imageUrl.trim().isEmpty() || imageUrl.contains("no-picture")) {
            return false;
        }
        return true;
    }

    public boolean hasRouting() {
        return !routingInfo.isEmpty();
    }

    public String getLinkTitle() {
        return title;
    }

    public String getLinkDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getRouteName() {
        if(!hasRouting()) {
            return null;
        }

        return routingInfo.get(ROUTE_NAME);
    }

    public Map<String, String> getRoutingVars(){
        if(!hasRouting()) {
            return null;
        }

        Map<String, String> filteredMap = new HashMap<>(routingInfo.size() - 1); //only-vars Map has the same size of routingInfo - 1 for "routeName" entry
        Iterator<String> routingKeysIterator = routingInfo.keySet().iterator();

        while (routingKeysIterator.hasNext()) {
            String routingKey = routingKeysIterator.next();

            if (routingKey.startsWith(VARS_PREFIX)) {
                int varNameStartIndex = routingKey.indexOf("@") + 1;
                String varKey = routingKey.substring(varNameStartIndex);

                filteredMap.put(varKey, routingInfo.get(routingKey));
            }
        }

        return filteredMap;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Returns the preferred image url between <i> ThumbnailUrl </i> and <i> ImageUrl </i> according the following rule:
     * <br/>First the thumbnail (because we want a preview image), if not exist then the image
     *
     * @return the url of the image to display according the previous rule
     */
    public String getPreferredPreviewImageUrl() {
        if(!hasImage()) {
            return null;
        }

        if(hasValidThumbnailUrl()) {
            return thumbnailUrl;
        } else {
            return imageUrl;
        }
    }
}
