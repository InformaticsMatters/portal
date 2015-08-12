
package portal.webapp;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import com.im.lac.services.ServicePropertyDescriptor;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import toolkit.wicket.marvinjs.MarvinSketcher;

import java.awt.*;

/**
 * @author simetrias
 */
public class StructurePropertyEditorPanel extends Panel {

    public static final Rectangle RECTANGLE = new Rectangle(200, 130);
    private MarvinSketcher marvinSketcherPanel;
    private NonCachingImage sketchThumbnail;

    public StructurePropertyEditorPanel(String id, ServicePropertyDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        super(id);
        addComponents(servicePropertyDescriptor, servicePropertyModel);
    }

    private void addComponents(ServicePropertyDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        add(new Label("label", servicePropertyDescriptor.getLabel()));


        RenderedDynamicImageResource renderedDynamicImageResource = new RenderedDynamicImageResource(getRectangle().width, getRectangle().height) {

            @Override
            protected boolean render(Graphics2D graphics2D, Attributes attributes) {
                return renderThumbnail(graphics2D);
            }
        };

        sketchThumbnail = new NonCachingImage("sketch", renderedDynamicImageResource);
        sketchThumbnail.add(AttributeModifier.append("style", "width: " + getRectangle().width + "px; height: " + getRectangle().height + "px;"));
        sketchThumbnail.setOutputMarkupId(true);
        sketchThumbnail.add(new AjaxEventBehavior("onclick") {

            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                marvinSketcherPanel.showModal();
            }
        });
        add(sketchThumbnail);

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", "modalElement");
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                marvinSketcherPanel.hideModal();
                getRequestCycle().find(AjaxRequestTarget.class).add(sketchThumbnail);
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);
    }

    private boolean renderThumbnail(Graphics2D graphics2D) {
        try {
            MolPrinter molPrinter = new MolPrinter();
            String sketchData = marvinSketcherPanel.getSketchData();
            if (sketchData == null) {
                sketchData = "<cml><MDocument></MDocument></cml>";
            }
            Molecule molecule = MolImporter.importMol(sketchData);
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
