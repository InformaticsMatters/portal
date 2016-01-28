package portal.notebook.api;

import java.io.Serializable;

public class VariableInstance implements Serializable {
    private CellInstance producerCell;
    private String name;
    private String displayName;
    private VariableType variableType;
    private Object value;

    public CellInstance getProducerCell() {
        return producerCell;
    }

    public void setProducerCell(CellInstance producerCell) {
        this.producerCell = producerCell;
    }

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
}
