package portal.notebook.webapp;

import org.squonk.options.OptionDescriptor;
import org.squonk.options.TypeDescriptor;

import java.io.Serializable;

public class FieldEditorModel<T> implements Serializable {
    private T value;
    private OptionDescriptor<T> optionDescriptor;

    protected FieldEditorModel(T value, OptionDescriptor<T> optionDescriptor) {
        this.value = value;
        this.optionDescriptor = optionDescriptor;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getDisplayName() {
        return optionDescriptor == null ? null : optionDescriptor.getLabel();
    }

    public OptionDescriptor<T> getOptionDescriptor() {
        return optionDescriptor;
    }

    public TypeDescriptor<T> getTypeDescriptor() {
        return optionDescriptor == null ? null : optionDescriptor.getTypeDescriptor();
    }
}
