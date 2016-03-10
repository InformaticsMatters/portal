package portal.notebook.api;

import java.io.Serializable;

public class BindingInstance implements Serializable {
    private final static long serialVersionUID = 1l;
    private BindingDefinition bindingDefinition;
    private VariableInstance variable;
    private boolean dirty = true;

    public String getName() {
        return bindingDefinition.getName();
    }

    public String getDisplayName() {
        return bindingDefinition.getDisplayName();
    }

    public VariableInstance getVariable() {
        return variable;
    }

    public void setVariable(VariableInstance variable) {
        dirty = true;
        this.variable = variable;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public BindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    public void setBindingDefinition(BindingDefinition bindingDefinition) {
        this.bindingDefinition = bindingDefinition;
    }
}
