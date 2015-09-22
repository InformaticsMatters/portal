package portal.webapp.notebook;

import java.util.ArrayList;
import java.util.List;

public class CodeCell extends AbstractCell {
    private String code;
    private String errorMessage;
    private Object outcome;
    private final List<String> inputVariableNameList = new ArrayList<String>();
    private final List<String> outputVariableNameList = new ArrayList<String>();

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
    public List<String> getInputVariableNameList() {
        return inputVariableNameList;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }
}
