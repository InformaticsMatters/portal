package portal.webapp.notebook;


import java.util.ArrayList;
import java.util.List;

public class NotebookDebugCellDescriptor extends AbstractCellDescriptor {


    @Override
    public CellType getCellType() {
        return CellType.DEBUG;
    }

    @Override
    public List<String> getInputVariableNameList() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return new ArrayList<>();
    }
}
