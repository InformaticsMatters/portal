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
        this.positionLeft = positionLeft;
    }

    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int positionTop) {
        this.positionTop = positionTop;
    }

    public int getSizeWidth() {
        return sizeWidth;
    }

    public void setSizeWidth(int sizeWidth) {
        this.sizeWidth = sizeWidth;
    }

    public int getSizeHeight() {
        return sizeHeight;
    }

    public void setSizeHeight(int sizeHeight) {
        this.sizeHeight = sizeHeight;
    }
}
