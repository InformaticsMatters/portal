package portal.webapp.notebook;


import java.util.ArrayList;
import java.util.List;

public class NotebookDebugCell extends AbstractCell {


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