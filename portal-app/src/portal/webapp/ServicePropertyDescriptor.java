package portal.webapp;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ServicePropertyDescriptor implements Serializable {

    private Type type = Type.STRING;
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {

        STRING,
        INTEGER,
        STRUCTURE

    }
}
