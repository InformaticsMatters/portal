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

    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        refreshPlotData();
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
        target.appendJavaScript(buildPlotJs());
    }

    private void refreshPlotData() {
        String xFieldName = advancedOptionsPanel.getXAxisFieldName();
        String yFieldName = advancedOptionsPanel.getYAxisFieldName();
        if (xFieldName != null || yFieldName != null) {
            CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellById(getCellId());
            BindingInstance bindingInstance = cellInstance.getBindingMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariable();
            if (variableInstance != null) {
                List<MoleculeObject> dataset = notebookSession.squonkDatasetAsMolecules(variableInstance);
                int[][] plotData = new int[dataset.size()][dataset.size()];
                int index = 0;
                for (MoleculeObject moleculeObject : dataset) {
                    plotData[index][0] = (int) moleculeObject.getValue(xFieldName);
                    plotData[index][1] = (int) moleculeObject.getValue(yFieldName);
                    index++;
                }
                ModelObject model = form.getModelObject();
                model.setPlotData(plotData);
            }
        }
    }

    private String buildPlotJs() {
        ModelObject model = form.getModelObject();
        return BUILD_PLOT_JS.replace(":id", getMarkupId()).replace(":data", model.getPlotDataAsJson());
    }

    @Override
    public Panel getAdvancedOptionsPanel() {
        if (advancedOptionsPanel == null) {
            advancedOptionsPanel = new ScatterPlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
        }
        return advancedOptionsPanel;
    }

    class ModelObject implements Serializable {

        private int[][] plotData = {};

        public int[][] getPlotData() {
            return plotData;
        }

        public void setPlotData(int[][] plotData) {
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
    }
}
