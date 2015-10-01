package portal.webapp.notebook;

import java.util.Collections;
import java.util.List;

public class TableDisplayCell extends AbstractCell {
    @Override
    public CellType getCellType() {
        return CellType.TABLE_DISPLAY;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return Collections.emptyList();
    }
}
