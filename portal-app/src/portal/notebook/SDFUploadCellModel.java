package portal.notebook;

import com.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.List;

public class SDFUploadCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();

    public SDFUploadCellModel(CellType cellType) {
        super(cellType);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }


    public String getNameFieldName() {
        return (String) getOptionMap().get("nameFieldName");
    }

    public void setNameFieldName(String nameFieldName) {
        getOptionMap().put("nameFieldName", nameFieldName);
    }

}
