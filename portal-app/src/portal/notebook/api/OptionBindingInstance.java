package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class OptionBindingInstance implements Serializable {

    private final static long serialVersionUID = 1L;
    private OptionBindingDefinition optionBindingDefinition;
    private OptionInstance optionInstance;
    private boolean dirty = true;

    @JsonIgnore
    public String getKey() {
        return optionBindingDefinition.getKey();
    }

    @JsonIgnore
    public String getName() {
        return optionBindingDefinition.getName();
    }

    @JsonIgnore
    public String getDescription() {
        return optionBindingDefinition.getDescription();
    }

    public OptionInstance getOptionInstance() {
        return optionInstance;
    }

    public void setOptionInstance(OptionInstance optionInstance) {
        dirty = true;
        this.optionInstance = optionInstance;
    }

    @JsonIgnore
    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public OptionBindingDefinition getOptionBindingDefinition() {
        return optionBindingDefinition;
    }

    public void setOptionBindingDefinition(OptionBindingDefinition optionBindingDefinition) {
        this.optionBindingDefinition = optionBindingDefinition;
    }
}