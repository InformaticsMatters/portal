package portal.notebook.webapp.cell.visual.image;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.squonk.types.PngImageFile;
import org.squonk.util.IOUtils;
import portal.notebook.api.CellInstance;
import portal.notebook.webapp.CanvasItemPanel;
import portal.notebook.webapp.CellBindingVariableProvider;
import portal.notebook.webapp.CellChangeEvent;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author timbo
 */
public class ImageCanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOG = Logger.getLogger(ImageCanvasItemPanel.class.getName());

    private CellBindingVariableProvider cellVariableProvider;
    private Form form;

    public ImageCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);

        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(400);
            cellInstance.setSizeHeight(300);
        }

        addForm();
        addTitleBar();
        addImage();

    }

    public CellBindingVariableProvider getCellVariableProvider() {
        if (cellVariableProvider == null) {
            cellVariableProvider = new CellBindingVariableProvider(notebookSession, getCellId(), "input");
        }
        return cellVariableProvider;
    }

    private void addForm() {
        form = new Form("form");
        form.setOutputMarkupId(true);
        add(form);
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        makeCanvasItemResizable(container, null, 100, 100);
    }

    @Override
    public void processCellChanged(CellChangeEvent evt, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        super.processCellChanged(evt, ajaxRequestTarget);
        if (doesCellChangeRequireRefresh(evt)) {
            onExecute();
        }
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() throws Exception {
        LOG.fine("Executing");
        rebuildImage();
    }

    private void rebuildImage() {
        LOG.fine("Rebuilding image");
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        target.add(this);
    }

    private void addImage() {
        Image image = new NonCachingImage("image", createImageResource());
        add(image);
    }

    DynamicImageResource createImageResource() {
        DynamicImageResource dir = new DynamicImageResource() {
            @Override
            protected byte[] getImageData(Attributes attributes) {
                try {
                    byte[] bytes = readImageBytes();
                    return bytes;
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to read image data", e);
                    notifyMessage("Error", "Failed to read image data" + e.getLocalizedMessage());
                }
                return null;
            }
        };
        return dir;
    }

    private byte[] readImageBytes() throws Exception {
        //TODO - make this support additional image formats
        PngImageFile png = getCellVariableProvider().readVariable(PngImageFile.class);
        LOG.fine("PNG: " + png);
        if (png != null && png.getInputStream() != null) {
            return  png.getBytes();
        }
        return null;
    }
}
