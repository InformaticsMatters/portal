package portal.notebook;


import portal.notebook.AbstractCell;
import portal.notebook.CellType;
import portal.notebook.Variable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUploadCell extends AbstractCell {
    private static final long serialVersionUID = 1l;
    private final List<Variable> inputVariableList = Collections.emptyList();
    private List<String> outputVariableNameList = Arrays.asList("resourceId");
    private String fileName;

    @Override
    public CellType getCellType() {
        return CellType.FILE_UPLOAD;
    }

    @Override
    public List<Variable> getInputVariableList() {
        return inputVariableList;
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

}
