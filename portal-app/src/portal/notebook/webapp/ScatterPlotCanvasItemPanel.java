package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
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
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

import static chemaxon.marvin.io.formats.cdx.CDXConstants.page;

/**
 * @author simetrias
 */
public class ScatterPlotCanvasItemPanel extends CanvasItemPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScatterPlotCanvasItemPanel.class);
    private static final String BUILD_PLOT_JS = "buildScatterPlot(':id', :data, ':xLabel', ':yLabel')";
    private static final String OPTION_X_AXIS = "xAxis";
    private static final String OPTION_Y_AXIS = "yAxis";
    private static final String OPTION_COLOR = "color";
    private static final String OPTION_AXIS_LABELS = "axisLabels";
    private Form<ModelObject> form;
    private ScatterPlotAdvancedOptionsPanel advancedOptionsPanel;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;

    public ScatterPlotCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(480);
            cellInstance.setSizeHeight(260);
        }
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

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/scatterplot.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/scatterplot.css")));
        response.render(OnDomReadyHeaderItem.forScript(buildPlotJs()));
        makeScatterPlotResizable(container, 480, 270);
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) throws Exception {
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

    @Override
    public Panel getAdvancedOptionsPanel() {
        if (advancedOptionsPanel == null) {
            createAdvancedOptionsPanel();
        }
        return advancedOptionsPanel;
    }

    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        ModelObject model = form.getModelObject();
        model.setX((String)cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).getValue());
        model.setY((String)cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).getValue());
        model.setColor((String)cellInstance.getOptionInstanceMap().get(OPTION_COLOR).getValue());
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
        if (xFieldName != null || yFieldName != null) {
            CellInstance cellInstance = findCellInstance();
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                List<MoleculeObject> dataset = notebookSession.squonkDatasetAsMolecules(variableInstance);
                DataItem[] data = new DataItem[dataset.size()];
                int index = 0;
                for (MoleculeObject moleculeObject : dataset) {
                    Float x = safeConvertToFloat(moleculeObject.getValue(xFieldName));
                    Float y = safeConvertToFloat(moleculeObject.getValue(yFieldName));
                    Integer color = (Integer) moleculeObject.getValue(colorFieldName);
                    if (x != null && y != null) {
                        DataItem dataItem = new DataItem();
                        dataItem.setX(x);
                        dataItem.setY(y);
                        dataItem.setColor(color);
                        data[index] = dataItem;
                        index++;
                        // TODO - should we record how many records are not handled?
                    }
                }
                model.setData(data);
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
        String result = BUILD_PLOT_JS.replace(":id", getMarkupId()).replace(":data", model.getDataAsJson());
        String xLabel = "";
        String yLabel = "";
        if (advancedOptionsPanel != null && Boolean.TRUE.equals(advancedOptionsPanel.getShowAxisLabels())) {
            xLabel = model.getX() != null ? model.getX() : "";
            yLabel = model.getY() != null ? model.getY() : "";
        }
        result = result.replace(":xLabel", xLabel).replace(":yLabel", yLabel);
        return result;
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
                cellInstance.getOptionInstanceMap().get(OPTION_AXIS_LABELS).setValue(advancedOptionsPanel.getShowAxisLabels());
                notebookSession.storeCurrentNotebook();

                ModelObject model = form.getModelObject();
                model.setX(advancedOptionsPanel.getX());
                model.setY(advancedOptionsPanel.getY());
                model.setColor(advancedOptionsPanel.getColor());
                model.setShowAxisLabels(advancedOptionsPanel.getShowAxisLabels());
                onExecute();
            }
        });
        advancedOptionsPanel.setX(form.getModelObject().getX());
        advancedOptionsPanel.setY(form.getModelObject().getY());
        advancedOptionsPanel.setColor(form.getModelObject().getColor());
        advancedOptionsPanel.setShowAxisLabels(form.getModelObject().getShowAxisLabels());
    }

    private Float safeConvertToFloat(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else {
            try {
                return new Float(o.toString());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    class ModelObject implements Serializable {

        private String x;
        private String y;
        private DataItem[] data = {};
        private String color;
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

        public Boolean getShowAxisLabels() {
            return showAxisLabels;
        }

        public void setShowAxisLabels(Boolean showAxisLabels) {
            this.showAxisLabels = showAxisLabels;
        }
    }

    class DataItem implements Serializable {

        private float x;
        private float y;
        private Integer color;

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

        public Integer getColor() {
            return color;
        }

        public void setColor(Integer color) {
            this.color = color;
        }
    }
}
