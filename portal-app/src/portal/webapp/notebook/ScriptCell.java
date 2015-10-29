package portal.webapp.notebook;

import portal.notebook.AbstractCell;
import portal.notebook.CellType;
import portal.notebook.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptCell extends AbstractCell {
    private static final long serialVersionUID = 1l;
    private String code;
    private String errorMessage;
    private Object outcome;
    private final List<Variable> inputVariableList = new ArrayList<>();
    private final List<String> outputVariableNameList = Collections.singletonList("outcome");

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getOutcome() {
        return outcome;
    }

    public void setOutcome(Object outcome) {
        this.outcome = outcome;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public CellType getCellType() {
        return CellType.CODE;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return inputVariableList;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }
}
