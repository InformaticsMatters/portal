package portal.notebook;


import com.squonk.notebook.api.CellType;
import com.squonk.notebook.api.VariableType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PropertyCalculateCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private VariableModel inputVariableModel;
    private final List<String> outputVariableNameList = new ArrayList<>();
    private String serviceName;

    public PropertyCalculateCellModel(CellType cellType) {
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
        return outputVariableNameList;
    }

    public VariableModel getInputVariableModel() {
        return inputVariableModel;
    }

    public void setInputVariableModel(VariableModel inputVariableModel) {
        this.inputVariableModel = inputVariableModel;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        super.store(notebookContents, cell);
        cell.getPropertyMap().put("serviceName", serviceName);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        Variable variable = cell.getInputVariableList().isEmpty() ? null : cell.getInputVariableList().get(0) ;
        inputVariableModel = variable == null ? null : notebookModel.findVariableModel(variable.getProducerCell().getName(), variable.getName());
        serviceName = (String)cell.getPropertyMap().get("serviceName");
    }

    @Override
    public void bindVariableModel(VariableModel sourceVariableModel, BindingTargetModel bindingTargetModel) {
        if (bindingTargetModel.getName().equals("input")) {
            inputVariableModel = sourceVariableModel;
        }
    }
}
