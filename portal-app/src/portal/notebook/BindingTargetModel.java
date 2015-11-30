package portal.notebook;

import com.squonk.notebook.api.VariableType;

public class BindingTargetModel {
    private String displayName;
    private String name;
    private VariableType variableType;
    private VariableModel sourceVariableModel;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    public VariableModel getSourceVariableModel() {
        return sourceVariableModel;
    }

    public void setSourceVariableModel(VariableModel sourceVariableModel) {
        this.sourceVariableModel = sourceVariableModel;
    }
}
