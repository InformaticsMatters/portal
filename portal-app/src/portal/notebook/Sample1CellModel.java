package portal.notebook;

import portal.notebook.execution.api.CellType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sample1CellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<VariableModel> inputVariableModelList = Collections.emptyList();
    private final List<String> outputVariableNameList = new ArrayList<>();

    public Sample1CellModel(CellType cellType) {
        super(cellType);
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
