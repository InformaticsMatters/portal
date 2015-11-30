package portal.notebook;

import com.squonk.notebook.api.CellType;
import com.squonk.notebook.api.VariableType;
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
    protected void createVariableTargets(List<BindingTargetModel> bindingTargetModelList) {
        BindingTargetModel bindingTargetModel = new BindingTargetModel();
        bindingTargetModel.setDisplayName("Input file");
        bindingTargetModel.setName("input");
        bindingTargetModel.setVariableType(VariableType.FILE);
        bindingTargetModelList.add(bindingTargetModel);
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
        inputVariableModel = variable == null ? null : notebookModel.findVariableModel(variable.getProducerCell().getName(), variable.getName());
    }

    @Override
    public void bindVariableModel(VariableModel sourceVariableModel, BindingTargetModel bindingTargetModel) {
        if (bindingTargetModel.getName().equals("input")) {
            setInputVariableModel(sourceVariableModel);
        } else {
            throw new RuntimeException("Unknown target");
        }
    }


}
