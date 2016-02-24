package portal.notebook;

import portal.notebook.api.VariableInstance;

public class VariableFieldEditorModel extends FieldEditorModel {
    private final VariableInstance variableInstance;

    public VariableFieldEditorModel(VariableInstance variableInstance) {
        this.variableInstance = variableInstance;
        setValue(variableInstance.getValue());
    }


    @Override
    String getDisplayName() {
        return null;
    }

    public VariableInstance getVariableInstance() {
        return variableInstance;
    }
}
