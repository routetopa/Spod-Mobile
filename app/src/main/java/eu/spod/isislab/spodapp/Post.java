package eu.spod.isislab.spodapp;

import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Post {

    public class User{
        private int userId;
        private String avatarUrl;
        private String name;
        private String label;
        private String labelColor;

        public User(int userId, String name, String src, String label, String labelColor) {
            this.userId = userId;
            this.avatarUrl = src;
            this.name = name;
            this.label = label;
            this.labelColor = labelColor;
        }

        public int getUserId() { return userId; }
        public String getAvatarUrl() { return avatarUrl; }
        public String getName() { return name; }
        public String getLabel() { return label; }
        public String getLabelColor() { return labelColor; }
    }

    //Base fields
    private int id;
    private String entityType;
    private int entityId;
    private String pluginKey;
    private long timestamp;
    private String format;
    private int userId;
    private int[] userIds;
    private JSONObject lastActivityRespond;

    private Boolean liked;
    private Integer likesCount;
    private Integer commentsCount;

    //Presentation fields
    private JSONObject context;
    private JSONObject features;
    private List<ContextActionMenuItem> contextActionMenu;
    private String string;
    private String line;
    private JSONObject extras;
    private String status;

    private SparseArray<User> usersInfo;

    public Post(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.pluginKey = pluginKey;
        this.timestamp = timestamp;
        this.format = format;
        this.userId = userId;
        this.userIds = userIds;
        this.usersInfo = new SparseArray<>();
        this.liked = null;

        this.context = null;
        this.features = null;
        this.contextActionMenu = new ArrayList<>();
        this.string = null;
        this.line = null;
        this.extras = null;
        this.status = null;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public void setPluginKey(String pluginKey) {
        this.pluginKey = pluginKey;
    }

    public void setTimestamp(long  timestamp) {
        this.timestamp = timestamp;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setLastActivityRespond(JSONObject lastActivityRespond) {
        this.lastActivityRespond = lastActivityRespond;
    }

    public void setUserIds(int[] userIds) {
        this.userIds = userIds;
    }

    public void setContext(JSONObject context) {
        this.context = context;
    }

    public void setFeatures(JSONObject features) {
        this.features = features;
    }

    public void setContextActionMenu(List<ContextActionMenuItem> contextActionMenu) {
        this.contextActionMenu = contextActionMenu;
    }

    public void addContextActionMenuItem(ContextActionMenuItem item) {
        contextActionMenu.add(item);
    }

    public void setString(String string) {
        this.string = string;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void setExtras(JSONObject extras) {
        this.extras = extras;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUsersInfo(SparseArray<User> usersInfo) {
        this.usersInfo = usersInfo;
    }

    public void setUsersInfo(JSONObject usersJson) throws JSONException {

        if(usersJson == null){
            return;
        }

        Iterator<String> keys = usersJson.keys();

        while(keys.hasNext()) {
            String key = keys.next();
            JSONObject userJson = usersJson.getJSONObject(key);
            int userId = userJson.getInt(NewsfeedJSONHelper.USER_ID);
            String avatarSrc = userJson.getString(NewsfeedJSONHelper.SRC);
            String name = userJson.getString(NewsfeedJSONHelper.TITLE);
            String label = userJson.getString(NewsfeedJSONHelper.LABEL);
            String labelColor = userJson.getString(NewsfeedJSONHelper.LABEL_COLOR);

            usersInfo.put(userId, new User(userId, name, avatarSrc, label, labelColor));
        }
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getPluginKey() {
        return pluginKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTimestampInMillis() {
        return timestamp * 1000;
    }

    public String getFormat() {
        return format;
    }

    public int getUserId() {
        return userId;
    }

    public boolean hasActivityRespond() {
        return lastActivityRespond != null;
    }

    public int getActivityUserId() {
        if(!hasActivityRespond()) {
            return -1;
        }

        return lastActivityRespond.optInt(NewsfeedJSONHelper.USER_ID);
    }

    public String getActivityString() {
        if(!hasActivityRespond()) {
            return null;
        }

        return lastActivityRespond.optString(NewsfeedJSONHelper.STRING);
    }

    public int[] getUserIds() {
        return userIds;
    }

    public JSONObject getContext() {
        return context;
    }

    public boolean hasContext() {
        try {
            return (context != null && context.has(NewsfeedJSONHelper.LABEL) && context.getString(NewsfeedJSONHelper.LABEL) != null);
        } catch (JSONException e) {
            return false;
        }
    }

    public String getContextLabel(){
        if(!hasContext()){
            return null;
        }

        try {
            return context.getString(NewsfeedJSONHelper.LABEL);
        } catch (JSONException e) {
            return null;
        }
    }

    public int getContextUserId() {
        if(!hasContext()) {
            return -1;
        }

        try {
            return context.getInt(NewsfeedJSONHelper.USER_ID);
        } catch (JSONException e) {
            return -1;
        }
    }

    public String getContextUrl() {
        if (!hasContext()) {
            return null;
        }

        try {
            return context.getString(NewsfeedJSONHelper.URL);
        } catch (JSONException e) {
            return null;
        }

    }

    public JSONObject getFeatures() {
        return features;
    }

    public boolean hasCommentsFeature(){
        return features != null && features.has(NewsfeedJSONHelper.COMMENTS);
    }

    public boolean isCommentable() {
        try {
            return hasCommentsFeature() && features.getJSONObject(NewsfeedJSONHelper.COMMENTS).getBoolean(NewsfeedJSONHelper.ALLOW);
        } catch (JSONException e) {
            return false;
        }
    }

    public int getCommentCount() {
        if(!hasCommentsFeature()){
            return -1;
        }

        if(commentsCount == null) {
            try {
                commentsCount = features.getJSONObject(NewsfeedJSONHelper.COMMENTS).getInt(NewsfeedJSONHelper.COUNT);
            } catch (JSONException ignore) {
            } //we can ignore the exception because if is commentable then it must contains this fields
        }

        return commentsCount;
    }

    public boolean hasLikeFeature(){
        return features != null && features.has(NewsfeedJSONHelper.LIKES);
    }

    public boolean isLikeable() {
        try {
            return hasLikeFeature() && features.getJSONObject(NewsfeedJSONHelper.LIKES).getBoolean(NewsfeedJSONHelper.ALLOW);
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean isLiked() {
        if(liked == null) {
            try {
                liked = features.getJSONObject(NewsfeedJSONHelper.LIKES).getBoolean(NewsfeedJSONHelper.SELF_LIKE);
            } catch (JSONException e) {
                liked = false;
            }
        }

        return liked;
    }

    public int getLikesCount() {
        if(!hasLikeFeature()){
            return -1;
        }
        if(likesCount == null) {
            try {
                likesCount = features.getJSONObject(NewsfeedJSONHelper.LIKES).getInt(NewsfeedJSONHelper.COUNT);
            } catch (JSONException ignore) {
            } //we can ignore the exception because if is likeable then it must contains this fields
        }

        return likesCount;
    }

    public List<ContextActionMenuItem> getContextActionMenu() {
        return contextActionMenu;
    }

    public ContextActionMenuItem getContextActionMenuItem(ContextActionMenuItem.ContextActionType actionType) {
        for (ContextActionMenuItem item : contextActionMenu) {
            if(actionType.equals(item.getActionType())) {
                return item;
            }
        }

        return null;
    }

    public String getString() {
        return string;
    }

    public String getLine() {
        return line;
    }

    public String getStatus() {
        return status;
    }

    public boolean hasStatus(){
        return status != null && status.trim().isEmpty();
    }

    public JSONObject getExtras() {
        return extras;
    }

    public User getUserInfo(int userId) {
        return usersInfo.get(userId);
    }

}
