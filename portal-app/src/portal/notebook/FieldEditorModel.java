package portal.notebook;

import java.io.Serializable;

public class FieldEditorModel implements Serializable {
    private final String displayName;
    private Object value;

    protected FieldEditorModel(Object value, String displayName) {
        this.value = value;
        this.displayName = displayName;
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

}
