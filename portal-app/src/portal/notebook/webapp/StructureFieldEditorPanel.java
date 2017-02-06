
package portal.notebook.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.squonk.core.client.StructureIOClient;
import org.squonk.io.DepictionParameters;
import org.squonk.options.types.Structure;
import toolkit.wicket.marvinjs.MarvinSketcher;

import java.awt.*;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class StructureFieldEditorPanel extends FieldEditorPanel<Structure> {

    public static final Rectangle RECTANGLE = new Rectangle(250, 140);
    private static final Logger LOG = Logger.getLogger(StructureFieldEditorPanel.class.getName());
    private final String uniqueMarvinName;
    private MarvinSketcher marvinSketcherPanel;
    private NonCachingImage sketchThumbnail;
    private Model<Structure> model;
    private final StructureIOClient structureIOClient;

    public StructureFieldEditorPanel(String id, String uniqueMarvinName, FieldEditorModel fieldEditorModel, StructureIOClient structureIOClient) {
        super(id, fieldEditorModel);
        this.uniqueMarvinName = uniqueMarvinName;
        this.structureIOClient = structureIOClient;
        addComponents();
    }

    private void addComponents() {
        model = new Model<Structure>(){
            @Override
            public Structure getObject() {
                Object o = getFieldEditorModel().getValue();
                if (o instanceof String) {
                    // old data as String in mrv format
                    return new Structure((String)o, "mrv");
                } else if (o instanceof Structure) {
                    return (Structure)o;
                } else {
                    LOG.warning("Unexpected format for structure option. Setting to null");
                    return new Structure(null, "mol");
                }
            }

            @Override
            public void setObject(Structure object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));

        Structure struct = model.getObject();
        DynamicImageResource imgResource = new SvgMoleculeObjectImageResource(struct.getSource(), struct.getFormat());

        sketchThumbnail = new NonCachingImage("sketch", imgResource);
        sketchThumbnail.add(AttributeModifier.append("style", "width: " + getRectangle().width + "px; height: " + getRectangle().height + "px;"));
        sketchThumbnail.setOutputMarkupId(true);
        sketchThumbnail.add(new AjaxEventBehavior("onclick") {

            @Override
            protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                Structure struct = model.getObject();
                String mol = struct.getSource();
                LOG.info("Setting mol to sketcher: " + mol);
                if (mol != null && !mol.isEmpty()) {
                    marvinSketcherPanel.setSketchData(ajaxRequestTarget, mol, struct.getFormat());
                }
                marvinSketcherPanel.showModal();
            }
        });
        add(sketchThumbnail);

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", uniqueMarvinName);
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                String mol = marvinSketcherPanel.getSketchData();
                System.out.println("Sketched Mol: " + mol);
                // wierd bug (in MarvinJS?) that cuts off the first newline of a molfile so we need to add it back
                model.setObject(new Structure("\n" + mol, "mol"));
                refreshThumbnail();
                marvinSketcherPanel.hideModal();
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);
    }

    private void refreshThumbnail() {
        Structure struct = model.getObject();
        sketchThumbnail.setImageResource(new SvgMoleculeObjectImageResource(struct.getSource(), struct.getFormat()));
        getRequestCycle().find(AjaxRequestTarget.class).add(sketchThumbnail);
    }

    private Rectangle getRectangle() {
        return RECTANGLE;
    }

    @Override
    public void enableEditor(boolean editable) {
        marvinSketcherPanel.setEnabled(editable);
    }


    class SvgMoleculeObjectImageResource extends DynamicImageResource {

        private final String mol;
        private final String format;

        public SvgMoleculeObjectImageResource(String mol, String format) {
            super("svg");
            this.mol = mol;
            this.format = format;
        }

        @Override
        protected void configureResponse(ResourceResponse response, Attributes attributes) {
            super.configureResponse(response, attributes);
            response.setContentType("image/svg+xml");
        }

        @Override
        protected byte[] getImageData(Attributes attributes) {
            DepictionParameters params = new DepictionParameters(25, 14);
            params.setExpandToFit(true);
            params.setMargin(1.0);
            byte[] bytes = structureIOClient.renderImage(mol, format, DepictionParameters.OutputFormat.svg, params);
            //String svg = new String(bytes);
            return bytes;
        }
    }

}