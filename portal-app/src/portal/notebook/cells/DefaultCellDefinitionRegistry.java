package portal.notebook.cells;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;
import portal.notebook.api.BindingDefinition;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellDefinitionRegistry;
import portal.notebook.api.VariableType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.*;

@Default
@ApplicationScoped
public class DefaultCellDefinitionRegistry implements CellDefinitionRegistry {

    public static final String VAR_NAME_INPUT = "input";
    public static final String VAR_NAME_OUTPUT = "output";
    public static final String VAR_NAME_FILECONTENT = "fileContent";
    private static final Logger logger = LoggerFactory.getLogger(DefaultCellDefinitionRegistry.class);
    private final Map<String, CellDefinition> cellDefinitionMap = new LinkedHashMap<>();
    @Inject
    private SessionContext sessionContext;
    @Inject
    private ServiceCellsProvider serviceCellsProvider;

    public DefaultCellDefinitionRegistry() {
        registerCellDefinition(new ChemblActivitiesFetcherCellDefinition());
        registerCellDefinition(createTableDisplayCellDefinition());
        registerCellDefinition(createScatterPlotCellDefinition());
        registerCellDefinition(new CsvUploadCellDefinition());
        registerCellDefinition(new SdfUploadCellDefinition());
        registerCellDefinition(new DatasetMergerCellDefinition());
        registerCellDefinition(new ConvertToMoleculesCellDefinition());
        registerCellDefinition(new TransformValuesCellDefinition());
        registerCellDefinition(new ProcessDatasetTrustedGroovyScriptCellDefinition());
        registerCellDefinition(new ProcessDatasetUntrustedGroovyScriptCellDefinition());
    }

    private static CellDefinition createTableDisplayCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("TableDisplay", "Table display", "icons/view.png", new String[]{"table", "spreadsheet", "visualization", "visualisation", "viz"}, false);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STRING);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        return cellDefinition;
    }

    private static CellDefinition createScatterPlotCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("ScatterPlot", "Scatter plot", "icons/view.png", new String[]{"scatter", "plot", "visualization", "visualisation", "viz"}, false);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STRING);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        return cellDefinition;
    }

    public void registerCellDefinition(CellDefinition cellDefinition) {
        cellDefinitionMap.put(cellDefinition.getName(), cellDefinition);
    }

    public Collection<CellDefinition> listCellDefinition() {
        List<CellDefinition> definitionList = new ArrayList<>();
        definitionList.addAll(cellDefinitionMap.values());
        definitionList.addAll(serviceCellsProvider.listServiceCellDefinition());
        return definitionList;
    }

    public CellDefinition findCellDefinition(String name) {
        return cellDefinitionMap.get(name);
    }
}
