package portal.notebook.api;

import org.squonk.dataset.DatasetSelection;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@XmlRootElement
public class CellInstance implements Serializable {

    private final static long serialVersionUID = 1L;
    private final Map<String, BindingInstance> bindingInstanceMap = new LinkedHashMap<>();
    private final Map<String, OptionBindingInstance> optionBindingInstanceMap = new LinkedHashMap<>();
    private final Map<String, VariableInstance> variableInstanceMap = new LinkedHashMap<>();
    private final Map<String, OptionInstance> optionInstanceMap = new LinkedHashMap<>();
    private final Map<String, Object> settings = new LinkedHashMap<>();
    private CellDefinition cellDefinition;
    private Long id;
    private String name;
    private Integer positionLeft;
    private Integer positionTop;
    private Integer sizeWidth;
    private Integer sizeHeight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CellDefinition getCellDefinition() {
        return cellDefinition;
    }

    public void setCellDefinition(CellDefinition cellDefinition) {
        this.cellDefinition = cellDefinition;
    }

    public Map<String, BindingInstance> getBindingInstanceMap() {
        return bindingInstanceMap;
    }

    public Map<String, OptionBindingInstance> getOptionBindingInstanceMap() {
        return optionBindingInstanceMap;
    }

    public Map<String, VariableInstance> getVariableInstanceMap() {
        return variableInstanceMap;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, OptionInstance> getOptionInstanceMap() {
        return optionInstanceMap;
    }

    public Integer getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(Integer positionLeft) {
        this.positionLeft = positionLeft;
    }

    public Integer getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(Integer positionTop) {
        this.positionTop = positionTop;
    }

    public Integer getSizeWidth() {
        return sizeWidth;
    }

    public void setSizeWidth(Integer sizeWidth) {
        this.sizeWidth = sizeWidth;
    }

    public Integer getSizeHeight() {
        return sizeHeight;
    }

    public void setSizeHeight(Integer sizeHeight) {
        this.sizeHeight = sizeHeight;
    }

    /**
     * Get the settings for this cell. These are things that are not related to bindings etc.
     * An example is the number of columns to use the for the results viewer
     *
     * @return
     */
    public Map<String, Object> getSettings() {
        return settings;
    }

    public BindingInstance getBindingInstance(String bindingName) {
        return getBindingInstanceMap().get(bindingName);
    }

    public OptionBindingInstance getOptionBindingInstance(String bindingName) {
        return getOptionBindingInstanceMap().get(bindingName);
    }

    public VariableInstance getVariableInstance(String variableName) {
        return getVariableInstanceMap().get(variableName);
    }

    public OptionInstance getOptionInstance(String optionName) {
        return getOptionInstanceMap().get(optionName);
    }

    public VariableInstance getBoundVariableInstance(String bindingName) {
        BindingInstance bindingInstance = getBindingInstance(bindingName);
        if (bindingInstance == null) {
            return null;
        }
        return bindingInstance.getVariableInstance();
    }

    public Set<UUID> readOptionBindingFilter(String optionBindingName) {
        OptionBindingInstance optionBindingInstance = getOptionBindingInstance(optionBindingName);
        Set<UUID> result = null;
        if (optionBindingInstance != null) {
            OptionInstance optionInstance = optionBindingInstance.getOptionInstance();
            if (optionInstance != null) {
                DatasetSelection datasetSelection = (DatasetSelection) optionInstance.getValue();
                if (datasetSelection != null) {
                    result = datasetSelection.getUuids();
                }
            }
        }
        return result;
    }

    public Set<UUID> readOptionFilter(String optionName) {
        OptionInstance optionInstance = getOptionInstance(optionName);
        Set<UUID> result = null;
        if (optionInstance != null) {
            DatasetSelection datasetSelection = (DatasetSelection) optionInstance.getValue();
            if (datasetSelection != null) {
                result = datasetSelection.getUuids();
            }
        }
        return result;
    }
}
