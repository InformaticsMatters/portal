package portal.notebook;

import tmp.squonk.notebook.api.OptionType;

import java.io.Serializable;

public class OptionModel implements Serializable {
    private String name;
    private OptionType optionType;
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
