package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.IOUtils;
import org.squonk.dataset.Dataset;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author simetrias
 */
public class HeatmapCanvasItemPanel extends AbstractD3CanvasItemPanel {

    public static final String OPTION_ROWS_FIELD = "rowsField";
    public static final String OPTION_COLS_FIELD = "colsField";
    public static final String OPTION_VALUES_FIELD = "valuesField";
    public static final String OPTION_COLLECTOR = "collector";
    public static final String OPTION_CELL_SIZE = "cellSize";
    public static final String OPTION_LEFT_MARGIN = "leftMargin";
    public static final String OPTION_TOP_MARGIN = "topMargin";
    private static final Logger LOG = Logger.getLogger(HeatmapCanvasItemPanel.class.getName());
    private static final int DEFAULT_CELL_SIZE = 12;
    private static final int DEFAULT_LEFT_MARGIN = 75;
    private static final int DEFAULT_TOP_MARGIN = 75;
    private static final String BUILD_PLOT_JS = "buildHeatmap(\"_id_\", {" +
            "\"cellSize\": _size_, " +
            "\"margin\" : { \"top\": _margin_top_, \"right\": 20, \"bottom\": 20, \"left\": _margin_left_ }, " +
            "\"rowNames\": _row_names_, " +
            "\"colNames\": _col_names_" +
            "}, _data_)";
    private Form<ModelObject> form;
    private Label statusLabel;
    public HeatmapCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);

        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(400); // initial size
            cellInstance.setSizeHeight(400);
        }
        addForm();

        loadModelFromPersistentData();
        addTitleBar();

        try {
            refreshPlotData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //addStatus();
    }

    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        ModelObject model = form.getModelObject();
        model.setRowsField((String) cellInstance.getOptionInstanceMap().get(OPTION_ROWS_FIELD).getValue());
        model.setColsField((String) cellInstance.getOptionInstanceMap().get(OPTION_COLS_FIELD).getValue());
        model.setValuesField((String) cellInstance.getOptionInstanceMap().get(OPTION_VALUES_FIELD).getValue());
        model.setCollector((String) cellInstance.getOptionInstanceMap().get(OPTION_COLLECTOR).getValue());
        model.setCellSize((Integer) cellInstance.getOptionInstanceMap().get(OPTION_CELL_SIZE).getValue());
        model.setLeftMargin((Integer) cellInstance.getOptionInstanceMap().get(OPTION_LEFT_MARGIN).getValue());
        model.setTopMargin((Integer) cellInstance.getOptionInstanceMap().get(OPTION_TOP_MARGIN).getValue());
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/heatmap.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/heatmap.css")));
        makeCanvasItemResizable(container, "fitHeatmap", 200, 150); // minimum size
        try {
            response.render(OnDomReadyHeaderItem.forScript(buildPlotJs()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build JSON data", e);
        }
    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

//    private void addStatus() {
//        statusLabel = createStatusLabel("cellStatus");
//        add(statusLabel);
//    }

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

    private void refreshPlotData() throws Exception {
        HeatmapCanvasItemPanel.ModelObject model = form.getModelObject();
        String rowsFieldName = model.getRowsField();
        String colsFieldName = model.getColsField();
        String valuesFieldName = model.getValuesField();


        if (rowsFieldName != null && colsFieldName != null && valuesFieldName != null) {
            CellInstance cellInstance = findCellInstance();
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variableInstance);
                try (Stream<? extends BasicObject> stream = dataset.getStream()) {
                    Map<Pair, List<Object>> groupedData = stream.map((o) -> {
                        Object row = o.getValue(rowsFieldName);
                        Object col = o.getValue(colsFieldName);
                        Object value = o.getValue(valuesFieldName);
                        //LOG.info("Data: " + row + "," + col + " -> " +value);
                        if (row != null && col != null && value != null) {
                            return new Datum(new Pair(row, col), value);
                        } else {
                            return null;
                        }
                    }).filter((Datum d) -> d != null)
                            .collect(Collectors.groupingBy(Datum::getPair, HashMap::new,
                                    Collectors.mapping(Datum::getValue, toList())));

                    LOG.info("Num data elements: " + groupedData.size());
                    model.setPlotData(groupedData);
                }
            }
        }
    }

    private String buildPlotJs() throws IOException {
        ModelObject model = form.getModelObject();

        List<String> rows = model.getRowNames();
        List<String> cols = model.getColNames();

        String data = model.getPlotDataAsJson(rows, cols);

        String rowJson = JsonHandler.getInstance().objectToJson(rows);
        String colJson = JsonHandler.getInstance().objectToJson(cols);

        String fn = BUILD_PLOT_JS
                .replace("_id_", getMarkupId())
                .replace("_size_", "" + model.getSizeOrDefault())
                .replace("_margin_top_", "" + model.getTopMarginOrDefault())
                .replace("_margin_left_", "" + model.getLeftMarginOrDefault())
                .replace("_row_names_", rowJson)
                .replace("_col_names_", colJson)
                .replace("_data_", data);

        //LOG.info("Javascript: " + fn);
        return fn;

    }

    @Override
    public Panel getAdvancedOptionsPanel() {
        return createAdvancedOptionsPanel();
    }

    private HeatmapAdvancedOptionsPanel createAdvancedOptionsPanel() {
        HeatmapAdvancedOptionsPanel advancedOptionsPanel = new HeatmapAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
        advancedOptionsPanel.setCallbackHandler(new HeatmapAdvancedOptionsPanel.CallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() throws Exception {
                CellInstance cellInstance = findCellInstance();
                cellInstance.getOptionInstanceMap().get(OPTION_ROWS_FIELD).setValue(advancedOptionsPanel.getRowsField());
                cellInstance.getOptionInstanceMap().get(OPTION_COLS_FIELD).setValue(advancedOptionsPanel.getColsField());
                cellInstance.getOptionInstanceMap().get(OPTION_VALUES_FIELD).setValue(advancedOptionsPanel.getValuesField());
                cellInstance.getOptionInstanceMap().get(OPTION_COLLECTOR).setValue(advancedOptionsPanel.getCollector());
                cellInstance.getOptionInstanceMap().get(OPTION_CELL_SIZE).setValue(advancedOptionsPanel.getCellSize());
                cellInstance.getOptionInstanceMap().get(OPTION_LEFT_MARGIN).setValue(advancedOptionsPanel.getLeftMargin());
                cellInstance.getOptionInstanceMap().get(OPTION_TOP_MARGIN).setValue(advancedOptionsPanel.getTopMargin());
                notebookSession.storeCurrentEditable();

                ModelObject model = form.getModelObject();
                model.setRowsField(advancedOptionsPanel.getRowsField());
                model.setColsField(advancedOptionsPanel.getColsField());
                model.setValuesField(advancedOptionsPanel.getValuesField());
                model.setCollector(advancedOptionsPanel.getCollector());
                model.setCellSize(advancedOptionsPanel.getCellSize());
                model.setLeftMargin(advancedOptionsPanel.getLeftMargin());
                model.setTopMargin(advancedOptionsPanel.getTopMargin());

                onExecute();
            }
        });
        advancedOptionsPanel.setRowsField(form.getModelObject().getRowsField());
        advancedOptionsPanel.setColsField(form.getModelObject().getColsField());
        advancedOptionsPanel.setValuesField(form.getModelObject().getValuesField());
        advancedOptionsPanel.setCollector(form.getModelObject().getCollector());
        advancedOptionsPanel.setCellSize(form.getModelObject().getSizeOrDefault());
        advancedOptionsPanel.setLeftMargin(form.getModelObject().getLeftMarginOrDefault());
        advancedOptionsPanel.setTopMargin(form.getModelObject().getTopMarginOrDefault());

        return advancedOptionsPanel;
    }

    protected enum ValueCollector {
        Count, Sum, Average, First, Last
    }

    class ModelObject implements Serializable {

        private Map<Pair, List<Object>> plotData;
        /*
        need to create JSON like this:

        [
        {"row":"uuid1", "col":"uuid2","value":1.234},
        {"row":"uuid1", "col":"uuid3","value":2.345},
        {"row":"uuid1", "col":"uuid4","value":3.456},
        ]
         */

        private String rowsField;
        private String colsField;
        private String valuesField;
        private String collector;
        private Integer cellSize;
        private Integer leftMargin;
        private Integer topMargin;

        public String getRowsField() {
            return rowsField;
        }

        public void setRowsField(String rowsField) {
            this.rowsField = rowsField;
        }

        public String getColsField() {
            return colsField;
        }

        public void setColsField(String colsField) {
            this.colsField = colsField;
        }

        public String getValuesField() {
            return valuesField;
        }

        public void setValuesField(String valuesField) {
            this.valuesField = valuesField;
        }

        public String getCollector() {
            return collector;
        }

        public void setCollector(String collector) {
            this.collector = collector;
        }

        public String getCollectorOrDefault() {
            return collector == null ? ValueCollector.Count.toString() : collector;
        }

        public Integer getCellSize() {
            return cellSize;
        }

        public void setCellSize(Integer cellSize) {
            if (cellSize == null) {
                this.cellSize = null;
            } else if (cellSize < 1) {
                this.cellSize = DEFAULT_CELL_SIZE;
                LOG.warning("Illegal cell size: setting to default");
            } else {
                this.cellSize = cellSize;
            }
        }

        public Integer getSizeOrDefault() {
            return cellSize == null ? DEFAULT_CELL_SIZE : cellSize;
        }

        public Integer getLeftMargin() {
            return leftMargin;
        }

        public void setLeftMargin(Integer leftMargin) {
            if (leftMargin == null) {
                this.leftMargin = null;
            } else if (leftMargin < 1) {
                this.leftMargin = DEFAULT_LEFT_MARGIN;
                LOG.warning("Illegal margin size: setting to default");
            } else {
                this.leftMargin = leftMargin;
            }
        }

        public Integer getLeftMarginOrDefault() {
            return leftMargin == null ? DEFAULT_LEFT_MARGIN : leftMargin;
        }

        public Integer getTopMargin() {
            return topMargin;
        }

        public void setTopMargin(Integer topMargin) {
            if (topMargin == null) {
                this.topMargin = null;
            } else if (topMargin < 1) {
                this.topMargin = DEFAULT_TOP_MARGIN;
                LOG.warning("Illegal margin size: setting to default");
            } else {
                this.topMargin = topMargin;
            }
        }

        public Integer getTopMarginOrDefault() {
            return topMargin == null ? DEFAULT_TOP_MARGIN : topMargin;
        }

        public Map<Pair, List<Object>> getPlotData() {
            return plotData;
        }

        public void setPlotData(Map<Pair, List<Object>> plotData) {
            this.plotData = plotData;
        }

        private List<String> getRowNames() throws IOException {
            if (plotData == null) {
                return Collections.emptyList();
            } else {
                Stream<String> values = plotData.keySet().stream().map((p) -> p.row.toString()).distinct();
                return values.collect(Collectors.toList());
            }
        }

        private List<String> getColNames() throws IOException {
            if (plotData == null) {
                return Collections.emptyList();
            } else {
                Stream<String> values = plotData.keySet().stream().map((p) -> p.col.toString()).distinct();
                return values.collect(Collectors.toList());
            }
        }

        private String getPlotDataAsJson(List<String> rowNames, List<String> colNames) throws IOException {

            if (plotData == null) {
                return "[]";
            } else {

                ValueCollector collector1 = null;
                try {
                    collector1 = ValueCollector.valueOf(getCollectorOrDefault());
                } catch (IllegalArgumentException iae) {
                    LOG.warning("Failed to create collector. Defaulting to 'Count'");
                    collector1 = ValueCollector.Count;
                }
                final ValueCollector collector2 = collector1;

                LOG.info("Generating JSON for " + plotData.size() + " elements");
                Stream<Map> items = plotData.entrySet().stream().sequential()
                        .map((e) -> {
                            Map m = new LinkedHashMap();
                            int r = rowNames.indexOf(e.getKey().row.toString());
                            int c = colNames.indexOf(e.getKey().col.toString());
                            List values = e.getValue();
                            //LOG.info("Handling: " + e.getKey().row + "/" + r + "," + e.getKey().col + "/" + c + " -> " + (values == null ? "null" : values.size()));
                            if (r < 0 || c < 0) {
                                return null;
                            }
                            m.put("row", r);
                            m.put("col", c);

                            switch (collector2) {
                                case Count:
                                    m.put("value", values.size());
                                    break;
                                case First:
                                    if (values.isEmpty()) {
                                        return null;
                                    } else {
                                        m.put("value", safeConvertToDouble(values.get(0)));
                                    }
                                    break;
                                case Last:
                                    if (values.isEmpty()) {
                                        return null;
                                    } else {
                                        m.put("value", safeConvertToDouble(values.get(values.size() - 1)));
                                    }
                                    break;
                                case Sum:
                                    if (values.isEmpty()) {
                                        return null;
                                    } else {
                                        m.put("value", values.parallelStream()
                                                .mapToDouble((v) -> safeConvertToDouble(v))
                                                .sum()
                                        );
                                    }
                                    break;
                                case Average:
                                    if (values.isEmpty()) {
                                        return null;
                                    } else {
                                        m.put("value", values.parallelStream()
                                                .mapToDouble((v) -> safeConvertToDouble(v))
                                                .average()
                                                .getAsDouble()
                                        );
                                    }
                                    break;
                            }
                            return m;
                        })
                        .filter((m) -> m != null && m.get("value") != null);

                InputStream is = JsonHandler.getInstance().marshalStreamToJsonArray(items, false);
                String json = IOUtils.toString(is);
                //LOG.fine("JSON: " + json);
                return json;
            }
        }
    }

    class Pair implements Serializable {
        Object row, col;

        Pair(Object row, Object col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            Pair p = (Pair) o;
            return p.row.equals(row) && p.col.equals(col);
        }

        @Override
        public int hashCode() {
            return row.hashCode() ^ col.hashCode();
        }
    }

    class Datum {
        Pair p;
        Object value;

        Datum(Pair p, Object value) {
            this.p = p;
            this.value = value;
        }

        Pair getPair() {
            return p;
        }

        Object getValue() {
            return value;
        }
    }
}
