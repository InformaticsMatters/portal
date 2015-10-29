package portal.webapp.notebook;


import portal.notebook.AbstractCell;
import portal.notebook.CellType;
import portal.notebook.Variable;

import java.util.ArrayList;
import java.util.List;

public class NotebookDebugCell extends AbstractCell {
    private static final long serialVersionUID = 1l;

    @Override
    public CellType getCellType() {
        return CellType.NOTEBOOK_DEBUG;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return new ArrayList<>();
    }
}
