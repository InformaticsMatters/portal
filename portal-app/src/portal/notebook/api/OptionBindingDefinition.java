package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class OptionBindingDefinition implements Serializable {

    private final static long serialVersionUID = 1L;
    private final OptionDescriptor optionDescriptor;
    private final CellInstance.UpdateMode updateMode;


    public OptionBindingDefinition(OptionDescriptor optionDescriptor, CellInstance.UpdateMode updateMode) {
        this.optionDescriptor = optionDescriptor;
        this.updateMode = updateMode;
    }

    public OptionDescriptor getOptionDescriptor() {
        return optionDescriptor;
    }

    public CellInstance.UpdateMode getUpdateMode() {
        return updateMode;
    }

    @JsonIgnore
    public String getKey() {
        return optionDescriptor.getKey();
    }

    @JsonIgnore
    public String getName() {
        return optionDescriptor.getLabel();
    }

    @JsonIgnore
    public String getDescription() {
        return optionDescriptor.getDescription();
    }
}
