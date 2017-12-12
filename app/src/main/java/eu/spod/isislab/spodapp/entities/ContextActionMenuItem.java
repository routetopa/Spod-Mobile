package eu.spod.isislab.spodapp.entities;

import java.util.List;
import java.util.Map;

public class ContextActionMenuItem {


    public enum ContextActionType {
        DELETE_COMMENT,
        DELETE_POST,
        FLAG_CONTENT
    }

    private ContextActionType actionType;
    private String label;
    private Map<String, String> params;


    public ContextActionMenuItem(String actionType, String label, Map<String, String> params) {
        this.actionType = ContextActionType.valueOf(actionType.toUpperCase());
        this.label = label;
        this.params = params;
    }

    public ContextActionType getActionType() { return actionType; }
    public String getLabel() { return label; }
    public Map<String, String> getParams() { return params; }
}
