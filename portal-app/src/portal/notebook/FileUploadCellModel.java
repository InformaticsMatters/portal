package portal.notebook;


import portal.notebook.execution.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUploadCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<VariableModel> inputVariableModelList = Collections.emptyList();
    private final List<String> outputVariableNameList = new ArrayList<>();
    private String fileName;

    public FileUploadCellModel(CellType cellType) {
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


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void store(NotebookContents notebookContents, Cell cell) {
        storeHeader(cell);
        cell.getPropertyMap().put("fileName", fileName);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        fileName = (String)cell.getPropertyMap().get("fileName");
    }
}
