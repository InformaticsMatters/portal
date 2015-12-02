package portal.notebook;

import com.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.List;

public class ChemblActivitiesFetcherCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();

    public ChemblActivitiesFetcherCellModel(CellType cellType) {
        super(cellType);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }


    public String getAssayId() {
        return (String) getOptionMap().get("assayId");
    }

    public void setAssayId(String assayId) {
        getOptionMap().put("assayId", assayId);
    }

    public String getPrefix() {
        return (String) getOptionMap().get("prefix");
    }

    public void setPrefix(String prefix) {
        getOptionMap().put("prefix", prefix);
    }
}
