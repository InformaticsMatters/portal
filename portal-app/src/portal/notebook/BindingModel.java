package portal.notebook;

import portal.notebook.service.Binding;
import portal.notebook.service.Variable;
import tmp.squonk.notebook.api.VariableType;

import java.io.Serializable;
import java.util.List;

public class BindingModel implements Serializable {
    private final Binding binding;
    private NotebookModel notebookModel;
    private VariableModel variableModel;

    public BindingModel(Binding binding, NotebookModel notebookModel) {
        this.binding = binding;
        this.notebookModel = notebookModel;
        Variable variable = binding.getVariable();
        if (variable != null) {
            variableModel = notebookModel.findVariableModel(variable.getProducerCell().getName(), variable.getName());
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
