package portal.notebook.api;

import org.squonk.notebook.api.OptionType;
import org.squonk.options.OptionDescriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OptionInstance implements Serializable {
    private OptionDescriptor optionDescriptor;
    private Object value;
    private boolean dirty = true;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        dirty = true;
        this.value = value;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public OptionDescriptor getOptionDescriptor() {
        return optionDescriptor;
    }

    public void setOptionDescriptor(OptionDescriptor optionDescriptor) {
        this.optionDescriptor = optionDescriptor;
    }
}
