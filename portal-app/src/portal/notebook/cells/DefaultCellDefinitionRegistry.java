package portal.notebook.cells;


import org.squonk.options.MultiLineTextTypeDescriptor;
import portal.notebook.api.*;
import toolkit.test.TestCase;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Alternative
@ApplicationScoped
public class DefaultCellDefinitionRegistry implements CellDefinitionRegistry {
    private final Map<String, CellDefinition> cellDefinitionMap = new LinkedHashMap<>();
    public static final String OPTION_FILE_TYPE = "csvFormatType";
    public static final String OPTION_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    public static final String VAR_NAME_RESULTS = "results";
    public static final String VAR_NAME_INPUT = "input";
    public static final String VAR_NAME_OUTPUT = "output";
    public static final String VAR_NAME_FILECONTENT = "fileContent";

    public DefaultCellDefinitionRegistry() {

        registerCellDefinition(new ChemblActivitiesFetcherCellDefinition());
        registerCellDefinition(createTableDisplayCellDefinition());
        registerCellDefinition(createSdfUploaderCellDefinition());
        registerCellDefinition(createCsvUploaderCellDefinition());
        registerCellDefinition(createDatasetMergerCellDefinition());
        registerCellDefinition(createConvertBasicToMoleculeObjectCellDefinition());
        registerCellDefinition(createValueTransformerCellDefinition());
        registerCellDefinition(createGroovyScriptTrustedCellDefinition());

    }

    public void registerCellDefinition(CellDefinition cellDefinition) {
        cellDefinitionMap.put(cellDefinition.getName(), cellDefinition);
    }

    private static CellDefinition createDatasetMergerCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("DatasetMerger");
        cellDefinition.setDescription("Dataset merger");
        cellDefinition.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_RESULTS);
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(String.class, "mergeFieldName", "Merge field name",
                "Name of field to use to match items"));
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(Boolean.class, "keepFirst", "Keep first",
                "When merging keep the original value (or the new one)"));
        for (int i = 0; i < 5; i++) {
            BindingDefinition bindingDefinition = new BindingDefinition();
            bindingDefinition.setDisplayName("Input dataset " + (i + 1));
            bindingDefinition.setName("input" + (i + 1));
            bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
            cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        }
        return cellDefinition;
    }

    private static CellDefinition createCsvUploaderCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("CsvUploader");
        cellDefinition.setDescription("CSV upload");
        cellDefinition.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_FILECONTENT);
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_RESULTS);
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(String.class, OPTION_FILE_TYPE, "File type",
                "Type of CSV or TAB file")
                .withValues(new String[]{"TDF", "EXCEL", "MYSQL", "RFC4180", "DEFAULT"}).withDefaultValue("DEFAULT"));
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(Boolean.class, OPTION_FIRST_LINE_IS_HEADER, "First line is header",
                "First line contains field names"));
        return cellDefinition;
    }

    private static CellDefinition createSdfUploaderCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("SdfUploader");
        cellDefinition.setDescription("SDF upload");
        cellDefinition.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_FILECONTENT);
        variableDefinition.setDisplayName("File content");
        variableDefinition.setVariableType(VariableType.FILE);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_RESULTS);
        variableDefinition.setDisplayName("Results");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(String.class, "nameFieldName", "Name field´s name",
                "Field name to use for the molecule name (the part before the CTAB block").withMinValues(0));
        return cellDefinition;
    }

    private static CellDefinition createTableDisplayCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("TableDisplay");
        cellDefinition.setDescription("Table display");
        cellDefinition.setExecutable(Boolean.FALSE);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.VALUE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        return cellDefinition;
    }

//    private static CellDefinition createChemblActivitiesFetcherCellDefinition() {
//        CellDefinition cellDefinition = new SimpleCellDefinition();
//        cellDefinition.setName("ChemblActivitiesFetcher");
//        cellDefinition.setDescription("Chembl activities fetcher");
//        cellDefinition.setExecutable(Boolean.TRUE);
//        VariableDefinition variableDefinition = new VariableDefinition();
//        variableDefinition.setName(VAR_NAME_RESULTS);
//        variableDefinition.setDisplayName("Results");
//        variableDefinition.setVariableType(VariableType.DATASET);
//        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
//        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(String.class, "assayId", "Assay ID", "ChEBML Asssay ID"));
//        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(String.class, "prefix", "Prefix", "Prefix for result fields"));
//        cellDefinition.setExecutable(Boolean.TRUE);
//
//        return cellDefinition;
//    }


    private static CellDefinition createConvertBasicToMoleculeObjectCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("BasicObjectToMoleculeObject");
        cellDefinition.setDescription("Convert Dataset from BasicObjects to MoleculeObjects");
        cellDefinition.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(String.class, "structureFieldName", "Structure Field Name",
                "Name of property to use for the structure"));
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(String.class, "structureFormat",
                "Structure Format", "Format of the structures e.g. smiles, mol")
                .withValues(new String[]{"smiles", "mol"}));
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(Boolean.class, "preserveUuid", "Preserve UUID", "Keep the existing UUID or generate a new one").withMinValues(1));

        cellDefinition.setExecutable(Boolean.TRUE);
        return cellDefinition;
    }

    private static CellDefinition createValueTransformerCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("TransformValues");
        cellDefinition.setDescription("Transform dataset values");
        cellDefinition.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(new MultiLineTextTypeDescriptor(10, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "transformDefinitions", "Transform Definitions",
                "Definition of the transforms to perform"));
        cellDefinition.setExecutable(Boolean.TRUE);
        return cellDefinition;
    }

    private static CellDefinition createGroovyScriptTrustedCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition();
        cellDefinition.setName("TrustedGroovyDatasetScript");
        cellDefinition.setDescription("Groovy Script (trusted)");
        cellDefinition.setExecutable(Boolean.TRUE);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName(VAR_NAME_OUTPUT);
        variableDefinition.setDisplayName("Output");
        variableDefinition.setVariableType(VariableType.DATASET);
        cellDefinition.getOutputVariableDefinitionList().add(variableDefinition);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        cellDefinition.getOptionDefinitionList().add(new OptionDefinition(
                new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_GROOVY),
                "script", "Groovy Script", "Groovy script to execute"));
        cellDefinition.setExecutable(Boolean.TRUE);
        return cellDefinition;
    }

    public Collection<CellDefinition> listCellDefinition() {
        return cellDefinitionMap.values();
    }


    public CellDefinition retrieveCellDefinition(String name) {
        return cellDefinitionMap.get(name);
    }
}
