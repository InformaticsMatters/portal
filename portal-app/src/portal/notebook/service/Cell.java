package portal.notebook.service;

import org.squonk.notebook.api.CellType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Cell implements Serializable {
    private Long id;
    private String name;
    private CellType cellType;
    private final Map<String, Binding> bindingMap = new HashMap<>();
    private final Map<String, Variable> outputVariableMap = new HashMap<>();
    private final Map<String, Option> optionMap = new HashMap<>();
    private int positionLeft;
    private int positionTop;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    public Map<String, Binding> getBindingMap() {
        return bindingMap;
    }

    public Map<String, Variable> getOutputVariableMap() {
        return outputVariableMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Option> getOptionMap() {
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
}
