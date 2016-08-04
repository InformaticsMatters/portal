package portal.notebook.webapp;

import org.squonk.options.OptionDescriptor;
import org.squonk.options.TypeDescriptor;

import java.io.Serializable;

public class FieldEditorModel implements Serializable {
    private Object value;
    private OptionDescriptor optionDescriptor;

    protected FieldEditorModel(Object value, OptionDescriptor optionDescriptor) {
        this.value = value;
        this.optionDescriptor = optionDescriptor;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDisplayName() {
        return optionDescriptor == null ? null : optionDescriptor.getLabel();
    }

    public OptionDescriptor getOptionDescriptor() {
        return optionDescriptor;
    }

    public TypeDescriptor getTypeDescriptor() {
        return optionDescriptor == null ? null : optionDescriptor.getTypeDescriptor();
    }
}
