package portal.notebook.service;


import org.squonk.notebook.api.VariableType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Binding implements Serializable {
    private String name;
    private String displayName;
    private final List<VariableType> acceptedVariableTypeList = new ArrayList<>();
    private Variable variable;

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

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public List<VariableType> getAcceptedVariableTypeList() {
        return acceptedVariableTypeList;
    }
}
