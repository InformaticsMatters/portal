package portal.notebook.api;

import org.squonk.io.IODescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableInstance implements Serializable {

    private final static long serialVersionUID = 1L;
    private Long cellId;
    private IODescriptor variableDefinition;
    private boolean dirty = false;

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

    public IODescriptor getVariableDefinition() {
        return variableDefinition;
    }

    public void setVariableDefinition(IODescriptor variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    public String calculateKey() {
        return cellId + "." + variableDefinition.getName();
    }

    @Override
    public String toString() {
        return "VariableInstance [cellId: " + getCellId() + " " + variableDefinition + "]";
    }
}