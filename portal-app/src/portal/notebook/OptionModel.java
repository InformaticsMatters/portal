package portal.notebook;

import portal.notebook.service.Option;
import tmp.squonk.notebook.api.OptionType;

import java.io.Serializable;
import java.util.List;

public class OptionModel implements Serializable {
    private final Option option;

    public String getName() {
        return option.getName();
    }

    public OptionType getOptionType() {
        return option.getOptionType();
    }

    public List getPicklistValueList() {
        return option.getPicklistValueList();
    }

    public void setValue(Object value) {
        option.setValue(value);
    }

    public Object getValue() {
        return option.getValue();
    }

    public String getDisplayName() {
        return option.getDisplayName();
    }

    public OptionModel(Option option) {
        this.option = option;
    }
}
