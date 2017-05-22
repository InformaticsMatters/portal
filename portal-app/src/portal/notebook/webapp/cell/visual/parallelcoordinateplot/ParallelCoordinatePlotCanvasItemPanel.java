package portal.notebook.webapp.cell.visual.parallelcoordinateplot;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetSelection;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;
import portal.PortalWebApplication;
import portal.notebook.api.*;
import portal.notebook.webapp.CellChangeEvent;
import portal.notebook.webapp.VariableBindingCellDatasetProvider;
import portal.notebook.webapp.cell.visual.AbstractD3CanvasItemPanel;
import portal.notebook.webapp.results.DatasetResultsHandler;
import portal.notebook.webapp.results.ResultsPanelProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tim Dudgeon
 */
public class ParallelCoordinatePlotCanvasItemPanel extends AbstractD3CanvasItemPanel {

    protected enum NullValues {Ignore, Bottom, Top}

    public static final String OPTION_NULL_VALUES = "nullValues";
    public static final String OPTION_COLOR_DIMENSION = "colorDimension";
    private static final Logger LOG = Logger.getLogger(ParallelCoordinatePlotCanvasItemPanel.class.getName());
    private static final String BUILD_PLOT_JS = "buildParallelCoordinatePlot(\":id\", {:nullValues}, :data)";
    private VariableBindingCellDatasetProvider cellDatasetProvider;
    private final ModelObject model = new ModelObject();
    private Form<ModelObject> form;
    private ParallelCoordinatePlotAdvancedOptionsPanel advancedOptionsPanel;

    public ParallelCoordinatePlotCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(500);
            cellInstance.setSizeHeight(320);
        }
        addForm();
        loadModelFromPersistentData();
        addTitleBarAndResultsViewer();
        addStatus();
        try {
            refreshPlotData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void createResultsHandlers() {
        LOG.info("Creating results handler");
        this.cellDatasetProvider = new VariableBindingCellDatasetProvider(notebookSession, getCellId(), CellDefinition.VAR_NAME_INPUT, OPTION_FILTER_IDS, OPTION_SELECTED_IDS);

        resultsHandler = new DatasetResultsHandler("filtered", notebookSession, this, cellDatasetProvider);
    }

    @SuppressWarnings("unchecked")
    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        Map<String, OptionInstance> options = cellInstance.getOptionInstanceMap();
        model.setFields((List<String>) options.get(OPTION_FIELDS).getValue());
        OptionInstance nullValuesOpt = options.get(OPTION_NULL_VALUES);
        model.setNullValues(nullValuesOpt.getValue() == null ? NullValues.Ignore : NullValues.valueOf((String) nullValuesOpt.getValue()));
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.parcoords.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/parallelcoordinateplot.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/parallelcoordinateplot.css")));
        makeCanvasItemResizable(container, "fitParallelCoordinatePlot", 350, 250);
        try {
            response.render(OnDomReadyHeaderItem.forScript(buildPlotJs()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build JSON data", e);
        }
    }

    private void addStatus() {
        add(createStatusLabel("cellStatus"));
    }

    private void addForm() {

        TextField<String> selection = new HiddenField<>("selection", new Model<>(""));
        TextField<String> axes = new HiddenField<>("axes", new Model<>(""));
        TextField<String> extents = new HiddenField<>("extents", new Model<>(""));
        TextField<String> colorDimension = new HiddenField<>("colorDimension", new Model<>(""));

        form = new Form<ModelObject>("form") {
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                CellInstance cell = findCellInstance();
                Map<String, OptionInstance> options = cell.getOptionInstanceMap();
                selection.getModel().setObject(""); // never read
                OptionInstance axesOpt = options.get(OPTION_AXES);
                axes.getModel().setObject(axesOpt == null ? null : (String)axesOpt.getValue());
                OptionInstance extentsOpt = options.get(OPTION_EXTENTS);
                extents.getModel().setObject(extentsOpt == null ? null : (String)extentsOpt.getValue());
                OptionInstance colorDimOpt = options.get(OPTION_COLOR_DIMENSION);
                colorDimension.getModel().setObject(colorDimOpt == null ? null : (String)colorDimOpt.getValue());
            }
        };

        add(form);

        form.add(selection);
        form.add(extents);
        form.add(axes);
        form.add(colorDimension);

        AjaxButton selectionButton = new AjaxButton("updateSelection") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String selectedIdsJson = selection.getValue();
                String extentsValue = extents.getValue();
//                if (selectedIdsJson != null && selectedIdsJson.isEmpty()) {
//                    selectedIdsJson = null;
//                }
                if (extentsValue != null && extentsValue.isEmpty()) {
                    extentsValue = null;
                }

                CellInstance cell = findCellInstance();
                cell.getOptionInstanceMap().get(OPTION_EXTENTS).setValue(extentsValue);

                // selection is supplied as JSON array of UUIDs
                DatasetSelection selection = readSelectionJson(selectedIdsJson);
                cell.getOptionInstanceMap().get(OPTION_SELECTED_IDS).setValue(selection);

                //cell.getOptionBindingInstance(OPTION_SELECTED_IDS).getOptionInstance();

                saveNotebook();

                notifyOptionValuesChanged(OPTION_SELECTED_IDS, target);
                updateAndNotifyCellStatus(target);
            }
        };
        selectionButton.setDefaultFormProcessing(false);
        form.add(selectionButton);

        AjaxButton axesButton = new AjaxButton("updateAxes") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String axesValue = axes.getValue();
                //System.out.println("Axes: " + axesValue);
                if (axesValue.isEmpty()) {
                    axesValue = null;
                }
                String colorDimensionValue = colorDimension.getValue();
                if (colorDimensionValue.isEmpty()) {
                    colorDimensionValue = null;
                }

                findCellInstance().getOptionInstanceMap().get(OPTION_AXES).setValue(axesValue);
                findCellInstance().getOptionInstanceMap().get(OPTION_COLOR_DIMENSION).setValue(colorDimensionValue);

                saveNotebook();
            }
        };
        axesButton.setDefaultFormProcessing(false);
        form.add(axesButton);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() throws Exception {
        refreshPlotData();
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
        String js = buildPlotJs();
        target.appendJavaScript(js);
    }

    @Override
    public void processCellChanged(CellChangeEvent evt, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        super.processCellChanged(evt, ajaxRequestTarget);
        if (doesCellChangeRequireRefresh(evt)) {
            invalidatePlotData();
            onExecute();
        }
    }

    private void invalidatePlotData() {
        model.setPlotData(Collections.emptyList());
    }


    private void refreshPlotData() throws Exception {

        List<String> fields = model.getFields();
        if (fields == null) {
            // no data bound yet
            return;
        }
        CellInstance cellInstance = findCellInstance();
        BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        VariableInstance variableInstance = bindingInstance.getVariableInstance();

        if (variableInstance == null) {
            return;
        }


        Dataset<? extends BasicObject> dataset = cellDatasetProvider.getInputDataset();
        if (dataset == null) {
            return;
        }

        try (Stream<? extends BasicObject> input = dataset.getStream()) {
            // convert to data that the plot needs
            final AtomicInteger idx = new AtomicInteger(0);
            List<Map<String, Object>> items = input.sequential().map((o) -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("uuid", o.getUUID());
                data.put("idx", idx.incrementAndGet());
                for (String field : fields) {
                    Object val = o.getValue(field);
                    if (val != null) {
                        data.put(field, o.getValue(field));
                    }
                }
                return data;
            }).collect(Collectors.toList());

            // set data
            model.setPlotData(items);
            model.setSize(idx.get());
        }
    }

    private String buildPlotJs() throws IOException {

        String nullValuesStr = null;
        switch (model.getNullValues()) {
            case Bottom:
                nullValuesStr = "nullValueSeparator: \"bottom\"";
                break;
            case Top:
                nullValuesStr = "nullValueSeparator: \"top\"";
                break;
            default:
                nullValuesStr = "";
        }

        return BUILD_PLOT_JS
                .replace(":id", getMarkupId())
                .replace(":nullValues", nullValuesStr)
                .replace(":data", model.getPlotDataAsJson());
    }

    public String getStatusString() {
        StringBuilder b = new StringBuilder();
        Integer numRecords = model.getSize();
        if (numRecords == null || numRecords == 0) {
            b.append("No data");
        } else {
            b.append(numRecords).append(" records, ");
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
        advancedOptionsPanel = new ParallelCoordinatePlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId(), new DefaultCallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() throws Exception {
                CellInstance cellInstance = findCellInstance();
                Map<String, OptionInstance> opts = cellInstance.getOptionInstanceMap();
                opts.get(OPTION_FIELDS).setValue(advancedOptionsPanel.getFields());
                NullValues nullValues = advancedOptionsPanel.getNullValues();
                opts.get(OPTION_NULL_VALUES).setValue(nullValues.toString());

                notebookSession.storeCurrentEditable();

                model.setFields(advancedOptionsPanel.getFields());
                model.setNullValues(advancedOptionsPanel.getNullValues());

                onExecute();
            }

        });
        advancedOptionsPanel.setFields(model.getFields());
        advancedOptionsPanel.setNullValues(model.getNullValues());
    }

    @Override
    public List<ResultsPanelProvider> collectExpandedPanels(List<ResultsPanelProvider> panels) {
//        panels.add(new ResultsPanelProvider("viz", "Visual") {
//            @Override
//            public MarkupContainer createPanel(int tabIndex, Class dataType) {
//                return new ParallelCoordinatePlotPanel(String.valueOf(tabIndex));
//            }
//        });
        return panels;
    }

    class ModelObject implements Serializable {

        private List<Map<String, Object>> plotData;
        /*
        need to create JSON like this:

        [
        {"uuid":"uuid1","idx":1,"field_a":1.1,"field_b":5.1},
        {"uuid":"uuid2","idx":2,"field_a":2.2,"field_b":5.2},
        {"uuid":"uuid3","idx":3,"field_a":3.3,"field_b":5.3}
        ]

        uuid and idx must be present. Other fields as needed.
         */


        private Integer size = null;

        private List<String> fields;

        private NullValues nullValues = NullValues.Ignore;

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }

        public List<Map<String, Object>> getPlotData() {
            return plotData;
        }

        public void setPlotData(List<Map<String, Object>> plotData) {
            this.plotData = plotData;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public NullValues getNullValues() {
            return nullValues;
        }

        public void setNullValues(NullValues nullValues) {
            this.nullValues = nullValues;
        }

        private String getPlotDataAsJson() throws IOException {

            if (plotData == null) {
                return "[]";
            } else {
                InputStream is = JsonHandler.getInstance().marshalStreamToJsonArray(plotData.stream(), false);
                String json = IOUtils.toString(is);
                return json;
            }
        }

    }

}
