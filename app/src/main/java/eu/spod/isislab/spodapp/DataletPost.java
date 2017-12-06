package eu.spod.isislab.spodapp;



public class DataletPost extends Post{

    private int dataletId;
    private String url;
    private String previewImage;

    public DataletPost(int id, String entityType, int entityId, String pluginKey, long timestamp, String format, int userId, int[] userIds, int dataletId, String url, String previewImage) {
        super(id, entityType, entityId, pluginKey, timestamp, format, userId, userIds);
        this.dataletId = dataletId;
        this.url = url;

        this.previewImage = previewImage;
    }

    public int getDataletId() {
        return dataletId;
    }

    public String getUrl() {
        return url;
    }

    public String getPreviewImage() {
        return previewImage;
    }
}
