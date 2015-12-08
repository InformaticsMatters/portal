package portal.notebook;

import portal.notebook.service.Variable;
import tmp.squonk.notebook.api.VariableType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VariableModel implements Serializable {
    private final CellModel producerCellModel;
    private final Variable variable;
    private final List<VariableChangeListener> variableChangeListenerList = new ArrayList<>();

    public VariableModel(CellModel producerCellModel, Variable variable) {
        this.producerCellModel = producerCellModel;
        this.variable = variable;
    }

    public Variable getVariable() {
        return variable;
    }

    public String getName() {
        return variable.getName();
    }

    public Object getValue() {
        return variable.getValue();
    }

    public void setValue(Object value) {
        Object oldValue = variable.getValue();
        variable.setValue(value);
        if ((oldValue == null && value != null) || (oldValue != null && !oldValue.equals(value))) {
            notifyValueChanged(oldValue);
        }

    }

    private void notifyValueChanged(Object oldValue) {
        for (VariableChangeListener listener : variableChangeListenerList) {
            listener.onValueChanged(this, oldValue);
        }
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

    public void removeChangeListener(VariableChangeListener variableChangeListener) {
        variableChangeListenerList.remove(variableChangeListener);
    }

    public void addChangeListener(VariableChangeListener variableChangeListener) {
        variableChangeListenerList.add(variableChangeListener);
    }

    public String toString() {
        return getProducerCellModel().getName() + "." + getName();
    }

}
