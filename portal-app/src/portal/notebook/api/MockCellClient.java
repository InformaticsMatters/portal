package portal.notebook.api;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Alternative
@SessionScoped
public class MockCellClient implements CellClient {
    private static final List<CellType> CELL_TYPE_DESCRIPTOR_LIST = createDescriptors();
    @Inject
    private CallbackClient callbackClient;
    @Inject
    private CallbackContext callbackContext;
    @Inject
    private QnDCellExecutorProvider qnDCellExecutorProvider;

    private static List<CellType> createDescriptors() {
        List<CellType> list = new ArrayList<>();

        CellType cellType = new CellType();
        cellType.setName("FileUpload");
        cellType.setDescription("File upload");
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("file");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.setExecutable(Boolean.FALSE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("PropertyCalculate");
        cellType.setDescription("Property calc.");
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("outputFile");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.setExecutable(Boolean.TRUE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("ChemblActivitiesFetcher");
        cellType.setDescription("Chembl activities fetcher");
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.getOptionNameList().add("assayId");
        cellType.getOptionNameList().add("prefix");
        cellType.setExecutable(Boolean.TRUE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("TableDisplay");
        cellType.setDescription("Table display");
        cellType.setExecutable(Boolean.FALSE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("Script");
        cellType.setDescription("Script");
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("outcome");
        variableDefinition.setVariableType(VariableType.VALUE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.setExecutable(Boolean.TRUE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("Sample1");
        cellType.setDescription("Produce number \"1\"");
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("number");
        variableDefinition.setVariableType(VariableType.VALUE);
        variableDefinition.setDefaultValue(1);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.setExecutable(Boolean.FALSE);
        list.add(cellType);

        cellType = new CellType();
        cellType.setName("Sample2");
        cellType.setDescription("Sum input plus option");
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("result");
        variableDefinition.setVariableType(VariableType.VALUE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.getOptionNameList().add("number2");
        cellType.setExecutable(Boolean.TRUE);
        list.add(cellType);

        return list;
    }

    @Override
    public List<CellType> listCellType() {
        return CELL_TYPE_DESCRIPTOR_LIST;
    }

    @Override
    public void executeCell(Long notebookId, String cellName) {
        callbackContext.setNotebookId(notebookId);
        CellDTO cell = callbackClient.retrieveCell(cellName);
        qnDCellExecutorProvider.resolveCellHandler(cell.getCellType()).execute(cellName);
    }

    @Override
    public CellType retrieveCellType(String name) {
        for (CellType cellType : CELL_TYPE_DESCRIPTOR_LIST) {
            if (cellType.getName().equals(name)) {
                return cellType;
            }
        }
        return null;
    }
}
