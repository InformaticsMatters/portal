package portal.notebook;

import com.squonk.notebook.api.CellType;
import portal.notebook.service.Binding;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCellModel implements CellModel {
    private final CellType cellType;
    private final List<BindingModel> bindingModelList = new ArrayList<>();
    private final List<String> outputVariableNameList = new ArrayList<>();
    private final Map<String, Object> optionMap = new HashMap<>();
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
        cell.getOptionMap().putAll(optionMap);
    }


    protected void storeHeader(Cell cell) {
        cell.setCellType(getCellType());
        cell.setPositionLeft(getPositionLeft());
        cell.setPositionTop(getPositionTop());
        cell.setName(getName());
    }

    protected void storeBindingModels(NotebookContents notebookContents, Cell cell) {
        for (BindingModel bindingModel : getBindingModelList()) {
            Binding binding = new Binding();
            binding.setName(bindingModel.getName());
            binding.setDisplayName(bindingModel.getDisplayName());
            binding.setVariableType(bindingModel.getVariableType());
            VariableModel variableModel = bindingModel.getSourceVariableModel();
            Variable variable = variableModel == null ? null : notebookContents.findVariable(variableModel.getProducer().getName(), variableModel.getName());
            variable.setValue(variableModel.getValue());
            binding.setVariable(variable);
            cell.getBindingList().add(binding);
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
        optionMap.putAll(cell.getOptionMap());
    }

    protected void loadHeader(Cell cell) {
        setName(cell.getName());
        positionLeft = cell.getPositionLeft();
        positionTop = cell.getPositionTop();
    }

    protected void loadOutputVariables(Cell cell) {
        getOutputVariableNameList().clear();
        for (Variable variable : cell.getOutputVariableList())  {
            getOutputVariableNameList().add(variable.getName());
        }
    }

    protected void loadBindings(NotebookModel notebookModel, Cell cell) {
        bindingModelList.clear();
        for (Binding binding : cell.getBindingList()) {
            BindingModel bindingModel = new BindingModel();
            bindingModel.setVariableType(binding.getVariableType());
            bindingModel.setDisplayName(binding.getDisplayName());
            bindingModel.setName(binding.getName());
            Variable variable = binding.getVariable();
            VariableModel variableModel = variable == null ? null : notebookModel.findVariableModel(variable.getProducerCell().getName(), variable.getName());
            bindingModel.setSourceVariableModel(variableModel);
            bindingModelList.add(bindingModel);
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
    public List<BindingModel> getBindingModelList() {
        return bindingModelList;
    }

    @Override
    public Map<String, Object> getOptionMap() {
        return optionMap;
    }
}
