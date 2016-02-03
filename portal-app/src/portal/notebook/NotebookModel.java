package portal.notebook;

import portal.notebook.api.CellInstance;
import portal.notebook.api.NotebookInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotebookModel implements Serializable {
    private final NotebookInstance notebookInstance;
    private final Map<String, CellModel> cellModelMap = new HashMap<>();

    public NotebookModel(NotebookInstance notebookInstance) {
        this.notebookInstance = notebookInstance;
        loadCells();
    }

    public NotebookInstance getNotebookInstance() {
        return notebookInstance;
    }

    private void loadCells() {
        cellModelMap.clear();
        for (CellInstance cell : notebookInstance.getCellList()) {
            CellModel cellModel = new CellModel(cell, this);
            cellModelMap.put(cell.getName(), cellModel);
        }
        for (CellModel cellModel : cellModelMap.values()) {
            cellModel.loadBindings();
        }
    }

    public CellModel[] getCellModels() {
        return cellModelMap.values().toArray(new CellModel[0]);
    }

    public CellModel addCellModel(CellInstance cell) {
        CellModel cellModel = new CellModel(cell, this);
        cellModel.loadBindings();
        cellModelMap.put(cell.getName(), cellModel);
        return cellModel;
    }

    public void removeCellModel(CellModel cellModel) {
        for (CellModel targetCellModel : getCellModels()) {
            for (BindingModel bindingModel : targetCellModel.getBindingModelMap().values()) {
                if (bindingModel.getVariableModel() != null && bindingModel.getVariableModel().getProducerCellModel() == cellModel) {
                    bindingModel.setVariableModel(null);
                }
            }
        }
        cellModelMap.remove(cellModel.getName());
        notebookInstance.removeCell(cellModel.getName());
    }

    public CellModel findCellModelById(Long id) {
        for (CellModel cellModel : cellModelMap.values()) {
            if (cellModel.getId().equals(id)) {
                return cellModel;
            }
        }
        return null;
    }

    public VariableModel findVariableModel(Long cellId, String name) {
        CellModel cellModel = findCellModelById(cellId);
        return cellModel == null ? null : cellModel.findVariableModel(name);
    }

    public List<VariableModel> buildVariableModelList() {
        List<VariableModel> list = new ArrayList<>();
        for (CellModel cellModel : cellModelMap.values()) {
            for (VariableModel variableModel : cellModel.getOutputVariableModelMap().values()) {
                list.add(variableModel);
            }
        }
        return list;
    }
}
