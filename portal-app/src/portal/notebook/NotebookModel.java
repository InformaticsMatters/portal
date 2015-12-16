package portal.notebook;

import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotebookModel implements Serializable {
    private final NotebookContents notebookContents;
    private final Map<String, CellModel> cellModelMap = new HashMap<>();

    public NotebookModel(NotebookContents notebookContents) {
        this.notebookContents = notebookContents;
        loadCells();
    }

    public NotebookContents getNotebookContents() {
        return notebookContents;
    }

    private void loadCells() {
        cellModelMap.clear();
        for (Cell cell : notebookContents.getCellList()) {
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

    public CellModel addCellModel(Cell cell) {
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
        notebookContents.removeCell(cellModel.getName());
    }

    public CellModel findCellModel(String name) {
        return cellModelMap.get(name);
    }

    public VariableModel findVariableModel(String cellName, String name) {
        CellModel cellModel = findCellModel(cellName);
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
