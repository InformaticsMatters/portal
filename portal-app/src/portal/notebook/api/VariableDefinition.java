package portal.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableDefinition implements Serializable {

    private final static long serialVersionUID = 1L;
    private String name;
    private VariableType variableType;
    private Object defaultValue;

    public VariableDefinition() {
    }

    public VariableDefinition(String name, VariableType variableType) {
        this.name = name;
        this.variableType = variableType;
    }

    public VariableDefinition(String name, VariableType variableType, Object defaultValue) {
        this(name, variableType);
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VariableType getVariableType() {
        return this.variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "VariableDefinition [name: " + name + " type: " + variableType + "]";
    }
}
