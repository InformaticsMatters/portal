package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tim Dudgeon
 */
public class ParallelCoordinatePlotCanvasItemPanel extends AbstractD3CanvasItemPanel {

    private static final String BUILD_PLOT_JS = "buildParallelCoordinatePlot(':id', :data)";

    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;
    private Label statusLabel;
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
        addTitleBar();
        try {
            refreshPlotData();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        addStatus();
    }

    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        ModelObject model = form.getModelObject();
        model.setFields((List<String>)cellInstance.getOptionInstanceMap().get(OPTION_FIELDS).getValue());
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
        statusLabel = createStatusLabel("cellStatus");
        add(statusLabel);
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
    public void onExecute() throws Exception {
        refreshPlotData();
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
        String js = buildPlotJs();
        target.appendJavaScript(js);
    }

    private void refreshPlotData() throws Exception {
        ParallelCoordinatePlotCanvasItemPanel.ModelObject model = form.getModelObject();
        List<String> fields = model.getFields();
            CellInstance cellInstance = findCellInstance();
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variableInstance);
                final AtomicInteger i = new AtomicInteger(0);
                try (Stream<? extends BasicObject> stream = dataset.getStream()) {
                    List<Map<String,Object>> items = stream.sequential().map((o) -> {
                        Map<String,Object> data = new LinkedHashMap<>();
                        data.put("uuid", o.getUUID());
                        data.put("idx", i.incrementAndGet());
                        for (String field: fields) {
                            Object val = o.getValue(field);
                            if (val != null) {
                                data.put(field, o.getValue(field));
                            }
                        }
                        return data;
                    }).collect(Collectors.toList());

                    model.setPlotData(items);
                }
        }
    }

    private String buildPlotJs() throws IOException {
        ModelObject model = form.getModelObject();
        return BUILD_PLOT_JS
                .replace(":id", getMarkupId())
                .replace(":data", model.getPlotDataAsJson());
    }

    @Override
    public Panel getAdvancedOptionsPanel() {
        if (advancedOptionsPanel == null) {
            createAdvancedOptionsPanel();
        }
        return advancedOptionsPanel;
    }

    private void createAdvancedOptionsPanel() {
        advancedOptionsPanel = new ParallelCoordinatePlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
        advancedOptionsPanel.setCallbackHandler(new ParallelCoordinatePlotAdvancedOptionsPanel.CallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() throws Exception {
                CellInstance cellInstance = findCellInstance();
                cellInstance.getOptionInstanceMap().get(OPTION_FIELDS).setValue(advancedOptionsPanel.getFields());
                notebookSession.storeCurrentEditable();

                ModelObject model = form.getModelObject();
                model.setFields(advancedOptionsPanel.getFields());

                onExecute();
            }
        });
        advancedOptionsPanel.setFields(form.getModelObject().getFields());
    }

    class ModelObject implements Serializable {

        private List<Map<String,Object>> plotData;
        /*
        need to create JSON like this:

        [
        {"uuid":"uuid1","idx":1,"field_a":1.1,"field_b":5.1},
        {"uuid":"uuid2","idx":2,"field_a":2.2,"field_b":5.2},
        {"uuid":"uuid3","idx":3,"field_a":3.3,"field_b":5.3}
        ]

        uuid and idx must be present. Other fields as needed.
         */

        private List<String> fields;

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }

        public List<Map<String,Object>> getPlotData() {
            return plotData;
        }

        public void setPlotData(List<Map<String,Object>> plotData) {
            this.plotData = plotData;
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