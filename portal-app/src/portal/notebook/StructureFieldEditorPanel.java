
package portal.notebook;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolFormatException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(StructureFieldEditorPanel.class);
    public static final String EMPTY_MRV = "<cml><MDocument></MDocument></cml>";
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
                String mrv = toMrv(model.getObject());
                LOGGER.info(mrv);
                if (mrv == null) {
                    mrv = EMPTY_MRV;
                }
                marvinSketcherPanel.setSketchData(ajaxRequestTarget, mrv, "mrv");
                marvinSketcherPanel.showModal();
            }
        });
        add(sketchThumbnail);

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", uniqueMarvinName);
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                String mrv = marvinSketcherPanel.getSketchData();
                LOGGER.info(mrv);
                String smiles = toSmiles(mrv);
                model.setObject(smiles);
                refreshThumbnail();
                marvinSketcherPanel.hideModal();
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);
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
            String smiles = model.getObject();
            String sketchData = smiles == null ? EMPTY_MRV : toMrv(smiles);
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

    private String toSmiles(String mrv) {
        try {
            if (mrv == null) {
                return null;
            } else {
                Molecule molecule = MolImporter.importMol(mrv);
                return MolExporter.exportToFormat(molecule, "smiles");
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private String toMrv(String smiles) {
        try {
            if (smiles == null) {
               return null;
            } else {
                Molecule molecule = MolImporter.importMol(smiles);
                return MolExporter.exportToFormat(molecule, "mrv");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Rectangle getRectangle() {
        return RECTANGLE;
    }
}
