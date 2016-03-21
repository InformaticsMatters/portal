package portal.visualizers;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import portal.DynamicStructureImageResource;

public class ExternalStructureImage extends WebComponent {

    private String rowId;
    private String resourceName;
    private String datasetId;

    public ExternalStructureImage(String id, String rowId, String datasetId, String resourceName) {
        super(id);
        this.rowId = rowId;
        this.resourceName = resourceName;
        this.datasetId = datasetId;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        ResourceReference resource = new SharedResourceReference(resourceName);
        PageParameters pageParameters = new PageParameters();
        pageParameters.add(DynamicStructureImageResource.PARAM_DATASET, datasetId);
        pageParameters.add(DynamicStructureImageResource.PARAM_ROW, rowId);
        CharSequence url = RequestCycle.get().urlFor(resource, pageParameters);
        if (rowId != null && rowId.trim().length() > 0) {
            tag.put("src", url);
            String style = "width: :widthpx; height: :heightpx;"
                    .replace(":width", Integer.toString(DynamicStructureImageResource.RECTANGLE.width))
                    .replace(":height", Integer.toString(DynamicStructureImageResource.RECTANGLE.height));
            tag.put("style", style);
        }
    }
}

