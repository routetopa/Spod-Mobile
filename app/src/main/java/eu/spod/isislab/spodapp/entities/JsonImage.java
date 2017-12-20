package eu.spod.isislab.spodapp.entities;

import java.io.Serializable;

public class JsonImage implements Serializable {
    private String id;
    private String description;
    private int[] previewDimensions;
    private String previewUrl;

    public JsonImage(String id, String description, int[] dimensions, String previewUrl) {
        this.id = id;
        this.description = description;
        this.previewDimensions = dimensions;
        this.previewUrl = previewUrl;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
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

    public int[] getPreviewDimensions() {
        return previewDimensions;
    }
}
