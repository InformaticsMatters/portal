package portal.notebook;

import portal.notebook.service.*;
import tmp.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCellModel implements CellModel {
    private final CellType cellType;
    private final Map<String, BindingModel> bindingModelMap = new HashMap<>();
    private final List<String> outputVariableNameList = new ArrayList<>();
    private final Map<String, OptionModel> optionMap = new HashMap<>();
    private String name;
    private int positionLeft;
    private int positionTop;

    public DefaultCellModel(CellType cellType) {
        this.cellType = cellType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int x) {
        this.positionLeft = x;
    }

    @Override
    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int y) {
        this.positionTop = y;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }


    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        storeHeader(cell);
        storeBindingModels(notebookContents, cell);
        storeOptions(cell);
    }


    protected void storeOptions(Cell cell) {
        for (OptionModel optionModel : optionMap.values()) {
            cell.getOptionMap().get(optionModel.getName()).setValue(optionModel.getValue());
        }
    }

    protected void storeHeader(Cell cell) {
        cell.setCellType(getCellType());
        cell.setPositionLeft(getPositionLeft());
        cell.setPositionTop(getPositionTop());
        cell.setName(getName());
    }

    protected void storeBindingModels(NotebookContents notebookContents, Cell cell) {
        for (BindingModel bindingModel : getBindingModelMap
                ().values()) {
            Binding binding = cell.getBindingMap().get(bindingModel.getName());
            VariableModel variableModel = bindingModel.getSourceVariableModel();
            Variable variable = variableModel == null ? null : notebookContents.findVariable(variableModel.getProducer().getName(), variableModel.getName());
            binding.setVariable(variable);
        }
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        loadBindings(notebookModel, cell);
        loadOutputVariables(cell);
        loadOptions(cell);
    }

    private void loadOptions(Cell cell) {
        optionMap.clear();
        for (Option option : cell.getOptionMap().values()) {
            OptionModel optionModel = new OptionModel();
            optionModel.setOptionType(option.getOptionType());
            optionModel.setName(option.getName());
            optionModel.setValue(option.getValue());
            optionModel.getPicklistValueList().addAll(option.getPicklistValueList());
            optionMap.put(option.getName(), optionModel);
        }
    }

    protected void loadHeader(Cell cell) {
        setName(cell.getName());
        positionLeft = cell.getPositionLeft();
        positionTop = cell.getPositionTop();
    }

    protected void loadOutputVariables(Cell cell) {
        getOutputVariableNameList().clear();
        for (Variable variable : cell.getOutputVariableMap().values()) {
            getOutputVariableNameList().add(variable.getName());
        }
    }

    protected void loadBindings(NotebookModel notebookModel, Cell cell) {
        bindingModelMap.clear();
        for (Binding binding : cell.getBindingMap().values()) {
            BindingModel bindingModel = new BindingModel();
            bindingModel.getAcceptedVariableTypeList().addAll(binding.getAcceptedVariableTypeList());
            bindingModel.setDisplayName(binding.getDisplayName());
            bindingModel.setName(binding.getName());
            Variable variable = binding.getVariable();
            VariableModel variableModel = variable == null ? null : notebookModel.findVariableModel(variable.getProducerCell().getName(), variable.getName());
            bindingModel.setSourceVariableModel(variableModel);
            bindingModelMap.put(binding.getName(), bindingModel);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        return ((CellModel)o).getName().equals(getName());
    }

    @Override
    public CellType getCellType() {
        return cellType;
    }

    @Override
    public Map<String, BindingModel> getBindingModelMap() {
        return bindingModelMap;
    }

    @Override
    public Map<String, OptionModel> getOptionMap() {
        return optionMap;
    }
}
