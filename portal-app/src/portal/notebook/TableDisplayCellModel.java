package portal.notebook;

import java.util.Collections;
import java.util.List;

public class TableDisplayCellModel extends AbstractCellModel {
    private static final Long serialVersionUID = 1l;
    private VariableModel inputVariableModel;

    @Override
    public CellType getCellType() {
        return CellType.TABLE_DISPLAY;
    }

    @Override
    public List<VariableModel> getInputVariableModelList() {
        return inputVariableModel == null ? Collections.emptyList() : Collections.singletonList(inputVariableModel);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return Collections.emptyList();
    }

    public VariableModel getInputVariableModel() {
        return inputVariableModel;
    }

    public void setInputVariableModel(VariableModel inputVariableModel) {
        this.inputVariableModel = inputVariableModel;
    }

}
