package tmp.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableDefinition implements Serializable {
    private String name;
    private String displayName;
    private VariableType variableType;
    private Object defaultValue;

    public VariableDefinition() {

    }
    
    public VariableDefinition(String name, VariableType variableType) {
        this.name = name;
        this.variableType = variableType;
    }
    
    public VariableDefinition(String name, VariableType variableType, Object defaultValue) {
        this.name = name;
        this.variableType = variableType;
        this.defaultValue = defaultValue;
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

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
