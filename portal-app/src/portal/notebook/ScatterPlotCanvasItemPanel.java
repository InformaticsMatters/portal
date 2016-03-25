package portal.notebook;

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
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class ScatterPlotCanvasItemPanel extends CanvasItemPanel {

    private static final String BUILD_PLOT_JS = "buildScatterPlot(':id', :data)";
    private static final String OPTION_X_AXIS = "xAxis";
    private static final String OPTION_Y_AXIS = "yAxis";
    private Form<ModelObject> form;
    private ScatterPlotAdvancedOptionsPanel advancedOptionsPanel;
    @Inject
    private NotebookSession notebookSession;

    public ScatterPlotCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(500);
        }
        addForm();
        loadModelFromPersistentData();
        addTitleBar();
        refreshPlotData();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/scatterplot.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/scatterplot.css")));
        response.render(OnDomReadyHeaderItem.forScript(buildPlotJs()));
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        if (isChangedCellBoundCell(changedCellId)) {
            System.out.println("bound cell changed: executing");
            invalidatePlotData();
            onExecute();
        }
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
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
        model.setX(cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).getValue(String.class));
        model.setY(cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).getValue(String.class));
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
        model.setPlotData(new float[][]{});
    }

    private void refreshPlotData() {
        ModelObject model = form.getModelObject();
        String xFieldName = model.getX();
        String yFieldName = model.getY();
        if (xFieldName != null || yFieldName != null) {
            CellInstance cellInstance = findCellInstance();
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                List<MoleculeObject> dataset = notebookSession.squonkDatasetAsMolecules(variableInstance);
                float[][] plotData = new float[dataset.size()][dataset.size()];
                int index = 0;
                for (MoleculeObject moleculeObject : dataset) {
                    Float x = safeConvertToFloat(moleculeObject.getValue(xFieldName));
                    Float y = safeConvertToFloat(moleculeObject.getValue(yFieldName));
                    if (x != null && y != null) {
                        plotData[index][0] = x;
                        plotData[index][1] = y;
                        index++;
                        // TODO - should we record how many records are not handled?
                    }
                }
                model.setPlotData(plotData);
            }
        }
    }

    private void rebuildPlot() {
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
        target.appendJavaScript(buildPlotJs());
    }

    private Float safeConvertToFloat(Object o) {
        if (o == null) {
            return null;
        } if (o instanceof  Float) {
            return (Float)o;
        } else if (o instanceof Number) {
            return ((Number)o).floatValue();
        } else {
            try {
                return new Float(o.toString());
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    private String buildPlotJs() {
        ModelObject model = form.getModelObject();
        return BUILD_PLOT_JS.replace(":id", getMarkupId()).replace(":data", model.getPlotDataAsJson());
    }

    private void createAdvancedOptionsPanel() {
        advancedOptionsPanel = new ScatterPlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
        advancedOptionsPanel.setCallbackHandler(new ScatterPlotAdvancedOptionsPanel.CallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() {
                CellInstance cellInstance = findCellInstance();
                cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).setValue(advancedOptionsPanel.getX());
                cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).setValue(advancedOptionsPanel.getY());
                notebookSession.storeCurrentNotebook();

                ModelObject model = form.getModelObject();
                model.setX(advancedOptionsPanel.getX());
                model.setY(advancedOptionsPanel.getY());
                onExecute();
            }
        });
        advancedOptionsPanel.setX(form.getModelObject().getX());
        advancedOptionsPanel.setY(form.getModelObject().getY());
    }

    class ModelObject implements Serializable {

        private float[][] plotData = {};
        private String x;
        private String y;

        public float[][] getPlotData() {
            return plotData;
        }

        public void setPlotData(float[][] plotData) {
            this.plotData = plotData;
        }

        private String getPlotDataAsJson() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                objectMapper.writeValue(outputStream, plotData);
                outputStream.flush();
                return outputStream.toString();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

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
    }
}
