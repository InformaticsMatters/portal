package portal.notebook;

import java.io.Serializable;

public abstract class FieldEditorModel implements Serializable {
    private Object value;


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    abstract String getDisplayName();

}
