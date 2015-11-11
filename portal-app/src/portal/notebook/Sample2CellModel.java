package portal.notebook;

import portal.notebook.api.CellType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sample2CellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<VariableModel> inputVariableModelList = Collections.emptyList();
    private final List<String> outputVariableNameList = new ArrayList<>();

    @Override
    public CellType getCellType() {
        return CellType.SAMPLE2;
    }

    @Override
    public List<VariableModel> getInputVariableModelList() {
        return inputVariableModelList;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

}
