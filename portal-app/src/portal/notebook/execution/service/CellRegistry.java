package portal.notebook.execution.service;


import org.squonk.options.MultiLineTextTypeDescriptor;
import portal.notebook.api.*;
import portal.notebook.cells.ChemblActivitiesFetcherCell;
import portal.notebook.cells.SimpleCellDefinition;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class CellRegistry {
    private final Map<String, CellDefinition> cellDefinitionMap = new LinkedHashMap<>();
    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    public static final String VAR_NAME_RESULTS = "results";
    public static final String VAR_NAME_INPUT = "input";
    public static final String VAR_NAME_OUTPUT = "output";
    public static final String VAR_NAME_FILECONTENT = "fileContent";


    public CellRegistry() {

        registerCell(new ChemblActivitiesFetcherCell());
        registerCell(createTableDisplayCellDefinition());
        registerCell(createSdfUploaderCellDefinition());
        registerCell(createCsvUploaderCellDefinition());
        registerCell(createDatasetMergerCellDefinition());
        registerCell(createConvertBasicToMoleculeObjectCellDefinition());
        registerCell(createValueTransformerCellDefinition());
        registerCell(createGroovyScriptTrustedCellDefinition());

    }

    public void registerCell(CellDefinition cell) {
        cellDefinitionMap.put(cell.getName(), cell);
    }

    private static CellDefinition createDatasetMergerCellDefinition() {
        CellDefinition cellType = new SimpleCellDefinition();
        cellType.setName("DatasetMerger");
        cellType.setDescription("Dataset merger");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_RESULTS);
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.getOptionDefinitionList().add(new OptionDefinition(String.class, "mergeFieldName", "Merge field name",
                "Name of field to use to match items"));
        cellType.getOptionDefinitionList().add(new OptionDefinition(Boolean.class, "keepFirst", "Keep first",
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

    private static CellDefinition createCsvUploaderCellDefinition() {
        CellDefinition cellType = new SimpleCellDefinition();
        cellType.setName("CsvUploader");
        cellType.setDescription("CSV upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_FILECONTENT);
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_RESULTS);
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.getOptionDefinitionList().add(new OptionDefinition(String.class, OPTION_FILE_TYPE, "File type",
                "Type of CSV or TAB file")
                .withValues(new String[]{"TDF", "EXCEL", "MYSQL", "RFC4180", "DEFAULT"}).withDefaultValue("DEFAULT"));
        cellType.getOptionDefinitionList().add(new OptionDefinition(Boolean.class, OPTION_FIRST_LINE_IS_HEADER, "First line is header",
                "First line contains field names"));
        return cellType;
    }

    private static CellDefinition createSdfUploaderCellDefinition() {
        CellDefinition cellType = new SimpleCellDefinition();
        cellType.setName("SdfUploader");
        cellType.setDescription("SDF upload");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_FILECONTENT);
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_RESULTS);
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        cellType.getOptionDefinitionList().add(new OptionDefinition(String.class, "nameFieldName", "Name field´s name",
                "Field name to use for the molecule name (the part before the CTAB block").withMinValues(0));
        return cellType;
    }

    private static CellDefinition createTableDisplayCellDefinition() {
        CellDefinition cellType = new SimpleCellDefinition();
        cellType.setName("TableDisplay");
        cellType.setDescription("Table display");
        cellType.setExecutable(Boolean.FALSE);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.VALUE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        return cellType;
    }

//    private static CellDefinition createChemblActivitiesFetcherCellDefinition() {
//        CellDefinition cellType = new SimpleCellDefinition();
//        cellType.setName("ChemblActivitiesFetcher");
//        cellType.setDescription("Chembl activities fetcher");
//        cellType.setExecutable(Boolean.TRUE);
//        VariableDefinition variableDefinition = new VariableDefinition();
//        variableDefinition.setName(VAR_NAME_RESULTS);
//        variableDefinition.setDisplayName("Results");
//        variableDefinition.setVariableType(VariableType.DATASET);
//        cellType.getOutputVariableDefinitionList().add(variableDefinition);
//        cellType.getOptionDefinitionList().add(new OptionDefinition(String.class, "assayId", "Assay ID", "ChEBML Asssay ID"));
//        cellType.getOptionDefinitionList().add(new OptionDefinition(String.class, "prefix", "Prefix", "Prefix for result fields"));
//        cellType.setExecutable(Boolean.TRUE);
//
//        return cellType;
//    }


    private static CellDefinition createConvertBasicToMoleculeObjectCellDefinition() {
        CellDefinition cellType = new SimpleCellDefinition();
        cellType.setName("BasicObjectToMoleculeObject");
        cellType.setDescription("Convert Dataset from BasicObjects to MoleculeObjects");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        cellType.getOptionDefinitionList().add(new OptionDefinition(String.class, "structureFieldName", "Structure Field Name",
                "Name of property to use for the structure"));
        cellType.getOptionDefinitionList().add(new OptionDefinition(String.class, "structureFormat",
                "Structure Format", "Format of the structures e.g. smiles, mol")
                .withValues(new String[]{"smiles", "mol"}));
        cellType.getOptionDefinitionList().add(new OptionDefinition(Boolean.class, "preserveUuid", "Preserve UUID", "Keep the existing UUID or generate a new one").withMinValues(1));

        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellDefinition createValueTransformerCellDefinition() {
        CellDefinition cellType = new SimpleCellDefinition();
        cellType.setName("TransformValues");
        cellType.setDescription("Transform dataset values");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        cellType.getOptionDefinitionList().add(new OptionDefinition(new MultiLineTextTypeDescriptor(10, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "transformDefinitions", "Transform Definitions",
                "Definition of the transforms to perform"));
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    private static CellDefinition createGroovyScriptTrustedCellDefinition() {
        CellDefinition cellType = new SimpleCellDefinition();
        cellType.setName("TrustedGroovyDatasetScript");
        cellType.setDescription("Groovy Script (trusted)");
        cellType.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellType.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellType.getBindingDefinitionList().add(bindingDefinition);
        cellType.getOptionDefinitionList().add(new OptionDefinition(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Groovy Script", "Groovy script to execute"));
        cellType.setExecutable(Boolean.TRUE);
        return cellType;
    }

    public Collection<CellDefinition> listCellDefinition() {
        return cellDefinitionMap.values();
    }


    public CellDefinition retrieveCellDefinition(String name) {
        return cellDefinitionMap.get(name);
    }
}
