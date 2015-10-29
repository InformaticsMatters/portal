package portal.notebook;


import java.util.ArrayList;
import java.util.List;

public class NotebookDebugCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;

    @Override
    public CellType getCellType() {
        return CellType.NOTEBOOK_DEBUG;
    }

    @Override
    public List<VariableModel> getInputVariableModelList() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return new ArrayList<>();
    }
}
