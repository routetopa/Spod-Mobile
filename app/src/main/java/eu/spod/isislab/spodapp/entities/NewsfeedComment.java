package eu.spod.isislab.spodapp.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.spod.isislab.spodapp.utils.NewsfeedJSONHelper;

public class NewsfeedComment {
    private int id;
    private int commentEntityId;
    private String userDisplayName;
    private String avatarUrl;
    private int userId;
    private long time;
    private String message;
    private Map<String, String> attachment;
    private List<ContextActionMenuItem> contextActionMenu;

    public NewsfeedComment(int id, int commentEntityId, String userDisplayName, String avatarUrl, int userId, long time, String message) {
        this.id = id;
        this.commentEntityId = commentEntityId;
        this.userDisplayName = userDisplayName;
        this.avatarUrl = avatarUrl;
        this.userId = userId;
        this.time = time;
        this.message = message;
        this.attachment = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommentEntityId() {
        return commentEntityId;
    }

    public void setCommentEntityId(int commentEntityId) {
        this.commentEntityId = commentEntityId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public long getTimeInMillis() {
        return time * 1000; //oxwall save the timestamp in seconds
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }

    public List<ContextActionMenuItem> getContextActionMenu() {
        return contextActionMenu;
    }

    public void setContextActionMenu(List<ContextActionMenuItem> contextActionMenu) {
        this.contextActionMenu = contextActionMenu;
    }

    public ContextActionMenuItem getContextActionMenuItem(ContextActionMenuItem.ContextActionType actionType) {
        for (ContextActionMenuItem item : contextActionMenu) {
            if(actionType.equals(item.getActionType())) {
                return item;
            }
        }

        return null;
    }

    public boolean hasAttachment() {
        return attachment != null;
    }

    public boolean hasLinkImage() {
        if(!hasAttachment() || !"link".equals(getAttachmentType())) {
            return false;
        }

        return attachment.get("thumbnail_url") != null;
    }


    public String getAttachmentType() {
        //If comment haven't 'type' filed we try an heuristic way to find it
        if(!attachment.containsKey("type")) {
            if(attachment.containsKey("thumbnail_url")) {
                attachment.put("type", "link");
            } else if(attachment.containsKey("url") && attachment.containsKey("href")) {
                attachment.put("type", "photo");
            }
        }

        return attachment.get("type");
    }
}
