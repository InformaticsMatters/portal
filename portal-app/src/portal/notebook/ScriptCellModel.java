package portal.notebook;

import com.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.List;

public class ScriptCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();

    public ScriptCellModel(CellType cellType) {
        super(cellType);
    }

    public String getCode() {
        return (String) getOptionMap().get("code");
    }

    public void setCode(String code) {
        getOptionMap().put("code", code);
    }

    public Object getOutcome() {
        return (String) getOptionMap().get("outcome");
    }

    public void setOutcome(Object outcome) {
        getOptionMap().put("outcome", outcome);
    }

    public String getErrorMessage() {
        return (String) getOptionMap().get("errorMessage");
    }

    public void setErrorMessage(String errorMessage) {
        getOptionMap().put("errorMessage", errorMessage);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

}
