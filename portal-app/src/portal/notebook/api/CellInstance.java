package portal.notebook.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CellInstance implements Serializable {

    private final Map<String, BindingInstance> bindingMap = new HashMap<>();
    private final Map<String, VariableInstance> outputVariableMap = new HashMap<>();
    private final Map<String, OptionInstance> optionMap = new HashMap<>();
    private Long id;
    private String name;
    private CellDefinition cellDefinition;
    private int positionLeft;
    private int positionTop;
    private int sizeWidth;
    private int sizeHeight;
    private transient boolean dirty = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellDefinition getCellDefinition() {
        return cellDefinition;
    }

    public void setCellDefinition(CellDefinition cellDefinition) {
        this.cellDefinition = cellDefinition;
    }

    public Map<String, BindingInstance> getBindingMap() {
        return bindingMap;
    }

    public Map<String, VariableInstance> getOutputVariableMap() {
        return outputVariableMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, OptionInstance> getOptionMap() {
        return optionMap;
    }

    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int positionLeft) {
        dirty = true;
        this.positionLeft = positionLeft;
    }

    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int positionTop) {
        dirty = true;
        this.positionTop = positionTop;
    }

    public int getSizeWidth() {
        return sizeWidth;
    }

    public void setSizeWidth(int sizeWidth) {
        dirty = true;
        this.sizeWidth = sizeWidth;
    }

    public int getSizeHeight() {
        return sizeHeight;
    }

    public void setSizeHeight(int sizeHeight) {
        dirty = true;
        this.sizeHeight = sizeHeight;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void applyChangesFrom(CellInstance cellInstance) {
        if (cellInstance.isDirty()) {
            setPositionLeft(cellInstance.getPositionLeft());
            setPositionTop(cellInstance.getPositionTop());
            setSizeHeight(cellInstance.getSizeHeight());
            setSizeWidth(cellInstance.getSizeWidth());
        }
        for (OptionInstance optionInstance : cellInstance.getOptionMap().values()) {
            if (optionInstance.isDirty()) {
                optionMap.get(optionInstance.getName()).setValue(optionInstance.getValue());
            }
        }
        for (VariableInstance variableInstance : cellInstance.getOutputVariableMap().values()) {
            if (variableInstance.isDirty()) {
                outputVariableMap.get(variableInstance.getName()).setValue(variableInstance.getValue());
            }
        }
        for (BindingInstance bindingInstance : cellInstance.getBindingMap().values()) {
            if (bindingInstance.isDirty()) {
                if (bindingInstance.getVariable() == null) {
                    bindingMap.get(bindingInstance.getName()).setVariable(null);
                } else {
                    VariableInstance variableInstance = outputVariableMap.get(bindingInstance.getVariable().getName());
                    bindingMap.get(bindingInstance.getName()).setVariable(variableInstance);
                }
            }
        }
    }
}
