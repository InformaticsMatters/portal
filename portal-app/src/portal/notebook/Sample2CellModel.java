package portal.notebook;

import com.squonk.notebook.api.CellType;
import com.squonk.notebook.api.VariableType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sample2CellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();
    private Integer num2;
    private VariableModel inputVariableModel;


    public Sample2CellModel(CellType cellType) {
        super(cellType);
    }

    @Override
    protected void createVariableTargets(List<BindingTargetModel> bindingTargetModelList) {
        BindingTargetModel bindingTargetModel = new BindingTargetModel();
        bindingTargetModel.setDisplayName("Number 1");
        bindingTargetModel.setName("num1");
        bindingTargetModel.setVariableType(VariableType.VALUE);
        bindingTargetModelList.add(bindingTargetModel);
    }

    @Override
    public List<VariableModel> getInputVariableModelList() {
        return inputVariableModel == null ? Collections.emptyList() : Collections.singletonList(inputVariableModel);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        super.store(notebookContents, cell);
        cell.getPropertyMap().put("number2", num2);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        num2 = (Integer) cell.getPropertyMap().get("number2");
        Variable variable = cell.getInputVariableList().isEmpty() ? null : cell.getInputVariableList().get(0);
        inputVariableModel = variable == null ? null : notebookModel.findVariableModel(variable.getProducerCell().getName(), variable.getName());

    }


    public Integer getNum2() {
        return num2;
    }

    public void setNum2(Integer num2) {
        this.num2 = num2;
    }

    public VariableModel getInputVariableModel() {
        return inputVariableModel;
    }

    public void setInputVariableModel(VariableModel inputVariableModel) {
        this.inputVariableModel = inputVariableModel;
    }

    @Override
    public void bindVariableModel(VariableModel sourceVariableModel, BindingTargetModel bindingTargetModel) {
        if (bindingTargetModel.getName().equals("num1")) {
            setInputVariableModel(sourceVariableModel);
        } else {
            throw new RuntimeException("Unknown target");
        }
    }

}
