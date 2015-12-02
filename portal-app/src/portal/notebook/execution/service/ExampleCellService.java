package portal.notebook.execution.service;

import com.squonk.notebook.api.CellDTO;
import com.squonk.notebook.api.CellType;
import com.squonk.notebook.api.VariableDefinition;
import com.squonk.notebook.api.VariableType;
import com.squonk.notebook.client.CallbackClient;
import com.squonk.notebook.client.CallbackContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("cell")
public class ExampleCellService {
    private static final List<CellType> CELL_TYPE_DESCRIPTOR_LIST = createDescriptors();
    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    @Inject
    private QndCellExecutorProvider qndCellExecutorProvider;
    @Inject
    private CallbackClient callbackClient;
    @Inject
    private CallbackContext callbackContext;

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

        list.add(new CellType("SdfUploader", "SDF upload", true)
                .withOutputVariable("FileContent", VariableType.FILE)
                .withOutputVariable("Results", VariableType.DATASET)
                .withOption("NameFieldName"));

        list.add(new CellType("CsvUploader", "CSV upload", true)
                .withOutputVariable("FileContent", VariableType.FILE)
                .withOutputVariable("Results", VariableType.DATASET)
                .withOption(OPTION_FILE_TYPE)
                .withOption(OPTION_FIRST_LINE_IS_HEADER));

        list.add(new CellType("DatasetMerger", "Dataset merger", true)
                .withOutputVariable("Results", VariableType.DATASET)
                .withOption("MergeFieldName")
                .withOption("KeepFirst"));

        return list;
    }

    @Path("listCellType")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CellType> listCellType() {
        return CELL_TYPE_DESCRIPTOR_LIST;
    }

    @Path("executeCell")
    @POST
    public void executeCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
        callbackContext.setNotebookId(notebookId);
        CellDTO cell = callbackClient.retrieveCell(cellName);
        qndCellExecutorProvider.resolveCellHandler(cell.getCellType()).execute(cellName);
    }

    @Path("retrieveCellType")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CellType retrieveCellType(@QueryParam("name") String name) {
        for (CellType cellType : CELL_TYPE_DESCRIPTOR_LIST) {
            if (cellType.getName().equals(name)) {
                return cellType;
            }
        }
        return null;
    }
}
