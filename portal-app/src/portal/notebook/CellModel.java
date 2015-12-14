package portal.notebook;

import portal.notebook.service.Binding;
import portal.notebook.service.Cell;
import portal.notebook.service.Option;
import portal.notebook.service.Variable;
import tmp.squonk.notebook.api.CellType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CellModel implements Serializable {
    private final Cell cell;
    private NotebookModel notebookModel;
    private final Map<String, VariableModel> variableModelMap = new HashMap<>();
    private final Map<String, BindingModel> bindingModelMap = new HashMap<>();
    private final Map<String, OptionModel> optionModelMap = new HashMap<>();

    public CellModel(Cell cell, NotebookModel notebookModel) {
        this.cell = cell;
        this.notebookModel = notebookModel;
        loadOptions();
        loadOutputVariables();
    }

    public String getName() {
        return cell.getName();
    }

    public CellType getCellType() {
        return cell.getCellType();
    }

    public int getPositionLeft() {
        return cell.getPositionLeft();
    }

    public int getPositionTop() {
        return cell.getPositionTop();
    }

    public void setPositionTop(int positionTop) {
        cell.setPositionTop(positionTop);
    }

    public void setPositionLeft(int positionLeft) {
        cell.setPositionLeft(positionLeft);
    }

    private void loadOutputVariables() {
        for (Variable variable : cell.getOutputVariableMap().values()) {
            VariableModel variableModel = new VariableModel(this, variable);
            variableModelMap.put(variable.getName(), variableModel);
        }
    }

    public void loadBindings() {
        for (Binding binding : cell.getBindingMap().values()) {
            BindingModel bindingModel = new BindingModel(binding, notebookModel);
            bindingModelMap.put(binding.getName(), bindingModel);
        }

    }

    private void loadOptions() {
        for (Option option : cell.getOptionMap().values()) {
            OptionModel optionModel = new OptionModel(option);
            optionModelMap.put(option.getName(), optionModel);
        }
    }


    public VariableModel findVariableModel(String name) {
        return variableModelMap.get(name);
    }

    public Map<String, VariableModel> getOutputVariableModelMap() {
        return variableModelMap;
    }

    public Map<String, BindingModel> getBindingModelMap() {
        return bindingModelMap;
    }

    public Map<String, OptionModel> getOptionModelMap() {
        return optionModelMap;
    }

    public Long getId() {
        return cell.getId();
    }
}
