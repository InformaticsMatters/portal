package portal.notebook;

import portal.notebook.api.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CellModel implements Serializable {

    private final CellInstance cell;
    private final Map<String, VariableModel> variableModelMap = new HashMap<>();
    private final Map<String, BindingModel> bindingModelMap = new HashMap<>();
    private final Map<String, OptionModel> optionModelMap = new HashMap<>();
    private NotebookModel notebookModel;

    public CellModel(CellInstance cell, NotebookModel notebookModel) {
        this.cell = cell;
        this.notebookModel = notebookModel;
        loadOptions();
        loadOutputVariables();
    }

    public String getName() {
        return cell.getName();
    }

    public CellDefinition getCellDefinition() {
        return cell.getCellDefinition();
    }

    public int getPositionLeft() {
        return cell.getPositionLeft();
    }

    public void setPositionLeft(int positionLeft) {
        cell.setPositionLeft(positionLeft);
    }

    public int getPositionTop() {
        return cell.getPositionTop();
    }

    public void setPositionTop(int positionTop) {
        cell.setPositionTop(positionTop);
    }

    private void loadOutputVariables() {
        for (VariableInstance variable : cell.getOutputVariableMap().values()) {
            VariableModel variableModel = new VariableModel(this, variable);
            variableModelMap.put(variable.getName(), variableModel);
        }
    }

    public void loadBindings() {
        for (BindingInstance binding : cell.getBindingMap().values()) {
            BindingModel bindingModel = new BindingModel(binding, notebookModel);
            bindingModelMap.put(binding.getName(), bindingModel);
        }
    }

    private void loadOptions() {
        for (OptionInstance<?> option : cell.getOptionMap().values()) {
            OptionModel<?> optionModel = new OptionModel<>(option);
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

    public int getSizeWidth() {
        return cell.getSizeWidth();
    }

    public void setSizeWidth(int sizeWidth) {
        cell.setSizeWidth(sizeWidth);
    }

    public int getSizeHeight() {
        return cell.getSizeHeight();
    }

    public void setSizeHeight(int height) {
        cell.setSizeHeight(height);
    }
}
