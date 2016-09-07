package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.squonk.core.client.StructureIOClient;
import org.squonk.io.DepictionParameters;
import org.squonk.types.MoleculeObject;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 31/08/16.
 */
public class MoleculeObjectCardPanel extends BasicObjectCardPanel<MoleculeObject> {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectCardPanel.class.getName());

    private StructureIOClient client;

    MoleculeObjectCardPanel(String id, Map<String, Class> classMappings, MoleculeObject mo, StructureIOClient client) {
        super(id, classMappings, mo);
        this.client = client;
    }

    @Override
    protected void handleMainContent(MoleculeObject mo) {

        Image image = new Image("structureImage", "svg for " + mo.getUUID());
        image.setImageResource(new DynamicImageResource("svg") {

            @Override
            protected void configureResponse(ResourceResponse response, Attributes attributes) {
                super.configureResponse(response, attributes);
                response.setContentType("image/svg+xml");
            }

            @Override
            protected byte[] getImageData(Attributes attributes) {
                return client.renderImage(mo.getSource(), mo.getFormat(), DepictionParameters.OutputFormat.svg, new DepictionParameters(30, 20));
            }
        });


        add(image);
    }

}
