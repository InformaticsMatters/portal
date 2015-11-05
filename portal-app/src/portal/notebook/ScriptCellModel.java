package portal.notebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private String code;
    private String errorMessage;
    private Object outcome;
    private final List<VariableModel> inputVariableModelList = new ArrayList<>();
    private final List<String> outputVariableNameList = new ArrayList<>();

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
    public List<VariableModel> getInputVariableModelList() {
        return inputVariableModelList;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        super.store(notebookContents, cell);
        cell.getPropertyMap().put("code", code);
        cell.getPropertyMap().put("outcome", outcome);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        outcome = cell.getPropertyMap().get("outcome");
        code = (String)cell.getPropertyMap().get("code");
        errorMessage = (String)cell.getPropertyMap().get("errorMessage");
    }

}
