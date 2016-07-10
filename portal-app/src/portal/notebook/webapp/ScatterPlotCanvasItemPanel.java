package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author simetrias
 */
public class ScatterPlotCanvasItemPanel extends AbstractD3CanvasItemPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScatterPlotCanvasItemPanel.class);
    private static final String BUILD_PLOT_JS = "buildScatterPlot(':id', :width, :height, :data, ':xLabel', ':yLabel', ':colorMode')";

    public static final String OPTION_COLOR = "color";
    public static final String OPTION_POINT_SIZE = "pointSize";
    public static final String OPTION_AXIS_LABELS = "axisLabels";
    private Form<ModelObject> form;
    private ScatterPlotAdvancedOptionsPanel advancedOptionsPanel;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;

    protected static Map<String,Integer> POINT_SIZES = new LinkedHashMap<>();

    static {
        POINT_SIZES.put("Smallest", 1);
        POINT_SIZES.put("Smaller", 2);
        POINT_SIZES.put("Small", 3);
        POINT_SIZES.put("Medium", 5);
        POINT_SIZES.put("Large", 7);
        POINT_SIZES.put("Larger", 9);
        POINT_SIZES.put("Largest", 12);
    }


    public ScatterPlotCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(480);
            cellInstance.setSizeHeight(265);
        }
        adjustSVGSize(cellInstance);
        addForm();
        loadModelFromPersistentData();
        addTitleBar();
        try {
            refreshPlotData();
        } catch (Throwable t) {
            LOGGER.warn("Error refreshing data", t);
            // TODO
        }
    }

    private void adjustSVGSize(CellInstance cellInstance) {
        // these are the adjustments needed to get the SVG to the right size.
        // I don't understand the adjustments - they just work!
        svgWidth = cellInstance.getSizeWidth() - 40;
        svgHeight = cellInstance.getSizeHeight() - 45;
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3-legend.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/scatterplot.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/scatterplot.css")));
        response.render(OnDomReadyHeaderItem.forScript(buildPlotJs()));
        response.render(OnDomReadyHeaderItem.forScript("fitScatterPlot('" + getMarkupId() + "')"));
        makeCanvasItemResizable(container, "fitScatterPlot", 300, 200);
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        super.processCellChanged(changedCellId, ajaxRequestTarget);
        if (isChangedCellBoundCell(changedCellId)) {
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
        refreshPlotData();
        rebuildPlot();
    }

    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        ModelObject model = form.getModelObject();
        model.setX((String)cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).getValue());
        model.setY((String)cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).getValue());
        model.setColor((String)cellInstance.getOptionInstanceMap().get(OPTION_COLOR).getValue());
        model.setPointSize((String)cellInstance.getOptionInstanceMap().get(OPTION_POINT_SIZE).getValue());
        model.setShowAxisLabels((Boolean)cellInstance.getOptionInstanceMap().get(OPTION_AXIS_LABELS).getValue());
    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

    private boolean isChangedCellBoundCell(Long changedCellId) {
        CellInstance cellInstance = findCellInstance();
        BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        VariableInstance variableInstance = bindingInstance.getVariableInstance();
        return variableInstance != null && changedCellId.equals(variableInstance.getCellId());
    }

    private void invalidatePlotData() {
        ModelObject model = form.getModelObject();
        model.setData(new DataItem[]{});
    }

    private void refreshPlotData() throws Exception {
        ModelObject model = form.getModelObject();
        String xFieldName = model.getX();
        String yFieldName = model.getY();
        String colorFieldName = model.getColor();
        Integer sizeOpt = POINT_SIZES.get(model.getPointSize());
        final int size = (sizeOpt == null ? 5 : sizeOpt);
        if (xFieldName != null || yFieldName != null) {
            CellInstance cellInstance = findCellInstance();
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variableInstance);
                DatasetMetadata meta = dataset.getMetadata();
                String colorMode = null;
                // TODO - improve how the color mode is determined as some types could be handled as categorical or continuous.
                // Allow user to specify?
                if (colorFieldName != null) {
                    Class colorFieldType = (Class)meta.getValueClassMappings().get(colorFieldName);
                    if (colorFieldType != null) {
                        if (Number.class.isAssignableFrom(colorFieldType)) {
                            colorMode = "steelblue-brown";
                        } else {
                            colorMode = "categorical";
                        }
                    }
                }
                model.setColorMode(colorMode);

                try (Stream<? extends BasicObject> stream = dataset.getStream()) {
                    List<DataItem> data = stream.map((o) -> {
                        Float x = safeConvertToFloat(o.getValue(xFieldName));
                        Float y = safeConvertToFloat(o.getValue(yFieldName));
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
                    }).filter((d) -> d != null)
                            .collect(Collectors.toList());

                    model.setData(data.toArray(new DataItem[data.size()]));
                }
            }
        }
    }

    private void rebuildPlot() {
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
        target.appendJavaScript(buildPlotJs());
    }

    private String buildPlotJs() {
        ModelObject model = form.getModelObject();

        String xLabel = "";
        String yLabel = "";
        if (advancedOptionsPanel != null && Boolean.TRUE.equals(advancedOptionsPanel.getShowAxisLabels())) {
            xLabel = model.getX() != null ? model.getX() : "X Axis";
            yLabel = model.getY() != null ? model.getY() : "Y Axis";
        }

        String result = BUILD_PLOT_JS
                .replace(":id", getMarkupId())
                .replace(":width", ""+svgWidth)
                .replace(":height", ""+svgHeight)
                .replace(":xLabel", xLabel)
                .replace(":yLabel", yLabel)
                .replace(":colorMode", ""+model.getColorMode())
                .replace(":data", model.getDataAsJson());

        return result;
    }

    @Override
    public Panel getAdvancedOptionsPanel() {
        if (advancedOptionsPanel == null) {
            createAdvancedOptionsPanel();
        }
        return advancedOptionsPanel;
    }

    private void createAdvancedOptionsPanel() {
        advancedOptionsPanel = new ScatterPlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
        advancedOptionsPanel.setCallbackHandler(new ScatterPlotAdvancedOptionsPanel.CallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() throws Exception {
                CellInstance cellInstance = findCellInstance();
                cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).setValue(advancedOptionsPanel.getX());
                cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).setValue(advancedOptionsPanel.getY());
                cellInstance.getOptionInstanceMap().get(OPTION_COLOR).setValue(advancedOptionsPanel.getColor());
                cellInstance.getOptionInstanceMap().get(OPTION_POINT_SIZE).setValue(advancedOptionsPanel.getPointSize());
                cellInstance.getOptionInstanceMap().get(OPTION_AXIS_LABELS).setValue(advancedOptionsPanel.getShowAxisLabels());
                notebookSession.storeCurrentEditable();

                ModelObject model = form.getModelObject();
                model.setX(advancedOptionsPanel.getX());
                model.setY(advancedOptionsPanel.getY());
                model.setColor(advancedOptionsPanel.getColor());
                model.setPointSize(advancedOptionsPanel.getPointSize());
                model.setShowAxisLabels(advancedOptionsPanel.getShowAxisLabels());
                onExecute();
            }
        });
        advancedOptionsPanel.setX(form.getModelObject().getX());
        advancedOptionsPanel.setY(form.getModelObject().getY());
        advancedOptionsPanel.setColor(form.getModelObject().getColor());
        advancedOptionsPanel.setPointSize(form.getModelObject().getPointSize());
        advancedOptionsPanel.setShowAxisLabels(form.getModelObject().getShowAxisLabels());
    }

    class ModelObject implements Serializable {

        private String x;
        private String y;
        private DataItem[] data = {};
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

        private String getDataAsJson() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(outputStream, data);
                outputStream.flush();
                return outputStream.toString();
            } catch (Throwable t) {
                throw new RuntimeException(t);
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
