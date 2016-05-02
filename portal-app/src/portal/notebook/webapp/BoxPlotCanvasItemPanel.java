package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import portal.PortalWebApplication;
import portal.notebook.api.CellInstance;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author simetrias
 */
public class BoxPlotCanvasItemPanel extends CanvasItemPanel {

    private static final String BUILD_PLOT_JS = "buildBoxPlot(':id', :data)";
    private Form<ModelObject> form;

    public BoxPlotCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(780);
        }
        addForm();
        addTitleBar();
        refreshPlotData();
        addStatus();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/boxplot.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/boxplot.css")));
        response.render(OnDomReadyHeaderItem.forScript(buildPlotJs()));
    }

    private void addStatus() {
        add(new Label("cellStatus", "Status message here"));
    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {

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
        ModelObject model = form.getModelObject();
        // modify the model
    }

    private String buildPlotJs() {
        ModelObject model = form.getModelObject();
        return BUILD_PLOT_JS.replace(":id", getMarkupId()).replace(":data", model.getPlotDataAsJson());
    }

    /**
     * using an array of arrays: data[n][2]
     * where n = number of columns
     * data[i][0] = name of the ith column
     * data[i][1] = array of values of ith column
     */
    class ModelObject implements Serializable {

        private final String[] columnName = {"Q1", "Q2", "Q3", "Q4"};
        private final int[][] columnData = {
                {20000, 9879, 5070, 7343, 9136, 7943, 10546, 9385, 8669, 4000},
                {15000, 9323, 9395, 8675, 5354, 6725, 10899, 9365, 8238, 7446},
                {8000, 3294, 17633, 12121, 4319, 18712, 17270, 13676, 6587, 16754},
                {20000, 5629, 5752, 7557, 5125, 5116, 5828, 6014, 5995, 8905}
        };
        private ArrayList<Object> plotData;

        public ModelObject() {
            plotData = new ArrayList<>();
            int columnCount = columnName.length;
            for (int i = 0; i < columnCount; i++) {
                Object[] columnSpec = new Object[2];
                columnSpec[0] = columnName[i];
                columnSpec[1] = columnData[i];
                plotData.add(columnSpec);
            }
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
