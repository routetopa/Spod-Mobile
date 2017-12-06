package eu.spod.isislab.spodapp;

import java.io.Serializable;

public class JsonImage implements Serializable {
    private int id;
    private String title;
    private int[] previewDimensions;
    private String previewUrl;

    public JsonImage(int id, String title, int[] dimensions, String previewUrl) {
        this.id = id;
        this.title = title;
        this.previewDimensions = dimensions;
        this.previewUrl = previewUrl;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPreviewWidth() {
        return previewDimensions[0];
    }

    public int getPreviewHeight() {
        return previewDimensions[1];
    }

    public String getPreviewUrl() {
        return previewUrl;
    }
}
