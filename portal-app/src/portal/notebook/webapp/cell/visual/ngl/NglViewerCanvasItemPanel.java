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
import portal.notebook.api.*;
import portal.notebook.webapp.AbstractD3CanvasItemPanel;
import portal.notebook.webapp.CellChangeEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class NglViewerCanvasItemPanel extends AbstractD3CanvasItemPanel {

    private static final Logger LOG = Logger.getLogger(NglViewerCanvasItemPanel.class.getName());
    private static final String BUILD_PLOT_JS = "buildNglViewer(':id', :data)";


    private final ModelObject model = new ModelObject();
    private Form<ModelObject> form;
    //private ScatterPlotAdvancedOptionsPanel advancedOptionsPanel;

    public NglViewerCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(480);
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
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/nglviewer/ngl.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/nglviewer/nglviewer.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/marcj_css_element_queries/ResizeSensor.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/nglviewer/nglviewer.css")));
        //makeCanvasItemResizable(container, "fitNglViewer", 400, 400);
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
        model.data = null;
    }

    private void refreshPlotData(boolean readDataset) throws Exception {

        CellInstance cellInstance = findCellInstance();
        BindingInstance bindingInstance1 = cellInstance.getBindingInstanceMap().get("input1");
        VariableInstance variableInstance1 = bindingInstance1.getVariableInstance();

        if (variableInstance1 != null) {
            Dataset<? extends BasicObject> dataset = readDataset ? notebookSession.squonkDataset(variableInstance1) : null;
            DatasetMetadata meta = dataset == null ? notebookSession.squonkDatasetMetadata(variableInstance1) : dataset.getMetadata();
            if (meta.getType() != MoleculeObject.class) {
                model.data = null;
                model.error = "Input must be molecules";
            } else {
                if (readDataset) {
                    NglMoleculeSet mols = generateSdf((Dataset<MoleculeObject>)dataset);
                    model.setData(mols);
                    LOG.info("Loaded molecules\n" + mols.getMolecules());
                } else {
                    model.data = null;
                }
            }
        }
    }

    private NglMoleculeSet generateSdf(Dataset<MoleculeObject> dataset) throws IOException {
        StringBuilder b = new StringBuilder();
        AtomicInteger count = new AtomicInteger(0);
        dataset.getStream().limit(5).forEachOrdered((mo) -> {
            String format = mo.getFormat();
            if (format.equals("mol") || format.startsWith("mol:")) {
                //mo.getUUID().toString(), mo.getSource());
                b.append(mo.getSource());
                b.append("\n$$$$$");
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
        LOG.info("JSON: " + json);
        String result = BUILD_PLOT_JS
                .replace(":id", getMarkupId())
                .replace(":data", json);

        return result;
    }

    public String getStatusString() {
        String error = model.getError();
        if (error != null) {
            return error;
        } else {
            StringBuilder b = new StringBuilder();
            NglMoleculeSet data = model.getData();
            if (data == null || data.getMolecules() == null || data.getSize() == null || data.getSize() == 0) {
                b.append("Loading ...");
            } else {
                b.append(data.getSize()).append(" records, ");
            }
            return b.toString();
        }
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

        NglMoleculeSet data;
        String error;

        NglMoleculeSet getData() {
            return data;
        }

        void setData(NglMoleculeSet mols) {
            this.data = mols;
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
                return JsonHandler.getInstance().objectToJson(data);
            } catch (JsonProcessingException e) {
                LOG.log(Level.WARNING, "Failed to geenrate JSON for NGL Viewer", e);
                return null;
            }
        }


    }


}
