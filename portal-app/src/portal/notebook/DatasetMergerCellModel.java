package portal.notebook;

import com.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.List;

public class DatasetMergerCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();

    public DatasetMergerCellModel(CellType cellType) {
        super(cellType);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    public String getMergeFieldName() {
        return (String) getOptionMap().get("mergeFieldName");
    }

    public void setMergeFieldName(String mergeFieldName) {
        getOptionMap().put("mergeFieldName", mergeFieldName);
    }

    public Boolean isKeepFirst() {
        Boolean value = (Boolean) getOptionMap().get("keepFirst");
        return value == null ? true : value;
    }

    public void setKeepFirst(Boolean keepFirst) {
        getOptionMap().put("keepFirst", keepFirst);
    }
}
