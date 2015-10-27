package portal.webapp.notebook;


import org.eclipse.jetty.util.log.Log;

import java.util.Collections;
import java.util.List;

public class PropertyCalculateCell extends AbstractCell {
    private static final long serialVersionUID = 1l;
    private Variable inputVariable;
    private final List<String> outputVariableNameList = Collections.singletonList("outputFileName");
    private String serviceName;

    @Override
    public CellType getCellType() {
        return CellType.PROPERTY_CALCULATE;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return inputVariable == null ? Collections.emptyList() : Collections.singletonList(inputVariable);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    public Variable getInputVariable() {
        return inputVariable;
    }

    public void setInputVariable(Variable inputVariable) {
        this.inputVariable = inputVariable;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
