package portal.notebook.execution.service;

import org.squonk.notebook.api.*;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.notebook.client.CallbackContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("cell")
public class ExampleCellService {
    private static final List<CellType> CELL_TYPE_LIST = createDefinitions();
    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    @Inject
    private QndCellExecutorProvider qndCellExecutorProvider;
    @Inject
    private CallbackClient callbackClient;
    @Inject
    private CallbackContext callbackContext;

    private static List<CellType> createDefinitions() {
        List<CellType> list = new ArrayList<>();

        CellType cellType = createFileUploadCellType();
        list.add(cellType);

        cellType = createPropertyCalculateCellType();
        list.add(cellType);

        cellType = createChemblActivitiesFetcherCellType();
        list.add(cellType);

        cellType = createTableDisplayCellType();
        list.add(cellType);

        cellType = createScriptCellType();
        list.add(cellType);

        cellType = createSdfUploaderCellType();
        list.add(cellType);

        cellType = createCsvUploaderCellType();
        list.add(cellType);

        cellType = createDatasetMergerCellType();
        list.add(cellType);
        return list;
    }

    private static CellType createDatasetMergerCellType() {
        CellType cellType = new CellType();
        cellType.setName("DatasetMerger");
        cellType.setDescription("Dataset merger");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition<String> fieldNameOptionDefinition = new OptionDefinition<String>();
        fieldNameOptionDefinition.setName("mergeFieldName");
        fieldNameOptionDefinition.setDisplayName("Merge field name");
        fieldNameOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(fieldNameOptionDefinition);
        OptionDefinition<Boolean> keepFirstOptionDefinition = new OptionDefinition<>();
        keepFirstOptionDefinition.setName("keepFirst");
        keepFirstOptionDefinition.setDisplayName("Keep first");
        keepFirstOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(keepFirstOptionDefinition);
        for (int i = 0; i < 5; i++) {
            BindingDefinition bindingDefinition = new BindingDefinition();
            bindingDefinition.setDisplayName("Input dataset " + (i + 1));
            bindingDefinition.setName("input" + (i + 1));
            bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
            cellType.getBindingDefinitionList().add(bindingDefinition);
        }
        return cellType;
    }

    private static CellType createCsvUploaderCellType() {
        CellType cellType = new CellType();
        cellType.setName("CsvUploader");
        cellType.setDescription("CSV upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("fileContent");
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition<String> fileTypeOptionDefinition = new OptionDefinition<String>();
        fileTypeOptionDefinition.setName(OPTION_FILE_TYPE);
        fileTypeOptionDefinition.setDisplayName("File type");
        fileTypeOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(fileTypeOptionDefinition);
        OptionDefinition<Boolean> firstLineIsHeaderOptionDefinition = new OptionDefinition<Boolean>();
        firstLineIsHeaderOptionDefinition.setName(OPTION_FIRST_LINE_IS_HEADER);
        firstLineIsHeaderOptionDefinition.setDisplayName("First line is header");
        firstLineIsHeaderOptionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(firstLineIsHeaderOptionDefinition);
        return cellType;
    }

    private static CellType createSdfUploaderCellType() {
        CellType cellType = new CellType();
        cellType.setName("SdfUploader");
        cellType.setDescription("SDF upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("fileContent");
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition optionDefinition = new OptionDefinition();
        optionDefinition.setName("nameFieldName");
        optionDefinition.setDisplayName("Name field´s name");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        return cellType;
    }

    private static CellType createScriptCellType() {
        CellType cellType = new CellType();
        cellType.setName("Script");
        cellType.setDescription("Script");
        cellType.setExecutable(Boolean.TRUE);
        OptionDefinition<String> optionDefinition = new OptionDefinition<String>();
        optionDefinition.setName("code");
        optionDefinition.setDisplayName("Code");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        optionDefinition = new OptionDefinition<String>();
        optionDefinition.setName("errorMessage");
        optionDefinition.setDisplayName("Error message");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("outcome");
        variableDefinition.setDisplayName("Outcome");
        variableDefinition.setVariableType(VariableType.VALUE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        return cellType;

    }

    private static CellType createTableDisplayCellType() {
        CellType cellType = new CellType();
        cellType.setName("TableDisplay");
        cellType.setDescription("Table display");
        cellType.setExecutable(Boolean.FALSE);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.VALUE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        return cellType;
    }

    private static CellType createChemblActivitiesFetcherCellType() {
        CellType cellType = new CellType();
        cellType.setName("ChemblActivitiesFetcher");
        cellType.setDescription("Chembl activities fetcher");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("results");
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition optionDefinition = new OptionDefinition();
        optionDefinition.setName("assayId");
        optionDefinition.setDisplayName("Assay ID");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        optionDefinition = new OptionDefinition();
        optionDefinition.setName("prefix");
        optionDefinition.setDisplayName("Prefix");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellType createPropertyCalculateCellType() {
        CellType cellType = new CellType();
        cellType.setName("PropertyCalculate");
        cellType.setDescription("Property calc.");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("outputFile");
        variableDefinition.setDisplayName("Output file");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input file");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        OptionDefinition<String> optionDefinition = new OptionDefinition<String>();
        optionDefinition.setName("serviceName");
        optionDefinition.setDisplayName("Service");
        optionDefinition.setOptionType(OptionType.PICKLIST);
        for (String serviceName : CalculatorsClient.getServiceNames()) {
            optionDefinition.getPicklistValueList().add(serviceName);
        }
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellType createFileUploadCellType() {
        CellType cellType = new CellType();
        cellType.setName("FileUpload");
        cellType.setDescription("File upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("file");
        variableDefinition.setDisplayName("Uploaded file");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        OptionDefinition<String> optionDefinition = new OptionDefinition<String>();
        optionDefinition.setName("fileName");
        optionDefinition.setDisplayName("Output file name");
        optionDefinition.setOptionType(OptionType.SIMPLE);
        cellType.getOptionDefinitionList().add(optionDefinition);
        cellType.setExecutable(Boolean.FALSE);
        return cellType;
    }

    @Path("listCellType")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CellType> listCellType() {
        return CELL_TYPE_LIST;
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
        for (CellType cellType : CELL_TYPE_LIST) {
            if (cellType.getName().equals(name)) {
                return cellType;
            }
        }
        return null;
    }
}
