package portal.notebook;

import toolkit.services.AbstractEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Cell extends AbstractEntity {
    private Long id;
    private String name;
    private CellType cellType;
    private final List<Variable> inputVariableList = new ArrayList<>();
    private final List<Variable> outputVariableList = new ArrayList<>();
    private final Map<String, Object> propertyMap = new HashMap<>();
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

    public List<Variable> getInputVariableList() {
        return inputVariableList;
    }

    public List<Variable> getOutputVariableList() {
        return outputVariableList;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
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
