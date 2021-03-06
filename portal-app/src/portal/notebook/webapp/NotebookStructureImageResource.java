package portal.notebook.webapp;


import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.squonk.io.DepictionParameters;
import org.squonk.options.types.Structure;

import javax.inject.Inject;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NotebookStructureImageResource extends DynamicImageResource {

    public static final Rectangle RECTANGLE = new Rectangle(130, 100);
    public static final String PARAM_DATASET = "dataset";
    public static final String PARAM_ROW = "row";

    @Inject
    private NotebookSession notebookSession;

    public NotebookStructureImageResource() {
        super("svg");
        CdiContainer.get().getNonContextualManager().postConstruct(this);
    }

//    @Override
//    protected void setResponseHeaders(ResourceResponse response, Attributes attributes) {
//        // this disables some unwanted default caching
//    }

    @Override
    protected void configureResponse(ResourceResponse response, Attributes attributes) {
        super.configureResponse(response, attributes);
        response.setContentType("image/svg+xml");
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {

        String datasetIdAsString = attributes.getParameters().get(PARAM_DATASET).toString();
        String rowIdAsString = attributes.getParameters().get(PARAM_ROW).toString();
        Structure struct = loadStructureData(datasetIdAsString, rowIdAsString);
        DepictionParameters params = new DepictionParameters((int)(RECTANGLE.getWidth() / 5.0d), (int)(RECTANGLE.getHeight() / 5.0d));
        params.setExpandToFit(true);
        params.setMargin(0.5);
        String svg;
        if (struct == null) {
            svg = notebookSession.getStructureIOClient().renderErrorSVG(params, "No structure");
        } else {
            svg = notebookSession.getStructureIOClient().renderSVG(struct.getSource(), struct.getFormat(), params);
        }
        return svg.getBytes();
    }

    protected Structure loadStructureData(String datasetIdAsString, String rowIdAsString) {
        Structure structure = null;
        Long datasetDescriptorId = Long.valueOf(datasetIdAsString);
        UUID rowId = UUID.fromString(rowIdAsString);

        IDatasetDescriptor dataset = notebookSession.findDatasetDescriptorById(datasetDescriptorId);
        List<IRow> rows = notebookSession.listDatasetRow(dataset, Collections.singletonList(rowId));
        IRow row = rows.get(0);

        if (row != null) {
            IPropertyDescriptor propertyDescriptor = row.getDescriptor().getStructurePropertyDescriptor();
            structure = (Structure) row.getProperty(propertyDescriptor);
        }
        return structure;
    }

    protected Rectangle getRectangle() {
        return RECTANGLE;
    }

}

