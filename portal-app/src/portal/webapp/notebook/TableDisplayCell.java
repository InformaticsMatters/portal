package portal.webapp.notebook;

import java.util.Collections;
import java.util.List;

public class TableDisplayCell extends AbstractCell {
    private Variable inputVariable;

    @Override
    public CellType getCellType() {
        return CellType.TABLE_DISPLAY;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return inputVariable == null ? Collections.emptyList() : Collections.singletonList(inputVariable);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return Collections.emptyList();
    }

    public Variable getInputVariable() {
        return inputVariable;
    }

    public void setInputVariable(Variable inputVariable) {
        this.inputVariable = inputVariable;
    }

}
