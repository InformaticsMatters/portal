package portal.notebook.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.options.OptionDescriptor;
import portal.SessionContext;
import portal.notebook.webapp.AbstractD3CanvasItemPanel;
import portal.notebook.webapp.ScatterPlotCanvasItemPanel;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Default
@ApplicationScoped
public class DefaultCellDefinitionRegistry implements CellDefinitionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCellDefinitionRegistry.class);
    private final Map<String, CellDefinition> cellDefinitionMap = new LinkedHashMap<>();
    private boolean serviceCellsRegistered = false;
    @Inject
    private SessionContext sessionContext;
    @Inject
    private ServiceCellsProvider serviceCellsProvider;

    private static CellDefinition createTableDisplayCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("TableDisplay", "Table display", "icons/visualisation_table.png", new String[]{"table", "spreadsheet", "visualization", "visualisation", "viz"}, false);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(CellDefinition.VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STRING);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("selection");
        variableDefinition.setDisplayName("Selection");
        variableDefinition.setVariableType(VariableType.STRING);
        cellDefinition.getVariableDefinitionList().add(variableDefinition);
        return cellDefinition;
    }

    private static CellDefinition createScatterPlotCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("ScatterPlot", "Scatter plot", "icons/visualisation_chart.png", new String[]{"scatter", "plot", "visualization", "visualisation", "viz"}, false);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(CellDefinition.VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STRING);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_X_AXIS, "x Axis", "Field to use for x axis values"));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_X_AXIS, "y Axis", "Field to use for y axis values"));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, ScatterPlotCanvasItemPanel.OPTION_COLOR, "Color", "Field to use as color switch"));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, ScatterPlotCanvasItemPanel.OPTION_POINT_SIZE, "Point size", "Size of points on plot"));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(Boolean.class, ScatterPlotCanvasItemPanel.OPTION_AXIS_LABELS, "Show axis labels", "Controls whether the axis labels are visible"));
        return cellDefinition;
    }

    private static CellDefinition create3DMolCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("3DMol", "3D viewer", "icons/view.png", new String[]{"3d", "viewer", "visualization", "visualisation", "viz"}, false);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(CellDefinition.VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STRING);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        return cellDefinition;
    }

    private static CellDefinition createBoxPlotCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("BoxPlot", "Box plot", "icons/visualisation_chart.png", new String[]{"box", "plot", "visualization", "visualisation", "viz"}, false);
        BindingDefinition bindingDefinition = new BindingDefinition();
        bindingDefinition.setDisplayName("Input");
        bindingDefinition.setName(CellDefinition.VAR_NAME_INPUT);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.FILE);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STREAM);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.STRING);
        bindingDefinition.getAcceptedVariableTypeList().add(VariableType.DATASET);
        cellDefinition.getBindingDefinitionList().add(bindingDefinition);
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_X_AXIS,
                        "x Axis", "Field to use to group values"));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_Y_AXIS,
                        "y Axis", "Field for values"));
        return cellDefinition;
    }

    @PostConstruct
    public void init() {
        registerStandardCellDefinitions();
        registerCustomCellDefinitions();
        registerServiceCellDefinitions();
    }

    public CellDefinition findCellDefinition(String name) {
        return cellDefinitionMap.get(name);
    }

    public Collection<CellDefinition> listCellDefinition() {
        if (cellDefinitionMap.isEmpty()) {
            registerStandardCellDefinitions();
            registerCustomCellDefinitions();
            registerServiceCellDefinitions();
        }
        if (!serviceCellsRegistered) {
            registerServiceCellDefinitions();
        }
       return cellDefinitionMap.values();
    }

    private void registerStandardCellDefinitions() {
        registerCellDefinition(new ChemblActivitiesFetcherCellDefinition());
        registerCellDefinition(new CsvUploadCellDefinition());
        registerCellDefinition(new SdfUploadCellDefinition());
        registerCellDefinition(new DatasetMergerCellDefinition());
        registerCellDefinition(new ConvertToMoleculesCellDefinition());
        registerCellDefinition(new DatasetFilterGroovyCellDefinition());
        registerCellDefinition(new TransformValuesCellDefinition());
        registerCellDefinition(new DatasetSelectSliceCellDefinition());
        registerCellDefinition(new DatasetSelectRandomCellDefinition());
        registerCellDefinition(new ProcessDatasetUntrustedGroovyScriptCellDefinition());
        registerCellDefinition(new SmilesDeduplicatorCellDefinition());
        registerCellDefinition(new DatasetDockerProcessorCellDefinition());
        registerCellDefinition(new CxnReactorCellDefinition());
    }

    private void registerCustomCellDefinitions() {
        registerCellDefinition(createTableDisplayCellDefinition());
        registerCellDefinition(createScatterPlotCellDefinition());
        registerCellDefinition(createBoxPlotCellDefinition());
        registerCellDefinition(create3DMolCellDefinition());
    }

    private void registerServiceCellDefinitions() {
        List<ServiceCellDefinition> serviceCellDefinitions = serviceCellsProvider.listServiceCellDefinition();
        for (CellDefinition cellDefinition : serviceCellDefinitions) {
            registerCellDefinition(cellDefinition);
        }
        serviceCellsRegistered = serviceCellDefinitions.size() > 0;
    }

    public void registerCellDefinition(CellDefinition cellDefinition) {
        cellDefinitionMap.put(cellDefinition.getName(), cellDefinition);
    }
}
