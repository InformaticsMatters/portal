package portal.notebook.webapp.results;

import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.squonk.core.client.StructureIOClient;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.io.DepictionParameters;
import org.squonk.types.MoleculeObject;
import org.squonk.types.MoleculeObjectHighlightable;
import org.squonk.types.Scale;

import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 31/08/16.
 */
public class MoleculeObjectCardPanel extends BasicObjectCardPanel<MoleculeObject> {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectCardPanel.class.getName());

    private final IModel<DatasetMetadata> datasetMetadataModel;
    private final Model<String> highlighterModel;

    @Inject
    private StructureIOClient client;


    MoleculeObjectCardPanel(String id, Map<String, Class> classMappings, MoleculeObject mo, IModel<DatasetMetadata> datasetMetadataModel, Model<String> highlighterModel) {
        super(id, classMappings, mo);
        this.datasetMetadataModel = datasetMetadataModel;
        this.highlighterModel = highlighterModel;
        CdiContainer.get().getNonContextualManager().postConstruct(this);
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
                DepictionParameters params = new DepictionParameters(30, 20);
                params.setMargin(0.5);
                String highlighterFieldName = highlighterModel.getObject();
                if (highlighterFieldName != null && !DatasetResultsPanel.HIGHLIGHTER_NONE.equals(highlighterFieldName)) {
                    MoleculeObjectHighlightable highlightable = (MoleculeObjectHighlightable)mo.getValue(highlighterFieldName);
                    Scale scale = (Scale)datasetMetadataModel.getObject().getFieldMetaProp(highlighterFieldName, DatasetMetadata.PROP_SCALE);
                    if (highlightable != null && scale != null) {
                        highlightable.highlight(params, scale.getFromColor(), scale.getToColor(),
                                scale.getFromValue(), scale.getToValue(),
                                DepictionParameters.HighlightMode.region, false);
                    }
                }
                return client.renderImage(mo.getSource(), mo.getFormat(), DepictionParameters.OutputFormat.svg, params);
            }
        });


        add(image);
    }

}
