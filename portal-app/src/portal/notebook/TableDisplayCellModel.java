package portal.notebook;

import portal.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.Variable;

import java.util.Collections;
import java.util.List;

public class TableDisplayCellModel extends AbstractCellModel {
    private static final Long serialVersionUID = 1l;
    private VariableModel inputVariableModel;

    public TableDisplayCellModel(CellType cellType) {
        super(cellType);
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

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        Variable variable = cell.getInputVariableList().isEmpty() ? null : cell.getInputVariableList().get(0);
        inputVariableModel = variable == null ? null : notebookModel.findVariable(variable.getProducerCell().getName(), variable.getName());
    }



}
