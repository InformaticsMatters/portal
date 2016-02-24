package portal.notebook.api;

import java.io.Serializable;

public class VariableInstance implements Serializable {
    private Long cellId;
    private VariableDefinition variableDefinition;
    private Object value;
    private boolean dirty = false;

    public String getName() {
        return variableDefinition.getName();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        dirty = true;
        this.value = value;
    }

    public VariableType getVariableType() {
        return variableDefinition.getVariableType();
    }

    public String getDisplayName() {
        return variableDefinition.getDisplayName();
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

    public void resetDirty() {
        dirty = false;
    }

    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    public void setVariableDefinition(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }


}
