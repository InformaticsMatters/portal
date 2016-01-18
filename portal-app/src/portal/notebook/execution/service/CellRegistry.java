package portal.notebook.execution.service;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.*;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.notebook.client.CallbackContext;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@ApplicationScoped
public class CellRegistry {
    private final Map<String,CellType> CELL_TYPES = new LinkedHashMap<>();
    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";


    public CellRegistry() {

        registerCell(createChemblActivitiesFetcherCellType());
        registerCell(createTableDisplayCellType());
        registerCell(createSdfUploaderCellType());
        registerCell(createCsvUploaderCellType());
        registerCell(createDatasetMergerCellType());
        registerCell(createConvertBasicToMoleculeObjectCellType());
        registerCell(createValueTransformerCellType());
        registerCell(createGroovyScriptTrustedCellType());

    }

    public void registerCell(CellType cell) {
        CELL_TYPES.put(cell.getName(), cell);
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "mergeFieldName", "Merge field name",
                "Name of field to use to match items"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(Boolean.class, "keepFirst", "Keep first",
                "When merging keep the original value (or the new one)"));
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, OPTION_FILE_TYPE, "File type",
                "Type of CSV or TAB file")
                .withValues(new String [] {"TDF", "EXCEL", "MYSQL", "RFC4180", "DEFAULT"}).withDefaultValue("DEFAULT"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(Boolean.class, OPTION_FIRST_LINE_IS_HEADER, "First line is header",
                "First line contains field names"));
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "nameFieldName", "Name field´s name",
                "Field name to use for the molecule name (the part before the CTAB block").withMinValues(0));
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
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "assayId", "Assay ID", "ChEBML Asssay ID"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "prefix", "Prefix", "Prefix for result fields"));
        cellType.setExecutable(Boolean.TRUE);

//        StepDefinition step1 = new  StepDefinition(StepDefinitionConstants.STEP_CHEMBL_ACTIVITIES_FETCHER)
//                .withOutputVariableMapping()
//                .withOption()

        return cellType;
    }


    private static CellType createConvertBasicToMoleculeObjectCellType() {
        CellType cellType = new CellType();
        cellType.setName("BasicObjectToMoleculeObject");
        cellType.setDescription("Convert Dataset from BasicObjects to MoleculeObjects");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("output");
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "structureFieldName", "Structure Field Name",
                "Name of property to use for the structure"));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(String.class, "structureFormat",
                "Structure Format", "Format of the structures e.g. smiles, mol")
                .withValues(new String[] {"smiles", "mol"}));
        cellType.getOptionDefinitionList().add(new OptionDescriptor(Boolean.class, "preserveUuid", "Preserve UUID", "Keep the existing UUID or generate a new one").withMinValues(1));

        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellType createValueTransformerCellType() {
        CellType cellType = new CellType();
        cellType.setName("TransformValues");
        cellType.setDescription("Transform dataset values");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("output");
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        cellType.getOptionDefinitionList().add(new OptionDescriptor(new MultiLineTextTypeDescriptor(10, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "transformDefinitions", "Transform Definitions",
                "Definition of the transforms to perform"));
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellType createGroovyScriptTrustedCellType() {
        CellType cellType = new CellType();
        cellType.setName("TrustedGroovyDatasetScript");
        cellType.setDescription("Groovy Script (trusted)");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("output");
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName("input");
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        cellType.getOptionDefinitionList().add(new OptionDescriptor(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Groovy Script", "Groovy script to execute"));
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    public Collection<CellType> listCellType() {
        return CELL_TYPES.values();
    }


    public CellType retrieveCellType(String name) {
        return CELL_TYPES.get(name);
    }
}
