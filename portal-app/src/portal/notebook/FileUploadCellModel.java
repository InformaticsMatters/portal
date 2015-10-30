package portal.notebook;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUploadCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<VariableModel> inputVariableModelList = Collections.emptyList();
    private List<String> outputVariableNameList = Arrays.asList("resourceId");
    private String fileName;

    @Override
    public CellType getCellType() {
        return CellType.FILE_UPLOAD;
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
        Variable outputVariable = notebookContents.findVariable(cell.getName(), "resourceId");
        cell.getOutputVariableList().add(outputVariable);
        cell.getPropertyMap().put("fileName", fileName);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        fileName = (String)cell.getPropertyMap().get("fileName");
    }
}
