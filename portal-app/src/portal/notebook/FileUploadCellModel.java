package portal.notebook;


import com.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.List;

public class FileUploadCellModel extends DefaultCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();
    private String fileName;

    public FileUploadCellModel(CellType cellType) {
        super(cellType);
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
