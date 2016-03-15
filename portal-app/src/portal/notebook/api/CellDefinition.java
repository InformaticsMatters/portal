package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.squonk.options.OptionDescriptor;

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
    public static final String VAR_DISPLAYNAME_INPUT = "Input";
    public static final String VAR_DISPLAYNAME_OUTPUT = "Output";
    public static final String VAR_DISPLAYNAME_FILECONTENT = "File Content";
    private final static long serialVersionUID = 1L;
    private final List<BindingDefinition> bindingDefinitionList = new ArrayList<>();
    private final List<VariableDefinition> outputVariableDefinitionList = new ArrayList<>();
    private final List<OptionDescriptor> optionDefinitionList = new ArrayList<>();
    private String name;
    private String description;
    private String icon;
    private Boolean executable;
    private String[] tags;


    public CellDefinition(String name, String description, String icon, String[] tags, Boolean executable) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.tags = tags;
        this.executable = executable;
    }


    public CellDefinition(String name, String description, String icon, String[] tags) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.tags = tags;
        this.executable = true;
    }

    public CellDefinition() {
    }

    public CellDefinition withOutputVariable(String name, String description, VariableType variableType, Object defaultValue) {
        this.outputVariableDefinitionList.add(new VariableDefinition(name, description, variableType, defaultValue));
        return this;
    }

    public CellDefinition withOutputVariable(String name, String description, VariableType variableType) {
        this.outputVariableDefinitionList.add(new VariableDefinition(name, description, variableType));
        return this;
    }

    public List<BindingDefinition> getBindingDefinitionList() {
        return this.bindingDefinitionList;
    }

    public List<VariableDefinition> getOutputVariableDefinitionList() {
        return this.outputVariableDefinitionList;
    }

    public List<OptionDescriptor> getOptionDefinitionList() {
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

    public String getIcon() {
        return icon;
    }
    public String[] getTags() {
        return tags;
    }

    @XmlTransient
    @JsonIgnore
    public abstract CellExecutor getCellExecutor();

}
