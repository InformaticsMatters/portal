package portal.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableInstance implements Serializable {
    private final static long serialVersionUID = 1l;
    private Long cellId;
    private VariableDefinition variableDefinition;
    private Object value;
    private boolean dirty = false;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        dirty = true;
        this.value = value;
    }

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    public void setVariableDefinition(VariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }


}
