package portal.notebook;

import portal.notebook.api.VariableInstance;
import portal.notebook.api.VariableType;

import java.io.Serializable;

public class VariableModel implements Serializable {
    private final CellModel producerCellModel;
    private final VariableInstance variable;

    public VariableModel(CellModel producerCellModel, VariableInstance variable) {
        this.producerCellModel = producerCellModel;
        this.variable = variable;
    }

    public VariableInstance getVariable() {
        return variable;
    }

    public String getName() {
        return variable.getName();
    }

    public Object getValue() {
        return variable.getValue();
    }

    public void setValue(Object value) {
        variable.setValue(value);
    }

    public VariableType getVariableType() {
        return variable.getVariableType();
    }

    public String getDisplayName() {
        return variable.getDisplayName();
    }

    public CellModel getProducerCellModel() {
        return producerCellModel;
    }

    public String toString() {
        return getProducerCellModel().getName() + "." + getName();
    }

}
