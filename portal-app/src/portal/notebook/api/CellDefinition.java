package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@XmlRootElement
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public abstract class CellDefinition implements Serializable {

    public enum UpdateMode {MANUAL, AUTO}

    public static final String VAR_NAME_INPUT = "input";
    public static final String VAR_NAME_OUTPUT = "output";
    public static final String VAR_NAME_FILECONTENT = "fileContent";
    ;
    private final static long serialVersionUID = 1L;
    private final List<BindingDefinition> bindingDefinitionList = new ArrayList<>();
    private final List<OptionBindingDefinition> optionBindingDefinitionList = new ArrayList<>();
    private final List<IODescriptor> variableDefinitionList = new ArrayList<>();
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

    public CellDefinition withOutputVariable(IODescriptor iod) {
        this.variableDefinitionList.add(iod);
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

    /**
     * Get the declared types of the output variables. Note that the runtime types (specified by the @{link #getVariableRuntimeList}
     * method) can be more specific.
     *
     * @return
     */
    public List<IODescriptor> getVariableDefinitionList() {
        return this.variableDefinitionList;
    }

    /**
     * Get the actual runtime types of an output variable, which might not be the same as it's declared types.
     * For insatance, a cell that filters a Dataset<BasicObject> based on some attribute would be declared as outputting
     * Dataset<BasicObject>, but it would also be able to handle input that was Dataset<MoleculeObject>, and in this case
     * the runtime type of the output would also be Dataset<MoleculeObject>.
     * <p>
     * The return value is a 2 element array describing the primary and secondary types.
     * <p>
     * This default implementation returns the declared types (so is identical to that found in the @{link #getVariableDefinitionList},
     * but subclasses can override this to return something different depending on the bound inputs.
     *
     * @return A 2 element array containg the primary and secondary types of the specified IODescriptor
     */
    public Class[] getOutputVariableRuntimeType(NotebookInstance notebook, Long cellId, IODescriptor outputDescriptor) {
        if (!getVariableDefinitionList().contains(outputDescriptor)) {
            throw new IllegalStateException("IODescriptor is not one of this cell's outputs. Requested: " + outputDescriptor + " present: " +
            getVariableDefinitionList().stream().map(IODescriptor::toString).collect(Collectors.joining(" , ")));
        }
        return new Class[] {outputDescriptor.getPrimaryType(), outputDescriptor.getSecondaryType()};
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

    protected Class[] getInputVariableRuntimeType(NotebookInstance notebook, Long cellId, String varname) {

        CellInstance cellInstance = notebook.findCellInstanceById(cellId);
        if (cellInstance!= null) {
            VariableInstance variableInstance = cellInstance.getBindingInstanceMap().get(varname).getVariableInstance();
            if (variableInstance != null) {
                Long upstreamCellId = variableInstance.getCellId();
                IODescriptor upstreamIODescriptor = variableInstance.getVariableDefinition();
                CellInstance upstreamCellInstance = notebook.findCellInstanceById(upstreamCellId);
                if (upstreamCellInstance != null) {
                    return upstreamCellInstance.getCellDefinition().getOutputVariableRuntimeType(notebook, upstreamCellInstance.getId(), upstreamIODescriptor);
                }
            }
        }
        return null;
    }

}
