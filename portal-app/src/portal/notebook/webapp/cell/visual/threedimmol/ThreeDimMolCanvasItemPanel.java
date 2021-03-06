package portal.notebook.webapp.cell.visual.threedimmol;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.squonk.dataset.Dataset;
import org.squonk.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import org.squonk.util.IOUtils;
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.webapp.CanvasItemPanel;
import portal.notebook.webapp.CellChangeEvent;
import portal.notebook.webapp.NotebookSession;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class ThreeDimMolCanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOG = Logger.getLogger(ThreeDimMolCanvasItemPanel.class.getName());

    private static final String JS_INIT_VIEWER = "init3DMolViewer(':id', ':data', ':format')";
    private static final String JS_SET_VIEWER_DATA = "set3DMolViewerData(':data', ':format')";
    private Form<ModelObject> form;
    private WebMarkupContainer webglPanel;
    private ThreeDimMolAdvancedOptionsPanel advancedOptionsPanel;
    @Inject
    private NotebookSession notebookSession;
    private Label statusLabel;

    public ThreeDimMolCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(500);
            cellInstance.setSizeHeight(250);
        }
        addForm();
        addWebglPanel();
        addTitleBar();
        addStatus();
    }

    private static String convertForJavaScript(String input) {
        String value = input;
        value = value.replace("\r\n", "\n");
        value = value.replace("\r", "\n");
        value = value.replace("\\", "\\\\");
        value = value.replace("\n", "\\n");
        value = value.replace("\"", "\\\"");
        value = value.replace("'", "\\'");
        return value;
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/3Dmol-nojquery.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/threedimmol.js")));
        response.render(OnDomReadyHeaderItem.forScript("fit3DViewer('" + getMarkupId() + "')"));
        loadInitialSampleData(response);
        makeCanvasItemResizable(container, "fit3DViewer", 325, 270);
    }

    @Override
    public void processCellChanged(CellChangeEvent evt, AjaxRequestTarget ajaxRequestTarget) throws Exception {

        // TODO - convert this to support selections/filters from other cells

        super.processCellChanged(evt, ajaxRequestTarget);
        CellInstance cellInstance = findCellInstance();
        BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        if (bindingInstance != null) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            boolean isOfInterest = variableInstance != null && evt.getSourceCellId().equals(variableInstance.getCellId());
            if (isOfInterest) {
                // TODO - support multiple section - input should be Dataset<MoleculeObject>
                // It should them be converted to SDF using StructureIOClient (to avoid depenedency on Marvin)
                // and then set to the 3DMol viewer as SDF.
                MoleculeObject mol = notebookSession.readMoleculeValue(variableInstance);
                String data = convertToSdf(mol);
                if (data == null) data = "";
                ajaxRequestTarget.appendJavaScript(JS_SET_VIEWER_DATA.replace(":data", convertForJavaScript(data)).replace(":format", "sdf"));
            }
        }
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public WebMarkupContainer getContentPanel() {
        return null;
    }

    @Override
    public void onExecute() {
    }

    @Override
    public Panel getAdvancedOptionsPanel() {
        if (advancedOptionsPanel == null) {
            createAdvancedOptionsPanel();
        }
        return advancedOptionsPanel;
    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

    private void addWebglPanel() {
        webglPanel = new WebMarkupContainer("gldiv");
        webglPanel.setOutputMarkupId(true);
        add(webglPanel);
    }

    private void loadInitialSampleData(IHeaderResponse response) {
        String sampleData = getSampleData("/portal/resources/caffeine.mol");
        String data = convertForJavaScript(sampleData + "\n$$$$");
        System.out.println("SDF: " + data);
        String js = JS_INIT_VIEWER.replace(":id", webglPanel.getMarkupId()).replace(":data", data).replace(":format", "sdf");
        response.render(OnDomReadyHeaderItem.forScript(js));
    }

    private String convertToSdf(MoleculeObject mol) {
        try {
            if (mol == null || mol.getSource().isEmpty()) {
                return null;
            } else {
                Dataset<MoleculeObject> dataset = new Dataset(MoleculeObject.class, Collections.singletonList(mol));
                InputStream is = notebookSession.getStructureIOClient().datasetExportToSdf(dataset, false);
                String sdf = IOUtils.convertStreamToString(is);
                return sdf;
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to convert structure", e);
            return null;
        }
    }

    private String getSampleData(String resourcePath) {
        try {
            try (InputStream inputStream = PortalWebApplication.class.getResourceAsStream(resourcePath); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                transfer(inputStream, outputStream);
                outputStream.flush();
                return outputStream.toString();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int r = inputStream.read(buffer, 0, buffer.length);
        while (r > -1) {
            outputStream.write(buffer, 0, r);
            r = inputStream.read(buffer, 0, buffer.length);
        }
    }

    private void createAdvancedOptionsPanel() {
        advancedOptionsPanel = new ThreeDimMolAdvancedOptionsPanel("advancedOptionsPanel", getCellId(), new ThreeDimMolAdvancedOptionsPanel.CallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() {
                AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
                target.appendJavaScript("downloadPdb(':pdbId')".replace(":pdbId", advancedOptionsPanel.getPdbId()));
            }
        });
        advancedOptionsPanel.setPdbId("1UBQ");
    }

    private void addStatus() {
        statusLabel = createStatusLabel("cellStatus");
        add(statusLabel);
    }

    class ModelObject implements Serializable {

    }
}
