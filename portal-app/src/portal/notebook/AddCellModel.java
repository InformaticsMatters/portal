package portal.notebook;

import portal.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<VariableModel> inputVariableModelList = Collections.emptyList();
    private final List<String> outputVariableNameList = new ArrayList<>();
    private Integer num1;
    private Integer num2;

    @Override
    public CellType getCellType() {
        return CellType.ADD;
    }

    @Override
    public List<VariableModel> getInputVariableModelList() {
        return inputVariableModelList;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        storeHeader(cell);
        cell.getPropertyMap().put("num1", num1);
        cell.getPropertyMap().put("num2", num2);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        num1 = (Integer) cell.getPropertyMap().get("num1");
        num2 = (Integer) cell.getPropertyMap().get("num2");
    }

    public Integer getNum1() {
        return num1;
    }

    public void setNum1(Integer num1) {
        this.num1 = num1;
    }

    public Integer getNum2() {
        return num2;
    }

    public void setNum2(Integer num2) {
        this.num2 = num2;
    }
}
