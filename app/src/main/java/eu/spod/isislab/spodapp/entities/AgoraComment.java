package eu.spod.isislab.spodapp.entities;

public class AgoraComment {

    String id;
    String roomId;
    String ownerId;
    String comment;
    String level;
    String sentiment;
    String timestamp;
    String total_comment;
    String username;
    String avatar_url;
    String datalet_id;

    public AgoraComment(String id, String roomId, String ownerId, String comment, String level, String sentiment, String timestamp, String total_comment, String username, String avatar_url, String datalet_id) {
        this.id = id;
        this.roomId        = roomId;
        this.ownerId       = ownerId;
        this.comment       = comment;
        this.level         = level;
        this.sentiment     = sentiment;
        this.timestamp     = timestamp;
        this.total_comment = total_comment;
        this.username      = username;
        this.avatar_url    = avatar_url;
        this.datalet_id = datalet_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTotal_comment() {
        return total_comment;
    }

    public void setTotal_comment(String total_comment) {
        this.total_comment = total_comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getDatalet_id() {
        return datalet_id;
    }

    public void setDatalet_id(String datalet_id) {
        this.datalet_id = datalet_id;
    }
}
