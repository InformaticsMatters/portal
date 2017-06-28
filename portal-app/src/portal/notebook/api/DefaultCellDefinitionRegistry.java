package portal.notebook.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetSelection;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.types.*;
import portal.SessionContext;
import portal.notebook.webapp.*;
import portal.notebook.webapp.cell.visual.AbstractD3CanvasItemPanel;
import portal.notebook.webapp.cell.visual.heatmap.HeatmapCanvasItemPanel;
import portal.notebook.webapp.cell.visual.ngl.NglViewerCanvasItemPanel;
import portal.notebook.webapp.cell.visual.parallelcoordinateplot.ParallelCoordinatePlotCanvasItemPanel;
import portal.notebook.webapp.cell.visual.scatterplot.ScatterPlotCanvasItemPanel;

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
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input", Dataset.class, MoleculeObject.class));
        cellDefinition.getVariableDefinitionList().add(IODescriptors.createString("selection"));

        // option inputs
        OptionDescriptor filterOptionDescriptor = new OptionDescriptor<>(DatasetSelection.class, CanvasItemPanel.OPTION_FILTER_IDS, "Filter", "Filter (IDs to include)", Mode.Input);
        cellDefinition.getOptionDefinitionList().add(filterOptionDescriptor);

        // the option bindings
        cellDefinition.getOptionBindingDefinitionList().add(new OptionBindingDefinition(filterOptionDescriptor, CellDefinition.UpdateMode.AUTO));

        return cellDefinition;
    }

    private static CellDefinition createScatterPlotCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("ScatterPlot", "Scatter plot", "icons/visualisation_chart.png", new String[]{"scatter", "plot", "visualization", "visualisation", "viz"}, false);

        // the data bindings
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input", Dataset.class, BasicObject.class));

        // these ones are the options that appear in the advanced options dialog
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_X_AXIS, "x Axis", "Field to use for x axis values", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_Y_AXIS, "y Axis", "Field to use for y axis values", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, ScatterPlotCanvasItemPanel.OPTION_COLOR, "Color", "Field to use as color switch", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, ScatterPlotCanvasItemPanel.OPTION_POINT_SIZE, "Point size", "Size of points on plot", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(Boolean.class, ScatterPlotCanvasItemPanel.OPTION_AXIS_LABELS, "Show axis labels", "Controls whether the axis labels are visible", Mode.Advanced)
                        .withDefaultValue(true));

        // option outputs - these are related to selection of points in the plot
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(NumberRange.class, ScatterPlotCanvasItemPanel.OPTION_SELECTED_X_RANGE, "Selected X", "Selected X range", Mode.Ignore));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(NumberRange.class, ScatterPlotCanvasItemPanel.OPTION_SELECTED_Y_RANGE, "Selected Y", "Selected Y range", Mode.Ignore));
        cellDefinition.getOptionDefinitionList().add(new OptionDescriptor<>(DatasetSelection.class, CanvasItemPanel.OPTION_SELECTED_IDS, "Selection", "Selected IDs", Mode.Output));

        // option inputs
        OptionDescriptor filterOptionDescriptor = new OptionDescriptor<>(DatasetSelection.class, CanvasItemPanel.OPTION_FILTER_IDS, "Filter", "Filter (IDs to include)", Mode.Input);
        cellDefinition.getOptionDefinitionList().add(filterOptionDescriptor);
        OptionDescriptor markedOptionDescriptor = new OptionDescriptor<>(DatasetSelection.class, CanvasItemPanel.OPTION_MARKED_IDS, "Marked", "Marked (IDs to highlight)", Mode.Input);
        cellDefinition.getOptionDefinitionList().add(markedOptionDescriptor);

        // the option bindings
        cellDefinition.getOptionBindingDefinitionList().add(new OptionBindingDefinition(filterOptionDescriptor, CellDefinition.UpdateMode.AUTO));
        cellDefinition.getOptionBindingDefinitionList().add(new OptionBindingDefinition(markedOptionDescriptor, CellDefinition.UpdateMode.AUTO));

        return cellDefinition;
    }

    private static CellDefinition create3DMolCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("3DMol", "3D viewer", "icons/view.png", new String[]{"3d", "3dmol", "viewer", "visualization", "vizualisation", "viz"}, false);
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input", Dataset.class, BasicObject.class, String.class, null));
        return cellDefinition;
    }

    private static CellDefinition createNglViewerCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("NGLViewer", "NGL viewer", "icons/view.png", new String[]{"3d", "nglviewer", "viewer", "visualization", "vizualisation", "viz"}, false);
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input2", Dataset.class, MoleculeObject.class, PDBFile.class, null, Mol2File.class, null));
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input1", Dataset.class, MoleculeObject.class, PDBFile.class, null, Mol2File.class, null));

        addFilterOption(cellDefinition, CanvasItemPanel.OPTION_FILTER_IDS + "2", "Filter2", "Filter2 (IDs to include)");
        addFilterOption(cellDefinition, CanvasItemPanel.OPTION_FILTER_IDS + "1", "Filter1", "Filter1 (IDs to include)");

        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, CanvasItemPanel.OPTION_CONFIG, "Config", "Viewer configuration", Mode.User));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, NglViewerCanvasItemPanel.OPTION_DISPLAY1, "Display1", "Viewer display 1", Mode.User));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, NglViewerCanvasItemPanel.OPTION_DISPLAY2, "Display2", "Viewer display 2", Mode.User));

        return cellDefinition;
    }

    private static CellDefinition createBoxPlotCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("BoxPlot", "Box plot", "icons/visualisation_chart.png", new String[]{"box", "plot", "visualization", "visualisation", "viz"}, false);
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input", Dataset.class, BasicObject.class));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_X_AXIS,
                        "x Axis", "Field to use to group values", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_Y_AXIS,
                        "y Axis", "Field for values", Mode.Advanced));

        addFilterOption(cellDefinition);

        return cellDefinition;
    }

    private static CellDefinition createParallelCoordinatePlotCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("ParallelCoordinatePlot", "Parallel coordinate plot", "icons/visualisation_chart.png",
                new String[]{"parallel", "coordinate", "plot", "visualization", "visualisation", "viz"}, false);
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input", Dataset.class, BasicObject.class));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_X_AXIS,
                        "x Axis", "Field to use to group values", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_Y_AXIS,
                        "y Axis", "Field for values", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_FIELDS,
                        "Fields", "Data Fields", Mode.Advanced).withMinMaxValues(2, 20));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, ParallelCoordinatePlotCanvasItemPanel.OPTION_AXES,
                        "Axes", "Axes configuration", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, ParallelCoordinatePlotCanvasItemPanel.OPTION_NULL_VALUES,
                        "Null Values", "Where to place null values", Mode.Advanced).withMinMaxValues(0,1));

        // outputs
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(DatasetSelection.class, CanvasItemPanel.OPTION_SELECTED_IDS,
                        "Selection", "Selected IDs", Mode.Output));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, AbstractD3CanvasItemPanel.OPTION_EXTENTS,
                        "Extents", "Brush extents", Mode.Ignore));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, ParallelCoordinatePlotCanvasItemPanel.OPTION_COLOR_DIMENSION,
                        "Colour dimension", "Dimension for colouring values", Mode.Ignore));

        // option inputs
        OptionDescriptor filterOptionDescriptor = new OptionDescriptor<>(DatasetSelection.class, CanvasItemPanel.OPTION_FILTER_IDS, "Filter", "Filter (IDs to include)", Mode.Input);
        cellDefinition.getOptionDefinitionList().add(filterOptionDescriptor);
        // cellDefinition.getOptionDefinitionList().add(
        //      new OptionDescriptor<>(String.class, ScatterPlotCanvasItemPanel.OPTION_SELECTED_MARKED_IDS, "Selected marked IDs", "Selected marked IDs"));

        // the option bindings
        cellDefinition.getOptionBindingDefinitionList().add(new OptionBindingDefinition(filterOptionDescriptor, CellDefinition.UpdateMode.AUTO));

        return cellDefinition;
    }

    private static CellDefinition createHeatmapCellDefinition() {
        CellDefinition cellDefinition = new SimpleCellDefinition("Heatmap", "Heat map", "icons/visualisation_chart.png", new String[]{"heatmap", "plot", "visualization", "visualisation", "viz"}, false);
        cellDefinition.getBindingDefinitionList().add(new BindingDefinition("input", Dataset.class, BasicObject.class));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, HeatmapCanvasItemPanel.OPTION_ROWS_FIELD,
                        "Rows field", "Field to use for the rows", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, HeatmapCanvasItemPanel.OPTION_COLS_FIELD,
                        "Columns field", "Field to use for the columns", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, HeatmapCanvasItemPanel.OPTION_VALUES_FIELD,
                        "Values field", "Field to use for the heatmap values", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(String.class, HeatmapCanvasItemPanel.OPTION_COLLECTOR,
                        "Collect values using", "How to handle potentially repeated values during data collection", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(Integer.class, HeatmapCanvasItemPanel.OPTION_CELL_SIZE,
                        "Cell size", "Size of each heatmap cell (in pixels)", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(Integer.class, HeatmapCanvasItemPanel.OPTION_LEFT_MARGIN,
                        "Left margin", "Size of space needed for row labels (in pixels)", Mode.Advanced));
        cellDefinition.getOptionDefinitionList().add(
                new OptionDescriptor<>(Integer.class, HeatmapCanvasItemPanel.OPTION_TOP_MARGIN,
                        "Top margin", "Size of space needed for column labels (in pixels)", Mode.Advanced));

        return cellDefinition;
    }

    private static void addFilterOption(CellDefinition cellDefinition) {
        addFilterOption(cellDefinition, CanvasItemPanel.OPTION_FILTER_IDS, "Filter", "Filter (IDs to include)");
    }

    private static void addFilterOption(CellDefinition cellDefinition, String key, String label, String description) {
        // option inputs
        OptionDescriptor filterOptionDescriptor = new OptionDescriptor<>(DatasetSelection.class, key, label, description, Mode.Input);
        cellDefinition.getOptionDefinitionList().add(filterOptionDescriptor);

        // the option bindings
        cellDefinition.getOptionBindingDefinitionList().add(new OptionBindingDefinition(filterOptionDescriptor, CellDefinition.UpdateMode.AUTO));
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

        // TODO - move all these to service cell definitions
        
        registerCellDefinition(new ChemblActivitiesFetcherCellDefinition());
        registerCellDefinition(new CsvUploadCellDefinition());
        registerCellDefinition(new SdfUploadCellDefinition());
        registerCellDefinition(new SmilesStructuresCellDefinition());
        registerCellDefinition(new MolfileUploadCellDefinition());
        registerCellDefinition(new PdbUploadCellDefinition());
        registerCellDefinition(new Mol2UploadCellDefinition());
        registerCellDefinition(new ZipFileUploadCellDefinition());
        registerCellDefinition(new ConvertToMoleculesCellDefinition());
        registerCellDefinition(new DatasetFilterGroovyCellDefinition());
        registerCellDefinition(new DatasetSorterCellDefinition());
        registerCellDefinition(new DataTransformPotionsCellDefinition());
        registerCellDefinition(new TransformValuesCellDefinition());
        registerCellDefinition(new DatasetMoleculesFromFieldCellDefinition());
        registerCellDefinition(new ProcessDatasetUntrustedGroovyScriptCellDefinition());
        registerCellDefinition(new ProcessDatasetUntrustedPythonScriptCellDefinition());
        registerCellDefinition(new SmilesDeduplicatorCellDefinition());
        registerCellDefinition(new CxnReactorCellDefinition());
    }

    private void registerCustomCellDefinitions() {

        // also add creation of cell in NotebookCanvasPage.java

        registerCellDefinition(createTableDisplayCellDefinition());
        registerCellDefinition(createScatterPlotCellDefinition());
        registerCellDefinition(createBoxPlotCellDefinition());
        registerCellDefinition(createParallelCoordinatePlotCellDefinition());
        registerCellDefinition(createHeatmapCellDefinition());
        registerCellDefinition(create3DMolCellDefinition());
        registerCellDefinition(createNglViewerCellDefinition());
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
