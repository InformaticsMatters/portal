package portal.notebook.webapp.cell.visual.ngl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.Utils;
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.webapp.CanvasItemPanel;
import portal.notebook.webapp.CellChangeEvent;
import portal.notebook.webapp.NotebookSession;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Tim Dudgeon
 */
public class NglViewerCanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOG = Logger.getLogger(NglViewerCanvasItemPanel.class.getName());
    private static final int MAX_MOLS = 100;

    public static final String OPTION_DISPLAY1 = "display1";
    public static final String OPTION_DISPLAY2 = "display2";


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
            LOG.fine("processCellChanged: " + evt);
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

    private void addForm() {

        TextField<String> config = new HiddenField<>("config", new Model<>(""));
        TextField<String> display1 = new HiddenField<>("display1", new Model<>(""));
        TextField<String> display2 = new HiddenField<>("display2", new Model<>(""));

        form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);
        form.add(config);
        form.add(display1);
        form.add(display2);

        AjaxButton updateConfigButton = new AjaxButton("updateConfig") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String configJson = config.getValue();
                //LOG.info("Config: " + configJson);

                String displayJson1 = display1.getValue();
                String displayJson2 = display2.getValue();
                //LOG.info("Display1: " + displayJson1);
                //LOG.info("Display2: " + displayJson2);


                CellInstance cell = findCellInstance();
                cell.getOptionInstanceMap().get(OPTION_CONFIG).setValue(configJson);
                cell.getOptionInstanceMap().get(OPTION_DISPLAY1).setValue(displayJson1);
                cell.getOptionInstanceMap().get(OPTION_DISPLAY2).setValue(displayJson2);

                saveNotebook();

                notifyOptionValuesChanged(OPTION_CONFIG, target);
                notifyOptionValuesChanged(OPTION_DISPLAY1, target);
                notifyOptionValuesChanged(OPTION_DISPLAY2, target);
                updateAndNotifyCellStatus(target);
            }
        };
        updateConfigButton.setDefaultFormProcessing(false);
        form.add(updateConfigButton);

    }

    private void invalidatePlotData() {
        //model.setData(null, null);
    }

    private void refreshPlotData(boolean readDataset) throws Exception {

        CellInstance cellInstance = findCellInstance();
        BindingInstance bindingInstance1 = cellInstance.getBindingInstanceMap().get("input1");
        VariableInstance variableInstance1 = bindingInstance1.getVariableInstance();
        BindingInstance bindingInstance2 = cellInstance.getBindingInstanceMap().get("input2");
        VariableInstance variableInstance2 = bindingInstance2.getVariableInstance();

        Set<UUID> selectionFilter1 = cellInstance.readOptionBindingFilter(OPTION_FILTER_IDS + 1);
        Set<UUID> selectionFilter2 = cellInstance.readOptionBindingFilter(OPTION_FILTER_IDS + 2);

        NglMoleculeSet mols1 = null;
        NglMoleculeSet mols2 = null;

        if (readDataset) {
            mols1 = generateData(variableInstance1, 1, selectionFilter1);
            mols2 = generateData(variableInstance2, 2, selectionFilter2);

            model.setData(mols1, mols2);
            if ((mols1 != null && mols1.getSize() >= MAX_MOLS) || (mols2 != null && mols2.getSize() >= MAX_MOLS)) {
                model.setError("WARN: " + MAX_MOLS + " mol limit reached");
            }

            String configJson = getOptionValue(cellInstance, OPTION_CONFIG, String.class);
            model.setConfig(configJson);

            String displayJson1 = null;
            String displayJson2 = null;
            if (!model.data1IsNew) {
                displayJson1 = getOptionValue(cellInstance, OPTION_DISPLAY1, String.class);
            } else {
                cellInstance.getOptionInstanceMap().get(OPTION_DISPLAY1).setValue(null);
            }
            if (!model.data2IsNew) {
                displayJson2 = getOptionValue(cellInstance, OPTION_DISPLAY2, String.class);
            } else {
                cellInstance.getOptionInstanceMap().get(OPTION_DISPLAY2).setValue(null);
            }

            //LOG.info("Display1: " + displayJson1);
            //LOG.info("Display2: " + displayJson2);

            model.setDisplays(displayJson1, displayJson2);
        }
    }

    private NglMoleculeSet generateData(VariableInstance variableInstance, int index, Set<UUID> selectionFilter) throws Exception {

        if (variableInstance != null) {

            if (CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON.equalsIgnoreCase(variableInstance.getVariableDefinition().getMediaType())) {
                Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variableInstance);
                DatasetMetadata meta = dataset == null ? notebookSession.squonkDatasetMetadata(variableInstance) : dataset.getMetadata();
                if (meta.getType() != MoleculeObject.class) {
                    model.error = "Input" + index + " does not contain molecules";
                } else {
                    return generateSdf((Dataset<MoleculeObject>) dataset, selectionFilter);
                }
            } else if (CommonMimeTypes.MIME_TYPE_PDB.equalsIgnoreCase(variableInstance.getVariableDefinition().getMediaType())) {
                InputStream is = notebookSession.readStreamValue(variableInstance);
                if (is != null) {
                    String pdb = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(is));
                    //LOG.info("PDB:\n" + pdb);
                    return new NglMoleculeSet("text/plain", "pdb", pdb, 1);
                }
            }
        }
        return null;
    }

    private NglMoleculeSet generateSdf(Dataset<MoleculeObject> dataset, Set<UUID> selectionFilter) throws IOException {
        StringBuilder b = new StringBuilder();
        AtomicInteger count = new AtomicInteger(0);

        Stream<MoleculeObject> input = dataset.getStream();
        if (selectionFilter != null) {
            input = input.filter((o) -> selectionFilter.contains(o.getUUID()));
        }

        input.limit(MAX_MOLS).forEachOrdered((mo) -> {
            String format = mo.getFormat();
            if (format.equals("mol") || format.startsWith("mol:")) {
                b.append(mo.getSource());
                b.append("\n$$$$\n");
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

        String data = model.getDataAsJson();
        String config = model.getConfigAsJson();
        String displays = model.getDisplaysAsJson();

        StringBuilder b = new StringBuilder("buildNglViewer('");
        b.append(getMarkupId()).append("',")
                .append(data).append(",")
                .append(config).append(",")
                .append(displays).append(")");

        String s = b.toString();
        //LOG.info("JSON: " + s);
        return s;
    }

    public String getStatusString() {
        StringBuilder b = new StringBuilder();

        String error = model.getError();
        if (error != null) {
            b.append(error);
        }

        NglMoleculeSet data1 = model.getData1();
        NglMoleculeSet data2 = model.getData2();
        if (data1 != null && data1.getMolecules() != null && data1.getSize() != null) {
            if (b.length() > 0) {
                b.append("; ");
            }
            b.append(data1.getSize()).append(" input1s");
        }

        if (data2 != null && data2.getMolecules() != null && data2.getSize() != null) {
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

    private static final String DATA__UNCHANGED = "DATA__UNCHANGED";

    class ModelObject implements Serializable {

        NglMoleculeSet data1;
        NglMoleculeSet data2;
        Boolean data1IsNew;
        Boolean data2IsNew;
        String config;
        String display1;
        String display2;
        String error;

        NglMoleculeSet getData1() {
            return data1;
        }

        NglMoleculeSet getData2() {
            return data2;
        }

        void setData(NglMoleculeSet data1, NglMoleculeSet data2) {

            if (data1IsNew == null) {
                data1IsNew = false;
            } else if (!Utils.safeEqualsIncludeNull(this.data1, data1)) {
                data1IsNew = true;
            } else {
                data1IsNew = false;
            }
            this.data1 = data1;

            if (data2IsNew == null) {
                data2IsNew = false;
            } else if (!Utils.safeEqualsIncludeNull(this.data2, data2)) {
                data2IsNew = true;
            } else {
                data2IsNew = false;
            }
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
            return generateJsonArray(data1, data2);
        }

        private void setConfig(String config) {
            this.config = config;
        }

        private String getConfig() {
            return config;
        }

        private String getConfigAsJson() {
            // config is already JSON
            return config;
        }

        private void setDisplays(String display1, String display2) {
            this.display1 = display1;
            this.display2 = display2;
        }

        private String getDisplaysAsJson() {
            // display values are already JSON
            // only provide display values if the data has not changed
            String d1 = (data1IsNew ? null : display1);
            String d2 = (data2IsNew ? null : display2);
            if (d1 == null && d2 == null) {
                return null;
            } else {
                return "[" + (d1 == null ? "null" : d1) + "," + (d2 == null ? "null" : d2) + "]";
            }
        }

        private String getDisplay1() {
            return display1;
        }

        private String getDisplay2() {
            return display2;
        }

        private String generateJsonArray(Object item1, Object item2) {
            if (item1 == null && item2 == null) {
                return null;
            }
            try {
                return JsonHandler.getInstance().objectToJson(new Object[] {item1, item2});
            } catch (JsonProcessingException e) {
                LOG.log(Level.WARNING, "Failed to generate JSON for NGL Viewer", e);
                return null;
            }
        }
    }

}
