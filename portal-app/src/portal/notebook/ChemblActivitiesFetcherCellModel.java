package portal.notebook;

import com.squonk.notebook.api.CellType;
import portal.notebook.service.Cell;
import portal.notebook.service.NotebookContents;
import portal.notebook.service.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChemblActivitiesFetcherCellModel extends AbstractCellModel {
    private static final long serialVersionUID = 1l;
    private final List<String> outputVariableNameList = new ArrayList<>();
    private String assayId;
    private String prefix;

    public ChemblActivitiesFetcherCellModel(CellType cellType) {
        super(cellType);
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
        super.store(notebookContents, cell);
        cell.getPropertyMap().put("assayId", assayId);
        cell.getPropertyMap().put("prefix", prefix);
    }

    @Override
    public void load(NotebookModel notebookModel, Cell cell) {
        loadHeader(cell);
        outputVariableNameList.clear();
        for (Variable variable : cell.getOutputVariableList()) {
            outputVariableNameList.add(variable.getName());
        }
        assayId = (String) cell.getPropertyMap().get("assayId");
        prefix = (String) cell.getPropertyMap().get("prefix");
    }

    public String getAssayId() {
        return assayId;
    }

    public void setAssayId(String assayId) {
        this.assayId = assayId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
