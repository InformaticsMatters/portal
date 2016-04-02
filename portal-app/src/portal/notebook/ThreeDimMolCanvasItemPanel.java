package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import portal.PortalWebApplication;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.cells.DefaultCellDefinitionRegistry;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * @author simetrias
 */
public class ThreeDimMolCanvasItemPanel extends CanvasItemPanel {

    private static final String JS_INIT_VIEWER = "init3DMolViewer(':data')";
    private static final String VAR_NAME_SELECTION = "selection";
    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public ThreeDimMolCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == 0) {
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
        response.render(OnDomReadyHeaderItem.forScript(JS_INIT_VIEWER.replace(":data", convertForJavaScript(getSampleData()))));
        makeCanvasItemResizable(container, "fit3DViewer", 325, 270);
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        CellInstance cellInstance = findCellInstance();
        BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(DefaultCellDefinitionRegistry.VAR_NAME_INPUT);
        if (bindingInstance != null) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            boolean isOfInterest = variableInstance != null && changedCellId.equals(variableInstance.getCellId());
            if (isOfInterest) {
                MoleculeObject moleculeObject = notebookSession.readMoleculeValue(variableInstance);
                Map<Object, Object> representations = moleculeObject.getRepresentations();
                for (Object representation : representations.keySet()) {
                    System.out.println(representation.toString() + " - " + representations.get(representation).getClass().getName());
                }
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

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

    private String getSampleData() {
        try {
            try (InputStream inputStream = PortalWebApplication.class.getResourceAsStream("resources/kinase_inhibs.sdf"); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
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

    class ModelObject implements Serializable {

    }
}
