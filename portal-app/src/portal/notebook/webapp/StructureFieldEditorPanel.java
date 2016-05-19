
package portal.notebook.webapp;

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import com.im.lac.types.MoleculeObject;
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
import java.io.IOException;

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
                renderThumbnail(graphics2D);
                return true;
            }
        };

        sketchThumbnail = new NonCachingImage("sketch", renderedDynamicImageResource);
        sketchThumbnail.add(AttributeModifier.append("style", "width: " + getRectangle().width + "px; height: " + getRectangle().height + "px;"));
        sketchThumbnail.setOutputMarkupId(true);
        sketchThumbnail.add(new AjaxEventBehavior("onclick") {

            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                String mrv = model.getObject();
                LOGGER.info(mrv);
                if (mrv != null && !mrv.isEmpty()) {
                    marvinSketcherPanel.setSketchData(ajaxRequestTarget, mrv, "mrv");
                }
                marvinSketcherPanel.showModal();
            }
        });
        add(sketchThumbnail);

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", uniqueMarvinName);
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                String mrv = marvinSketcherPanel.getSketchData();
                model.setObject(mrv);
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
                renderThumbnail(graphics2D);
                return true;
            }
        };
        sketchThumbnail.setImageResource(renderedDynamicImageResource);
        getRequestCycle().find(AjaxRequestTarget.class).add(sketchThumbnail);
    }

    private void renderThumbnail(Graphics2D graphics2D) {
        try {
            graphics2D.setColor(Color.white);
            graphics2D.fillRect(0, 0, getRectangle().width, getRectangle().height);
            String mrv = model.getObject();
            if (mrv != null && ! mrv.isEmpty()) {
                Molecule molecule = MolImporter.importMol(mrv);
                molecule.dearomatize();
                MolPrinter molPrinter = new MolPrinter();
                molPrinter.setMol(molecule);
                double scale = molPrinter.maxScale(getRectangle());
                molPrinter.setScale(scale);
                molPrinter.paint(graphics2D, getRectangle());
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private String toSmiles(String mrv) {
        try {
            if (mrv == null || mrv.isEmpty()) {
                return null;
            } else {
                Molecule molecule = MolImporter.importMol(mrv);
                MoleculeObject mo =  exportAsString(molecule, "smiles", "cxsmiles");
                return mo.getSource();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private String toMrv(String smiles) {
        try {
            if (smiles == null || smiles.isEmpty()) {
               return null;
            } else {
                Molecule molecule = MolImporter.importMol(smiles);
                return MolExporter.exportToFormat(molecule, "mrv");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MoleculeObject convertMolecule(String molstr, String... formats) {
        try {
            if (molstr == null || molstr.isEmpty()) {
                return null;
            } else {
                Molecule molecule = MolImporter.importMol(molstr);
                return exportAsString(molecule, formats);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // this method is copied from MolecuelUtils in the chemaxon-lib module
    private static MoleculeObject exportAsString(Molecule mol, String... format) throws IOException {
        IOException ex = null;
        for (String f : format) {
            try {
                return new MoleculeObject(MolExporter.exportToFormat(mol, f), f);
            } catch (IOException e) {
                ex = e;
            }
        }
        throw ex;
    }

    private Rectangle getRectangle() {
        return RECTANGLE;
    }

    @Override
    public void enableEditor(boolean editable) {
        marvinSketcherPanel.setEnabled(editable);
    }
}