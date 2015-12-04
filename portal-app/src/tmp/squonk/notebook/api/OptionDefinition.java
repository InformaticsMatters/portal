package tmp.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class OptionDefinition<T> implements Serializable {
    private String name;
    private String displayName;
    private OptionType optionType;
    private final List<T> picklistValueList = new ArrayList<>();
    private T defaultValue;

    public OptionDefinition() {
    }

    public OptionDefinition(String name, OptionType optionType) {
        this.name = name;
        this.optionType = optionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<T> getPicklistValueList() {
        return picklistValueList;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }
}
