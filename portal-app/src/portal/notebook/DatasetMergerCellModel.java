package portal.notebook;

import com.squonk.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.List;

public class DatasetMergerCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private VariableModel[] inputVariableModels = new VariableModel[5];
    private final List<String> outputVariableNameList = new ArrayList<>();
    private String mergeFieldName;
    private boolean keepFirst;

    public DatasetMergerCellModel(CellType cellType) {
        super(cellType);
    }


    @Override
    public List<VariableModel> getInputVariableModelList() {
        List<VariableModel> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (inputVariableModels[i] != null) {
                list.add(inputVariableModels[i]);
            }
        }
        return list;
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }


    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        super.store(notebookContents, cell);
        cell.getPropertyMap().put("MergeFieldName", mergeFieldName);
        cell.getPropertyMap().put("KeepFirst", keepFirst);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }

        for (int i = 0; i < 5; i++) {
            inputVariableModels[i] = null;
        }

        List<Variable> vars = cell.getInputVariableList();
        for (int i = 0; i < vars.size(); i++) {
            Variable var = vars.get(i);
            inputVariableModels[i] = var == null ? null : notebookModel.findVariable(var.getProducerCell().getName(), var.getName());
        }

        mergeFieldName = (String) cell.getPropertyMap().get("MergeFieldName");
        Boolean b = (Boolean) cell.getPropertyMap().get("KeepFirst");
        keepFirst = (b == null ? true : b);
    }

    public String getMergeFieldName() {
        return mergeFieldName;
    }

    public void setMergeFieldName(String mergeFieldName) {
        this.mergeFieldName = mergeFieldName;
    }

    public boolean isKeepFirst() {
        return keepFirst;
    }

    public void setKeepFirst(boolean prefix) {
        this.keepFirst = keepFirst;
    }
}
