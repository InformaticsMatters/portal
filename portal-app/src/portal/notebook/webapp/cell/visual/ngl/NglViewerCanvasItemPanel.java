package portal.notebook.webapp.cell.visual.ngl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.OptionInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.webapp.CanvasItemPanel;
import portal.notebook.webapp.CellChangeEvent;
import portal.notebook.webapp.NotebookSession;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tim Dudgeon
 */
public class NglViewerCanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOG = Logger.getLogger(NglViewerCanvasItemPanel.class.getName());
    private static final String BUILD_PLOT_JS = "buildNglViewer(':id', :data)";
    private static final int MAX_MOLS = 100;

    private final ModelObject model = new ModelObject();
    private Form<ModelObject> form;

    @Inject
    private NotebookSession notebookSession;

    public NglViewerCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(500);
            cellInstance.setSizeHeight(400);
        }
        addForm();
        loadModelFromPersistentData();
        addTitleBar();
        addStatus();
        try {
            refreshPlotData(false);
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error refreshing data", t);
            notifyMessage("Error", "Failed to refresh data" + t.getLocalizedMessage());
        }
    }

    private void addStatus() {
        add(createStatusLabel("cellStatus"));
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();

        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/nglviewer/nglviewer.css")));

        addJslibs(response, new String[]{
                "resources/nglviewer/ngl.js",
                "resources/nglviewer/nglviewer.js"
        });


        makeCanvasItemResizable(container, "fitNglViewer", 330, 270);
    }

    private void addJslibs(IHeaderResponse response, String... path) {
        for (int i = 0; i < path.length; i++) {
            response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, path[i])));
        }
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

    }

    private void addForm() {

        form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);

    }

    private void invalidatePlotData() {
        model.setData(null, null);
    }

    private void refreshPlotData(boolean readDataset) throws Exception {

        CellInstance cellInstance = findCellInstance();
        BindingInstance bindingInstance1 = cellInstance.getBindingInstanceMap().get("input1");
        VariableInstance variableInstance1 = bindingInstance1.getVariableInstance();
        BindingInstance bindingInstance2 = cellInstance.getBindingInstanceMap().get("input2");
        VariableInstance variableInstance2 = bindingInstance2.getVariableInstance();

        NglMoleculeSet mols1 = null;
        NglMoleculeSet mols2 = null;

        if (readDataset) {
            mols1 = generateData(variableInstance1, 1);
            mols2 = generateData(variableInstance2, 2);
        }

        model.setData(mols1, mols2);
        if ((mols1 != null && mols1.getSize() >= MAX_MOLS) || (mols2 != null && mols2.getSize() >= MAX_MOLS)) {
            model.setError("WARN: " + MAX_MOLS + " mol limit reached");
        }
    }

    private NglMoleculeSet generateData(VariableInstance variableInstance, int index) throws Exception {
        if (variableInstance != null) {
            Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variableInstance);
            DatasetMetadata meta = dataset == null ? notebookSession.squonkDatasetMetadata(variableInstance) : dataset.getMetadata();
            if (meta.getType() != MoleculeObject.class) {
                model.error = "Input" + index + " does not contain molecules";
            } else {
                return generateSdf((Dataset<MoleculeObject>) dataset);
            }
        }
        return null;
    }

    private NglMoleculeSet generateSdf(Dataset<MoleculeObject> dataset) throws IOException {
        StringBuilder b = new StringBuilder();
        AtomicInteger count = new AtomicInteger(0);
        dataset.getStream().limit(MAX_MOLS).forEachOrdered((mo) -> {
            String format = mo.getFormat();
            if (format.equals("mol") || format.startsWith("mol:")) {
                //mo.getUUID().toString(), mo.getSource());
                b.append(mo.getSource());
                b.append("\n$$$$$\n");
                count.incrementAndGet();
            } else {
                LOG.warning("Invalid molecule format for NGL Viewer: " + format);
            }
        });

        return new NglMoleculeSet("text/plain", "sdf", b.toString(), count.get());
    }


    private void rebuildPlot() {
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
        target.appendJavaScript(buildPlotJs());
    }


    private String buildPlotJs() {

        String json = model.getDataAsJson();
        //LOG.info("JSON: " + json);
        String result = BUILD_PLOT_JS
                .replace(":id", getMarkupId())
                .replace(":data", json);

        return result;
    }

    public String getStatusString() {
        StringBuilder b = new StringBuilder();

        String error = model.getError();
        if (error != null) {
            b.append(error);
        }

        NglMoleculeSet data1 = model.getData1();
        NglMoleculeSet data2 = model.getData2();
        if (data1 != null && data1.getMolecules() != null && data1.getSize() != null && data1.getSize() > 0) {
            if (b.length() > 0) {
                b.append("; ");
            }
            b.append(data1.getSize()).append(" input1s");
        }

        if (data2 != null && data2.getMolecules() != null && data2.getSize() != null && data2.getSize() > 0) {
            if (b.length() > 0) {
                b.append("; ");
            }
            b.append(data2.getSize()).append(" input2s");
        }
        if (b.length() == 0) {
            b.append("Loading...");
        }

        return b.toString();
    }

//    @Override
//    public Panel getAdvancedOptionsPanel() {
//        if (advancedOptionsPanel == null) {
//            createAdvancedOptionsPanel();
//        }
//        return advancedOptionsPanel;
//    }
//
//    private void createAdvancedOptionsPanel() {
//        advancedOptionsPanel = new ScatterPlotAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
//        advancedOptionsPanel.setCallbackHandler(new DefaultCallbackHandler() {
//
//            @Override
//            public void onApplyAdvancedOptions() throws Exception {
//                CellInstance cellInstance = findCellInstance();
//
//                notebookSession.storeCurrentEditable();
//
//                onExecute();
//            }
//
//        });
//    }

    class ModelObject implements Serializable {

        NglMoleculeSet data1;
        NglMoleculeSet data2;
        String error;

        NglMoleculeSet getData1() {
            return data1;
        }

        NglMoleculeSet getData2() {
            return data2;
        }

        void setData(NglMoleculeSet data1, NglMoleculeSet data2) {
            this.data1 = data1;
            this.data2 = data2;
            this.error = null;
        }

        String getError() {
            return error;
        }

        void setError(String error) {
            this.error = error;
        }

        private String getDataAsJson() {
            try {
                NglMoleculeSet[] arr = new NglMoleculeSet[2];
                arr[0] = data1;
                arr[1] = data2;
                return JsonHandler.getInstance().objectToJson(arr);
            } catch (JsonProcessingException e) {
                LOG.log(Level.WARNING, "Failed to geenrate JSON for NGL Viewer", e);
                return null;
            }
        }


    }


}
