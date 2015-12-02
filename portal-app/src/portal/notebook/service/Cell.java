package portal.notebook.service;

import com.squonk.notebook.api.CellType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Cell implements Serializable {
    private Long id;
    private String name;
    private CellType cellType;
    private final List<Binding> bindingList = new ArrayList<>();
    private final List<Variable> outputVariableList = new ArrayList<>();
    private final Map<String, Object> optionMap = new HashMap<>();
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

    public List<Binding> getBindingList() {
        return bindingList;
    }

    public List<Variable> getOutputVariableList() {
        return outputVariableList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getOptionMap() {
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
