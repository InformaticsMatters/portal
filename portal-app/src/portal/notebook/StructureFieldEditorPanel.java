
package portal.notebook;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.RenderedDynamicImageResource;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.notebook.api.OptionInstance;
import toolkit.wicket.marvinjs.MarvinSketcher;

import java.awt.*;

/**
 * @author simetrias
 */
public class StructureFieldEditorPanel extends FieldEditorPanel {

    public static final Rectangle RECTANGLE = new Rectangle(200, 130);
    private static final Logger logger = LoggerFactory.getLogger(StructureFieldEditorPanel.class);
    private final String uniqueMarvinName;
    private MarvinSketcher marvinSketcherPanel;
    private NonCachingImage sketchThumbnail;
    private Model<String> model;

    public StructureFieldEditorPanel(String id, String uniqueMarvinName, FieldEditorModel fieldEditorModel) {
        super(id, fieldEditorModel);
        this.uniqueMarvinName = uniqueMarvinName;
        addComponents();
    }

    private void addComponents() {
        model = new Model<String>(){
            @Override
            public String getObject() {
                return (String)getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));


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
                String currentSketchData = marvinSketcherPanel.getSketchData();
                if (currentSketchData == null) {
                    currentSketchData = "<cml><MDocument></MDocument></cml>";
                }
                marvinSketcherPanel.setSketchData(ajaxRequestTarget, currentSketchData, "mrv");
                marvinSketcherPanel.showModal();
            }
        });
        add(sketchThumbnail);

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", uniqueMarvinName);
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                refreshThumbnail();
                temporarilyConvertToSmiles();
                marvinSketcherPanel.hideModal();
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);
    }

    private void temporarilyConvertToSmiles() {
        try {
            String sketchData = marvinSketcherPanel.getSketchData();
            if (sketchData == null) {
                sketchData = "<cml><MDocument></MDocument></cml>";
            }
            Molecule molecule = MolImporter.importMol(sketchData);
            String smiles = MolExporter.exportToFormat(molecule, "smiles");
            model.setObject(smiles);
            logger.info(smiles);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void store(OptionInstance optionInstance) {
        optionInstance.setValue(model.getObject());
    }

    private void refreshThumbnail() {
        RenderedDynamicImageResource renderedDynamicImageResource = new RenderedDynamicImageResource(getRectangle().width, getRectangle().height) {

            @Override
            protected boolean render(Graphics2D graphics2D, Attributes attributes) {
                return renderThumbnail(graphics2D);
            }
        };
        sketchThumbnail.setImageResource(renderedDynamicImageResource);
        getRequestCycle().find(AjaxRequestTarget.class).add(sketchThumbnail);
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

    private Rectangle getRectangle() {
        return RECTANGLE;
    }
}