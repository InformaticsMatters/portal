package portal.notebook.webapp;

import chemaxon.calculations.clean.Cleaner;
import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;
import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import portal.PortalWebApplication;
import portal.notebook.api.DefaultCellDefinitionRegistry;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class ThreeDimMolCanvasItemPanel extends CanvasItemPanel {

    private static final String JS_INIT_VIEWER = "init3DMolViewer(':data', ':format')";
    private static final String JS_SET_VIEWER_DATA = "set3DMolViewerData(':data', ':format')";
    private Form<ModelObject> form;
    private ThreeDimMolAdvancedOptionsPanel advancedOptionsPanel;
    @Inject
    private NotebookSession notebookSession;

    public ThreeDimMolCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        BindingsPanel.CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(500);
            cellInstance.setSizeHeight(250);
        }
        addForm();
        addTitleBar();
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
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        BindingsPanel.CellInstance cellInstance = findCellInstance();
        BindingsPanel.BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(DefaultCellDefinitionRegistry.VAR_NAME_INPUT);
        if (bindingInstance != null) {
            BindingsPanel.VariableInstance variableInstance = bindingInstance.getVariableInstance();
            boolean isOfInterest = variableInstance != null && changedCellId.equals(variableInstance.getCellId());
            if (isOfInterest) {
                MoleculeObject moleculeObject = notebookSession.readMoleculeValue(variableInstance);
                String data = convertToFormat(moleculeObject.getSource(), "sdf");
                ajaxRequestTarget.appendJavaScript(JS_SET_VIEWER_DATA.replace(":data", convertForJavaScript(data)).replace(":format", "sdf"));
            }
        }
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
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

    private void loadInitialSampleData(IHeaderResponse response) {
        // String data = convertForJavaScript(getSampleData("/portal/resources/kinase_inhibs.sdf"));
        String sampleData = getSampleData("/portal/resources/caffeine.mol");
        String data = convertForJavaScript(convertToFormat(sampleData, "sdf"));
        String js = JS_INIT_VIEWER.replace(":data", data).replace(":format", "sdf");
        response.render(OnDomReadyHeaderItem.forScript(js));
    }

    private String convertToFormat(String input, String format) {
        try {
            if (input == null || input.isEmpty()) {
                return null;
            } else {
                Molecule molecule = MolImporter.importMol(input);
                if (molecule.getDim() != 3) {
                    // convert to 3D if not already
                    Cleaner.clean(molecule, 3);
                }
                return MolExporter.exportToFormat(molecule, format);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        advancedOptionsPanel = new ThreeDimMolAdvancedOptionsPanel("advancedOptionsPanel", getCellId());
        advancedOptionsPanel.setCallbackHandler(new ThreeDimMolAdvancedOptionsPanel.CallbackHandler() {

            @Override
            public void onApplyAdvancedOptions() {
                AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
                target.appendJavaScript("downloadPdb(':pdbId')".replace(":pdbId", advancedOptionsPanel.getPdbId()));
            }
        });
        advancedOptionsPanel.setPdbId("1UBQ");
    }

    class ModelObject implements Serializable {

    }
}
