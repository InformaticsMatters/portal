package portal.notebook;

import org.squonk.notebook.api.OptionType;
import portal.notebook.service.Option;

import java.io.Serializable;
import java.util.List;

public class OptionModel<T> implements Serializable {
    private final Option<T> option;

    public String getName() {
        return option.getName();
    }

    public OptionType getOptionType() {
        return option.getOptionType();
    }

    public List<T> getPicklistValueList() {
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

    public OptionModel(Option<T> option) {
        this.option = option;
    }
}
