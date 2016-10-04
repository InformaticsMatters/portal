package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public abstract class CellDefinition implements Serializable {

    public static final String VAR_NAME_INPUT = "input";
    public static final String VAR_NAME_OUTPUT = "output";
    public static final String VAR_NAME_FILECONTENT = "fileContent";
    public static final String VAR_DISPLAYNAME_INPUT = "Input";
    public static final String VAR_DISPLAYNAME_OUTPUT = "Output";
    public static final String VAR_DISPLAYNAME_FILECONTENT = "File Content";
    private final static long serialVersionUID = 1L;
    private final List<BindingDefinition> bindingDefinitionList = new ArrayList<>();
    private final List<OptionBindingDefinition> optionBindingDefinitionList = new ArrayList<>();
    private final List<VariableDefinition> variableDefinitionList = new ArrayList<>();
    private final List<OptionDescriptor> optionDefinitionList = new ArrayList<>();
    private String name;
    private String description;
    private String icon;
    private Boolean executable;
    private String[] tags;
    private Integer initialWidth, initialHeight;


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
        this.variableDefinitionList.add(new VariableDefinition(name, description, variableType, defaultValue));
        return this;
    }

    public CellDefinition withOutputVariable(String name, String description, VariableType variableType) {
        this.variableDefinitionList.add(new VariableDefinition(name, description, variableType));
        return this;
    }

    public Integer getInitialWidth() {
        return initialWidth;
    }

    public void setInitialWidth(Integer initialWidth) {
        this.initialWidth = initialWidth;
    }

    public Integer getInitialHeight() {
        return initialHeight;
    }

    public void setInitialHeight(Integer initialHeight) {
        this.initialHeight = initialHeight;
    }

    public CellDefinition withInitialDimensions(int width, int height) {
        this.initialWidth = width;
        this.initialHeight = height;
        return this;

    }

    public List<BindingDefinition> getBindingDefinitionList() {
        return this.bindingDefinitionList;
    }

    public List<OptionBindingDefinition> getOptionBindingDefinitionList() {
        return this.optionBindingDefinitionList;
    }
    public List<VariableDefinition> getVariableDefinitionList() {
        return this.variableDefinitionList;
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
        return executable;
    }

    public void setExecutable(Boolean executable) {
        this.executable = executable;
    }

    public abstract CellExecutor getCellExecutor();

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

}
