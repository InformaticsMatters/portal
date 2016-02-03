package portal.notebook;

import portal.notebook.api.BindingInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.api.VariableType;

import java.io.Serializable;
import java.util.List;

public class BindingModel implements Serializable {
    private final BindingInstance binding;
    private NotebookModel notebookModel;
    private VariableModel variableModel;

    public BindingModel(BindingInstance binding, NotebookModel notebookModel) {
        this.binding = binding;
        this.notebookModel = notebookModel;
        VariableInstance variable = binding.getVariable();
        if (variable != null) {
            variableModel = notebookModel.findVariableModel(variable.getCellId(), variable.getName());
        }
    }

    public String getName() {
        return binding.getName();
    }

    public String getDisplayName() {
        return binding.getDisplayName();
    }

    public List<VariableType> getAcceptedVariableTypeList() {
        return binding.getAcceptedVariableTypeList();
    }

    public void setVariableModel(VariableModel variableModel) {
        if (variableModel == null) {
            binding.setVariable(null);
        } else {
            binding.setVariable(variableModel.getVariable());
        }
        this.variableModel = variableModel;
    }

    public VariableModel getVariableModel() {
        return variableModel;
    }

}
