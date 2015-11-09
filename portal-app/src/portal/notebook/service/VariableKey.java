package portal.notebook.service;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class VariableKey implements Serializable {
    private String producerCellName;
    private String variableName;

    public String getProducerCellName() {
        return producerCellName;
    }

    public void setProducerCellName(String producerCellName) {
        this.producerCellName = producerCellName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
