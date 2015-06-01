package portal.webapp;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import toolkit.wicket.marvin4js.MarvinSketcher;

import java.awt.*;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPopupPanel extends Panel {

    public static final Rectangle RECTANGLE = new Rectangle(200, 130);
    private RenderedDynamicImageResource renderedDynamicImageResource;
    private ServiceCanvasItemPanel.Callbacks callbacks;
    private MarvinSketcher marvinSketcherPanel;
    private NonCachingImage sketchThumbnail;

    public ServiceCanvasItemPopupPanel(String id, ServiceCanvasItemPanel.Callbacks callbacks) {
        super(id);
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        addActions(callbacks);
        addSketchThumbnail();
    }

    private void addActions(final ServiceCanvasItemPanel.Callbacks callbacks) {
        add(new AjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onDelete();
            }
        });

        add(new AjaxLink("sketcher") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                marvinSketcherPanel.showModal();
            }
        });

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", "modalElement");
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                getRequestCycle().find(AjaxRequestTarget.class).add(sketchThumbnail);
                marvinSketcherPanel.hideModal();
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);
    }

    private void addSketchThumbnail() {
        renderedDynamicImageResource = new RenderedDynamicImageResource(getRectangle().width, getRectangle().height) {

            @Override
            protected boolean render(Graphics2D graphics2D, Attributes attributes) {
                return renderThumbnail(graphics2D);
            }
        };

        sketchThumbnail = new NonCachingImage("sketch", renderedDynamicImageResource);
        sketchThumbnail.setOutputMarkupId(true);
        add(sketchThumbnail);
    }

    private boolean renderThumbnail(Graphics2D graphics2D) {
        try {
            MolPrinter molPrinter = new MolPrinter();
            Molecule molecule = MolImporter.importMol(marvinSketcherPanel.getSketchData());
            molecule.dearomatize();
            molPrinter.setMol(molecule);
            graphics2D.setColor(Color.white);
            graphics2D.fillRect(0, 0, getRectangle().width, getRectangle().height);
            double scale = molPrinter.maxScale(getRectangle());
            molPrinter.setScale(scale);
            molPrinter.paint(graphics2D, getRectangle());
            return true;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected Rectangle getRectangle() {
        return RECTANGLE;
    }
}
