package portal.notebook;


import com.squonk.notebook.api.CellType;

import java.util.ArrayList;
import java.util.List;

public class PropertyCalculateCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();

    public PropertyCalculateCellModel(CellType cellType) {
        super(cellType);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    public void setServiceName(String serviceName) {
        getOptionMap().put("serviceName", serviceName);
    }

    public String getServiceName() {
        return (String) getOptionMap().get("serviceName");
    }

}
