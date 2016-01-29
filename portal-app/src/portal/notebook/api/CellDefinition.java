package portal.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public abstract class CellDefinition implements Serializable {

    public static final String VAR_NAME_INPUT = "input";
    public static final String VAR_NAME_OUTPUT = "output";
    public static final String VAR_NAME_FILECONTENT = "fileContent";

    private String name;
    private String description;
    private Boolean executable;
    private final List<BindingDefinition> bindingDefinitionList = new ArrayList();
    private final List<VariableDefinition> outputVariableDefinitionList = new ArrayList();
    private final List<OptionDefinition> optionDefinitionList = new ArrayList();


    public CellDefinition(String name, String description, Boolean executable) {
        this.name = name;
        this.description = description;
        this.executable = executable;
    }

    public CellDefinition() {
    }

    public CellDefinition withOutputVariable(String name, VariableType variableType, Object defaultValue) {
        this.outputVariableDefinitionList.add(new VariableDefinition(name, variableType, defaultValue));
        return this;
    }

    public CellDefinition withOutputVariable(String name, VariableType variableType) {
        this.outputVariableDefinitionList.add(new VariableDefinition(name, variableType));
        return this;
    }

    public List<BindingDefinition> getBindingDefinitionList() {
        return this.bindingDefinitionList;
    }

    public List<VariableDefinition> getOutputVariableDefinitionList() {
        return this.outputVariableDefinitionList;
    }

    public List<OptionDefinition> getOptionDefinitionList() {
        return this.optionDefinitionList;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getExecutable() {
        return this.executable;
    }

    public void setExecutable(Boolean executable) {
        this.executable = executable;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public abstract CellExecutor getCellExecutor();

}
