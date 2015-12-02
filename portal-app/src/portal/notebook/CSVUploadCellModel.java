package portal.notebook;

import com.squonk.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVUploadCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();

    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";

    private String csvFormatType;
    private boolean firstLineIsHeader = false;

    public CSVUploadCellModel(CellType cellType) {
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
        System.out.println("CSVUploadCellModel.store() -> csvFormatType=" + csvFormatType + " firstLineIsHeader=" + firstLineIsHeader);
        super.store(notebookContents, cell);
        cell.getPropertyMap().put(OPTION_FILE_TYPE, csvFormatType);
        cell.getPropertyMap().put(OPTION_FIRST_LINE_IS_HEADER, firstLineIsHeader);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        System.out.println("CSVUploadCellModel.load()");
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        setCsvFormatType((String) cell.getPropertyMap().get(OPTION_FILE_TYPE));
        if (cell.getPropertyMap().containsKey(OPTION_FIRST_LINE_IS_HEADER)) {
            setFirstLineIsHeader((Boolean)cell.getPropertyMap().get(OPTION_FIRST_LINE_IS_HEADER));
        }
    }

    @Override
    public void bindVariableModel(VariableModel sourceVariableModel, BindingTargetModel bindingTargetModel) {
        throw new UnsupportedOperationException();
    }

    public String getCsvFormatType() {
        System.out.println("CSVUploadCellModel.getCsvFormatType() -> " + csvFormatType);
        return csvFormatType;
    }

    public void setCsvFormatType(String type) {
        System.out.println("CSVUploadCellModel.setCsvFormatType() -> " + type);
        this.csvFormatType = type;
    }

    public boolean isFirstLineIsHeader() {
        System.out.println("CSVUploadCellModel.isFirstLineIsHeader() -> " + firstLineIsHeader);
        return firstLineIsHeader;
    }

    public void setFirstLineIsHeader(boolean b) {
        System.out.println("CSVUploadCellModel.setFirstLineIsHeader() -> " + b);
        this.firstLineIsHeader = b;
    }
}
