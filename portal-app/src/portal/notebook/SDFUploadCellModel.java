package portal.notebook;

import com.squonk.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SDFUploadCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();
    private String nameFieldName;

    public SDFUploadCellModel(CellType cellType) {
        super(cellType);
    }

    @Override
    protected void createVariableTargets(List<BindingTargetModel> bindingTargetModelList) {

    }


    @Override
    public List<VariableModel> getInputVariableModelList() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }


    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        System.out.println("SDFUploadCellModel.store(): nameFieldName=" + nameFieldName + " | " + this);
        super.store(notebookContents, cell);
        cell.getPropertyMap().put("NameFieldName", nameFieldName);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        System.out.println("SDFUploadCellModel.load()");
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        nameFieldName = (String) cell.getPropertyMap().get("NameFieldName");
        System.out.println("SDFUploadCellModel.load(): nameFieldName=" + nameFieldName);
    }

    @Override
    public void bindVariableModel(VariableModel sourceVariableModel, BindingTargetModel bindingTargetModel) {
        throw new UnsupportedOperationException();
    }

    public String getNameFieldName() {
        System.out.println("SDFUploadCellModel.getNameFieldName() -> " + nameFieldName);
        return nameFieldName;
    }

    public void setNameFieldName(String name) {
        System.out.println("SDFUploadCellModel.setNameFieldName() -> " + name + " | " + this);
        this.nameFieldName = name;
    }

}
