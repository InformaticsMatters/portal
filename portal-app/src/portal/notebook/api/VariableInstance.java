package portal.notebook.api;

import java.io.Serializable;

public class VariableInstance implements Serializable {
    private Long cellId;
    private String name;
    private String displayName;
    private VariableType variableType;
    private Object value;
    private transient boolean dirty = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        dirty = true;
        this.value = value;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    public boolean isDirty() {
        return dirty;
    }
}
