package portal.notebook.api;

import org.squonk.notebook.api.OptionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OptionInstance<T> implements Serializable {
    private String name;
    private OptionType optionType;
    private final List<T> picklistValueList = new ArrayList<>();
    private Object value;
    private String displayName;
    private transient boolean dirty = true;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        dirty = true;
        this.value = value;
    }

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

    public List<T> getPicklistValueList() {
        return picklistValueList;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void addPickListValue(T value) {
        picklistValueList.add(value);
    }

    public boolean isDirty() {
        return dirty;
    }
}
