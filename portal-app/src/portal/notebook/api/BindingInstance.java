package portal.notebook.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BindingInstance implements Serializable {
    private String name;
    private String displayName;
    private final List<VariableType> acceptedVariableTypeList = new ArrayList<>();
    private VariableInstance variable;
    private boolean dirty = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public VariableInstance getVariable() {
        return variable;
    }

    public void setVariable(VariableInstance variable) {
        dirty = true;
        this.variable = variable;
    }

    public List<VariableType> getAcceptedVariableTypeList() {
        return acceptedVariableTypeList;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }
}
