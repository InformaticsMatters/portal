package portal.webapp.notebook;


import java.util.Collections;
import java.util.List;

public class PropertyCalculateCell extends AbstractCell {
    private Variable inputVariable;
    private final List<String> outputVariableNameList = Collections.singletonList("fileName");

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

}
