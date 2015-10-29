package portal.notebook;


import java.util.Collections;
import java.util.List;

public class PropertyCalculateCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private VariableModel inputVariableModel;
    private final List<String> outputVariableNameList = Collections.singletonList("outputFileName");
    private String serviceName;

    @Override
    public CellType getCellType() {
        return CellType.PROPERTY_CALCULATE;
    }

    @Override
    public List<VariableModel> getInputVariableModelList() {
        return inputVariableModel == null ? Collections.emptyList() : Collections.singletonList(inputVariableModel);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return outputVariableNameList;
    }

    public VariableModel getInputVariableModel() {
        return inputVariableModel;
    }

    public void setInputVariableModel(VariableModel inputVariableModel) {
        this.inputVariableModel = inputVariableModel;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
