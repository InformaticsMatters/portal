package portal.notebook;

import com.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.List;

public class CSVUploadCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();

    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";

    public CSVUploadCellModel(CellType cellType) {
        super(cellType);
    }


    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }


    public String getFileType() {
        return (String) getOptionMap().get(OPTION_FILE_TYPE);
    }

    public void setFileType(String fileType) {
        getOptionMap().put(OPTION_FILE_TYPE, fileType);
    }

    public Boolean getFirstLineIsHeader() {
        return (Boolean) getOptionMap().get(OPTION_FIRST_LINE_IS_HEADER);
    }

    public void setFirstLineIsHeader(Boolean firstLineIsHeader) {
        getOptionMap().put(OPTION_FIRST_LINE_IS_HEADER, firstLineIsHeader);
    }
}
