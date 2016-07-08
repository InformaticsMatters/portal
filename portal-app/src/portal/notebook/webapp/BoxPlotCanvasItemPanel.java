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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author simetrias
 */
public class BoxPlotCanvasItemPanel extends AbstractD3CanvasItemPanel {

    private static final String BUILD_PLOT_JS = "buildBoxPlot(':id', :width, :height, ':groupsFieldName', ':valuesFieldName', :data)";

    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;
    private Label statusLabel;
    private BoxPlotAdvancedOptionsPanel advancedOptionsPanel;
    private int svgWidth, svgHeight;

    public BoxPlotCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(480);
            cellInstance.setSizeHeight(320);
        }
        adjustSVGSize(cellInstance);
        addForm();
        loadModelFromPersistentData();
        addTitleBar();
        try {
            refreshPlotData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        addStatus();
    }

    private void adjustSVGSize(CellInstance cellInstance) {
        // these are the adjustments needed to get the SVG to the right size.
        // I don't understand the adjustments - they just work!
        svgWidth = cellInstance.getSizeWidth() + 20;
        svgHeight = cellInstance.getSizeHeight() - 75;
    }


    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        ModelObject model = form.getModelObject();
        model.setGroupsFieldName((String)cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).getValue());
        model.setValuesFieldName((String)cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).getValue());
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/boxplot.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/boxplot.css")));
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
        BoxPlotCanvasItemPanel.ModelObject model = form.getModelObject();
        String groupsFieldName = model.getGroupsFieldName();
        String valuesFieldName = model.getValuesFieldName();

        if (groupsFieldName != null || valuesFieldName != null) {
            CellInstance cellInstance = findCellInstance();
            BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variableInstance);
                Map<Comparable, List<Float>> groupedData = dataset.getStream()
                        .map((o) -> {
                            Object group = o.getValue(groupsFieldName);
                            Float value = safeConvertToFloat(o.getValue(valuesFieldName));
                            if (group != null && value != null && group instanceof Comparable) {
                                return new Datum((Comparable) group, value);
                            } else {
                                return null;
                            }
                        })
                        .filter((Datum d) -> d != null)
                        .collect(Collectors.groupingBy(Datum::getGroup, TreeMap::new,
                                Collectors.mapping(Datum::getValue, toList())));

                model.setGroupsFieldName(groupsFieldName);
                model.setValuesFieldName(valuesFieldName);
                model.setPlotData(groupedData);
            }
        }
    }


    private String buildPlotJs() throws IOException {
        ModelObject model = form.getModelObject();
        return BUILD_PLOT_JS
                .replace(":id", getMarkupId())
                .replace(":width", ""+svgWidth)
                .replace(":height", ""+svgHeight)
                .replace(":groupsFieldName", model.getGroupsFieldName() == null ? "" : model.getGroupsFieldName())
                .replace(":valuesFieldName", model.getValuesFieldName() == null ? "" : model.getValuesFieldName())
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
        advancedOptionsPanel = new BoxPlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
        advancedOptionsPanel.setCallbackHandler(new BoxPlotAdvancedOptionsPanel.CallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() throws Exception {
                CellInstance cellInstance = findCellInstance();
                cellInstance.getOptionInstanceMap().get(OPTION_X_AXIS).setValue(advancedOptionsPanel.getX());
                cellInstance.getOptionInstanceMap().get(OPTION_Y_AXIS).setValue(advancedOptionsPanel.getY());
                notebookSession.storeCurrentEditable();

                ModelObject model = form.getModelObject();
                model.setGroupsFieldName(advancedOptionsPanel.getX());
                model.setValuesFieldName(advancedOptionsPanel.getY());

                onExecute();
            }
        });
        advancedOptionsPanel.setX(form.getModelObject().getGroupsFieldName());
        advancedOptionsPanel.setY(form.getModelObject().getValuesFieldName());
    }

    class ModelObject implements Serializable {

        private Map<Comparable, List<Float>> plotData;
        /*
        need to create JSON like this:

        [
        ["Q1",[20000,9879,5070,7343,9136,7943,10546,9385,8669,4000]],
        ["Q2",[15000,9323,9395,8675,5354,6725,10899,9365,8238,7446]],
        ["Q3",[8000,3294,17633,12121,4319,18712,17270,13676,6587,16754]],
        ["Q4",[20000,5629,5752,7557,5125,5116,5828,6014,5995,8905]]
        ]
         */

        private String groupsFieldName;
        private String valuesFieldName;

        public String getGroupsFieldName() {
            return groupsFieldName;
        }

        public void setGroupsFieldName(String groupsFieldName) {
            this.groupsFieldName = groupsFieldName;
        }

        public String getValuesFieldName() {
            return valuesFieldName;
        }

        public void setValuesFieldName(String valuesFieldName) {
            this.valuesFieldName = valuesFieldName;
        }

        public Map<Comparable, List<Float>> getPlotData() {
            return plotData;
        }

        public void setPlotData(Map<Comparable, List<Float>> plotData) {
            this.plotData = plotData;
        }

        private String getPlotDataAsJson() throws IOException {

            if (plotData == null) {
                return "[]";
            } else {
                Stream<Object[]> items = plotData.entrySet().stream().sequential()
                        .map((e) -> new Object[]{e.getKey(), e.getValue()});

                InputStream is = JsonHandler.getInstance().marshalStreamToJsonArray(items, false);
                return IOUtils.toString(is);
            }
        }
    }

    class Datum {
        Comparable group;
        Float value;

        Datum(Comparable group, Float value) {
            this.group = group;
            this.value = value;
        }

        Comparable getGroup() {
            return group;
        }

        Float getValue() {
            return value;
        }
    }
}
