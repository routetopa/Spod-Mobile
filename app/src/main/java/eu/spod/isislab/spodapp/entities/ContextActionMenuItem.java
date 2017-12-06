package eu.spod.isislab.spodapp.entities;

import java.util.List;
import java.util.Map;

public class ContextActionMenuItem {


    public enum ContextActionType {
        DELETE,
        FLAG;
    }
    private ContextActionType actionType;

    private String label;
    private String actionUrl;
    private Map<String, String> params;
    private String paramOptionsTarget;
    private List<String> options;
    public ContextActionMenuItem(String actionType, String label, String actionUrl, Map<String, String> params) {
        this.actionType = ContextActionType.valueOf(actionType.toUpperCase());
        this.label = label;
        this.actionUrl = actionUrl;
        this.params = params;
    }

    public void setOptions(String target, List<String> options) {
        this.paramOptionsTarget = target;
        this.options = options;
    }

    public ContextActionType getActionType() { return actionType; }

    public String getLabel() { return label; }
    public String getActionUrl() { return actionUrl; }
    public Map<String, String> getParams() { return params; }
    public String getParamOptionsTarget() { return paramOptionsTarget; }
    public List<String> getOptions() { return options; }
}
