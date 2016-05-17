package portal.notebook.webapp;

import org.squonk.options.TypeDescriptor;

import java.io.Serializable;

public class FieldEditorModel implements Serializable {
    private final String displayName;
    private Object value;
    private TypeDescriptor typeDescriptor;

    protected FieldEditorModel(Object value, String displayName, TypeDescriptor typeDescriptor) {
        this.value = value;
        this.displayName = displayName;
        this.typeDescriptor = typeDescriptor;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }
}
