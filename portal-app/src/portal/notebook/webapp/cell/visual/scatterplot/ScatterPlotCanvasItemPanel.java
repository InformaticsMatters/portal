package portal.notebook.webapp.cell.visual.scatterplot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.DatasetSelection;
import org.squonk.types.BasicObject;
import org.squonk.types.NumberRange;
import org.squonk.types.io.JsonHandler;
import portal.PortalWebApplication;
import portal.notebook.api.*;
import portal.notebook.webapp.CellBindingDatasetProvider;
import portal.notebook.webapp.cell.visual.AbstractD3CanvasItemPanel;
import portal.notebook.webapp.CellChangeEvent;
import portal.notebook.webapp.results.DatasetResultsHandler;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author simetrias
 */
public class ScatterPlotCanvasItemPanel extends AbstractD3CanvasItemPanel {

    public static final String OPTION_COLOR = "color";
    public static final String OPTION_POINT_SIZE = "pointSize";
    public static final String OPTION_AXIS_LABELS = "axisLabels";
    public static final String OPTION_SELECTED_X_RANGE = "selectionXRange";
    public static final String OPTION_SELECTED_Y_RANGE = "selectionYRange";
    private static final Logger LOG = Logger.getLogger(ScatterPlotCanvasItemPanel.class.getName());
    private static final String BUILD_PLOT_JS = "buildScatterPlot(':id', ':xLabel', ':yLabel', ':colorMode', true, :data)";
    protected static Map<String, Integer> POINT_SIZES = new LinkedHashMap<>();

    static {
        POINT_SIZES.put("Smallest", 1);
        POINT_SIZES.put("Smaller", 2);
        POINT_SIZES.put("Small", 3);
        POINT_SIZES.put("Medium", 5);
        POINT_SIZES.put("Large", 7);
        POINT_SIZES.put("Larger", 9);
        POINT_SIZES.put("Largest", 12);
    }

    private CellBindingDatasetProvider cellDatasetProvider;

    private final ModelObject model = new ModelObject();
    private Form<ModelObject> form;
    private ScatterPlotAdvancedOptionsPanel advancedOptionsPanel;

    public ScatterPlotCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(480);
            cellInstance.setSizeHeight(265);
        }
        addForm();
        loadModelFromPersistentData();
        addTitleBarAndResultsViewer();
        addStatus();
        try {
            refreshPlotData(false);
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error refreshing data", t);
            notifyMessage("Error", "Failed to refresh data" + t.getLocalizedMessage());
        }
    }

    @Override
    protected void createResultsHandlers() {
        LOG.info("Creating results handler");
        this.cellDatasetProvider = new CellBindingDatasetProvider(notebookSession, getCellId(), CellDefinition.VAR_NAME_INPUT, OPTION_FILTER_IDS, OPTION_SELECTED_IDS);
        resultsHandler = new DatasetResultsHandler("filtered", notebookSession, this, cellDatasetProvider);
    }

    private void addStatus() {
        add(createStatusLabel("cellStatus"));
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3-legend.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/scatterplot.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/scatterplot.css")));
        makeCanvasItemResizable(container, "fitScatterPlot", 300, 200);
    }


    @Override
    public void processCellChanged(CellChangeEvent evt, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        super.processCellChanged(evt, ajaxRequestTarget);
        if (doesCellChangeRequireRefresh(evt)) {
            invalidatePlotData();
            onExecute();
        }
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() throws Exception {
        refreshPlotData(true);
        rebuildPlot();
    }

    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        Map<String, OptionInstance> options = cellInstance.getOptionInstanceMap();
        model.setX((String) options.get(OPTION_X_AXIS).getValue());
        model.setY((String) options.get(OPTION_Y_AXIS).getValue());
        model.setColor((String) options.get(OPTION_COLOR).getValue());
        model.setPointSize((String) options.get(OPTION_POINT_SIZE).getValue());
        model.setShowAxisLabels((Boolean) options.get(OPTION_AXIS_LABELS).getValue());
    }

    private void addForm() {
        TextField<Float> brushXMin = new HiddenField<>("brushxmin", new Model<>());
        TextField<Float> brushXMax = new HiddenField<>("brushxmax", new Model<>());
        TextField<Float> brushYMin = new HiddenField<>("brushymin", new Model<>());
        TextField<Float> brushYMax = new HiddenField<>("brushymax", new Model<>());
        TextField<String> selectedIds = new HiddenField<>("selectedIds", new Model<>(""));

        form = new Form<ModelObject>("form") {
            @Override
            @SuppressWarnings("unchecked")
            protected void onBeforeRender() {
                super.onBeforeRender();
                CellInstance cell = findCellInstance();
                Map<String, OptionInstance> options = cell.getOptionInstanceMap();
                NumberRange<Float> xRange = (NumberRange<Float>) options.get(OPTION_SELECTED_X_RANGE).getValue();
                NumberRange<Float> yRange = (NumberRange<Float>) options.get(OPTION_SELECTED_Y_RANGE).getValue();
                brushXMin.getModel().setObject(xRange == null ? null : xRange.getMinValue());
                brushXMax.getModel().setObject(xRange == null ? null : xRange.getMaxValue());
                brushYMin.getModel().setObject(yRange == null ? null : yRange.getMinValue());
                brushYMax.getModel().setObject(yRange == null ? null : yRange.getMaxValue());
                selectedIds.getModel().setObject(""); // never read
            }
        };
        form.setOutputMarkupId(true);
        add(form);
        form.add(brushXMin);
        form.add(brushXMax);
        form.add(brushYMin);
        form.add(brushYMax);
        form.add(selectedIds);

        AjaxButton selectionButton = new AjaxButton("selection") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String xmin = brushXMin.getValue();
                String xmax = brushXMax.getValue();
                String ymin = brushYMin.getValue();
                String ymax = brushYMax.getValue();
                String selectionRaw = selectedIds.getValue();
                String selectionJson = (selectionRaw == null || selectionRaw.isEmpty()) ? null : selectionRaw;

                CellInstance cellInstance = findCellInstance();
                NumberRange<Float> xRange = null, yRange = null;
                try {
                    if ((xmin != null && !xmin.isEmpty()) || (xmax != null && !xmax.isEmpty())) {
                        xRange = new NumberRange.Float(
                                (xmin == null || xmin.isEmpty()) ? null : new Float(xmin),
                                (xmax == null || xmax.isEmpty()) ? null : new Float(xmax));
                    }
                    if ((ymin != null && !ymin.isEmpty()) || (ymax != null && !ymax.isEmpty())) {
                        yRange = new NumberRange.Float(
                                (ymin == null || ymin.isEmpty()) ? null : new Float(ymin),
                                (ymax == null || ymax.isEmpty()) ? null : new Float(ymax));
                    }
                } catch (Exception e) {
                    LOG.warning("Failed to build selection ranges");
                    notifyMessage("Error", "Failed to build selection ranges" + e.getLocalizedMessage());
                    return;
                }

                cellInstance.getOptionInstanceMap().get(OPTION_SELECTED_X_RANGE).setValue(xRange);
                cellInstance.getOptionInstanceMap().get(OPTION_SELECTED_Y_RANGE).setValue(yRange);

                // selection is supplied as JSON array of UUIDs
                DatasetSelection selection = readSelectionJson(selectionJson);
                cellInstance.getOptionInstanceMap().get(OPTION_SELECTED_IDS).setValue(selection);

                saveNotebook();

                notifyOptionValuesChanged(OPTION_SELECTED_IDS, target);
                updateAndNotifyCellStatus(target);
            }
        };
        selectionButton.setDefaultFormProcessing(false);
        form.add(selectionButton);
    }

    private void invalidatePlotData() {
        model.setData(new DataItem[]{});
    }

    private void refreshPlotData(boolean readDataset) throws Exception {
        String xFieldName = model.getX();
        String yFieldName = model.getY();
        String colorFieldName = model.getColor();
        Integer sizeOpt = POINT_SIZES.get(model.getPointSize());
        final int size = (sizeOpt == null ? 5 : sizeOpt);
        if (xFieldName != null || yFieldName != null) {
            CellInstance cellInstance = findCellInstance();
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariableInstance();

            if (variableInstance == null) {
                return;
            }
            Dataset<? extends BasicObject> dataset = readDataset ? cellDatasetProvider.getInputDataset() : null;
            DatasetMetadata meta = dataset == null ? notebookSession.squonkDatasetMetadata(variableInstance) : dataset.getMetadata();
            String colorMode = null;
            // TODO - improve how the color mode is determined as some types could be handled as categorical or continuous.
            // Allow user to specify?
            if (colorFieldName != null) {
                Class colorFieldType = (Class) meta.getValueClassMappings().get(colorFieldName);
                if (colorFieldType != null) {
                    if (Number.class.isAssignableFrom(colorFieldType)) {
                        colorMode = "steelblue-brown";
                    } else {
                        colorMode = "categorical";
                    }
                }
            }
            model.setColorMode(colorMode);

            if (readDataset && dataset != null) {


                Set<UUID> marked = cellInstance.readOptionBindingFilter(OPTION_MARKED_IDS);
                model.setMarked(marked);

//                Dataset<? extends BasicObject> filteredDataset = generateFilteredData(variableInstance, OPTION_FILTER_IDS);
//                if (filteredDataset == null) {
//                    return;
//                }

                AtomicInteger counter = new AtomicInteger(0);
                try (Stream<? extends BasicObject> input = dataset.getStream()) {

                    // convert to DataItems
                    Stream<DataItem> items = input.map((o) -> {
                        int count = counter.incrementAndGet();
                        Float x;
                        if (ScatterPlotAdvancedOptionsPanel.FIELD_RECORD_NUMBER.equals(xFieldName)) {
                            x = (float) count;
                        } else {
                            x = safeConvertToFloat(o.getValue(xFieldName));
                        }
                        Float y;
                        if (ScatterPlotAdvancedOptionsPanel.FIELD_RECORD_NUMBER.equals(yFieldName)) {
                            y = (float) count;
                        } else {
                            y = safeConvertToFloat(o.getValue(yFieldName));
                        }
                        Object color = (colorFieldName == null ? null : o.getValue(colorFieldName));
                        if (x != null && y != null) {
                            DataItem dataItem = new DataItem();
                            dataItem.setUuid(o.getUUID().toString());
                            dataItem.setX(x);
                            dataItem.setY(y);
                            if (color != null) {
                                dataItem.setColor(color);
                            }
                            dataItem.setSize(size);
                            return dataItem;
                        } else {
                            // TODO - should we record how many records are not handled?
                            return null;
                        }
                    }).filter((d) -> d != null);

                    // set result
                    List<DataItem> data = items.collect(Collectors.toList());
                    model.setData(data.toArray(new DataItem[data.size()]));
                }
            } else {
                model.setData(new DataItem[0]);
            }
        }
    }

    private void rebuildPlot() {
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
        target.appendJavaScript(buildPlotJs());

        String marked = buildMarkJs();
        if (marked != null) {
            target.appendJavaScript(marked);
        }
    }

    private String buildMarkJs() {
        Set<UUID> marked = model.getMarked();
        if (marked != null && marked.size() > 0) {
            try {
                LOG.info("Marking " + marked.size() + " records");
                String json = JsonHandler.getInstance().objectToJson(marked);
                String result = "markScatterPlot(':id', :data)"
                        .replace(":id", getMarkupId())
                        .replace(":data", json);

                return result;

            } catch (JsonProcessingException e) {
                LOG.log(Level.WARNING, "Failed to build json for marked IDs", e);
                notifyMessage("Error", "Failed to build json for marked IDs");
            }
        }
        return null;
    }

    private String buildPlotJs() {
        String xLabel = "";
        String yLabel = "";
        if (advancedOptionsPanel != null && Boolean.TRUE.equals(advancedOptionsPanel.getShowAxisLabels())) {
            xLabel = model.getX() != null ? model.getX() : "X Axis";
            yLabel = model.getY() != null ? model.getY() : "Y Axis";
        }

        String result = BUILD_PLOT_JS
                .replace(":id", getMarkupId())
                .replace(":xLabel", xLabel)
                .replace(":yLabel", yLabel)
                .replace(":colorMode", "" + model.getColorMode())
                .replace(":data", model.getDataAsJson());

        return result;
    }

    public String getStatusString() {
        StringBuilder b = new StringBuilder();
        DataItem[] data = model.getData();
        if (model.getX() == null || model.getY() == null) {
            b.append("X and Y fields need to be specified");
        } else if (data == null || data.length == 0) {
            b.append("Loading ...");
        } else {
            b.append(data.length).append(" records, ");
            Set<UUID> marked = model.getMarked();
            if (marked == null || marked.size() == 0) {
                b.append("0 marked, ");
            } else {
                b.append(marked.size()).append(" marked, ");
            }
            try {
                DatasetSelection selection = (DatasetSelection) findCellInstance().getOptionInstanceMap().get(OPTION_SELECTED_IDS).getValue();
                if (selection == null || selection.getUuids().size() == 0) {
                    b.append("0 selected");
                } else {
                    b.append(selection.getUuids().size()).append(" selected");
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to read selection", e);
                b.append("Error reading selection");
            }
        }
        return b.toString();
    }

    @Override
    public Panel getAdvancedOptionsPanel() {
        if (advancedOptionsPanel == null) {
            createAdvancedOptionsPanel();
        }
        return advancedOptionsPanel;
    }

    private void createAdvancedOptionsPanel() {
        advancedOptionsPanel = new ScatterPlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId(), new DefaultCallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() throws Exception {
                CellInstance cellInstance = findCellInstance();
                cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).setValue(advancedOptionsPanel.getX());
                cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).setValue(advancedOptionsPanel.getY());
                cellInstance.getOptionInstanceMap().get(OPTION_COLOR).setValue(advancedOptionsPanel.getColor());
                cellInstance.getOptionInstanceMap().get(OPTION_POINT_SIZE).setValue(advancedOptionsPanel.getPointSize());
                cellInstance.getOptionInstanceMap().get(OPTION_AXIS_LABELS).setValue(advancedOptionsPanel.getShowAxisLabels());
                notebookSession.storeCurrentEditable();

                model.setX(advancedOptionsPanel.getX());
                model.setY(advancedOptionsPanel.getY());
                model.setColor(advancedOptionsPanel.getColor());
                model.setPointSize(advancedOptionsPanel.getPointSize());
                model.setShowAxisLabels(advancedOptionsPanel.getShowAxisLabels());
                onExecute();
            }

        });
        advancedOptionsPanel.setX(model.getX());
        advancedOptionsPanel.setY(model.getY());
        advancedOptionsPanel.setColor(model.getColor());
        advancedOptionsPanel.setPointSize(model.getPointSize());
        advancedOptionsPanel.setShowAxisLabels(model.getShowAxisLabels());
    }

    class ModelObject implements Serializable {

        private String x;
        private String y;
        private DataItem[] data = {};
        private Set<UUID> marked = null;
        private String color;
        private String colorMode;
        private String pointSize;
        private Boolean showAxisLabels = Boolean.FALSE;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }

        public DataItem[] getData() {
            return data;
        }

        public void setData(DataItem[] data) {
            this.data = data;
        }

        public Set<UUID> getMarked() {
            return marked;
        }

        public void setMarked(Set<UUID> marked) {
            this.marked = marked;
        }

        private String getDataAsJson() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(outputStream, data);
                outputStream.flush();
                return outputStream.toString();
            } catch (Throwable t) {
                notifyMessage("Error", "Failed to generate data: " + t.getLocalizedMessage());
                return "[]";
            }
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getColorMode() {
            return colorMode;
        }

        public void setColorMode(String colorMode) {
            this.colorMode = colorMode;
        }

        public String getPointSize() {
            return pointSize;
        }

        public void setPointSize(String pointSize) {
            this.pointSize = pointSize;
        }

        public Boolean getShowAxisLabels() {
            return showAxisLabels;
        }

        public void setShowAxisLabels(Boolean showAxisLabels) {
            this.showAxisLabels = showAxisLabels;
        }

    }

    class DataItem implements Serializable {

        private String uuid;
        private float x;
        private float y;
        private Object color;
        private Integer size;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public Object getColor() {
            return color;
        }

        public void setColor(Object color) {
            this.color = color;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }
    }
}
