package portal.notebook;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;

public class NotebookStructureImage extends WebComponent {

    private String rowId;
    private String resourceName;
    private String datasetId;

    public NotebookStructureImage(String id, String rowId, String datasetId, String resourceName) {
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
        pageParameters.add(NotebookStructureImageResource.PARAM_DATASET, datasetId);
        pageParameters.add(NotebookStructureImageResource.PARAM_ROW, rowId);
        CharSequence url = RequestCycle.get().urlFor(resource, pageParameters);
        if (rowId != null && rowId.trim().length() > 0) {
            tag.put("src", url);
            String style = "width: :widthpx; height: :heightpx;"
                    .replace(":width", Integer.toString(NotebookStructureImageResource.RECTANGLE.width))
                    .replace(":height", Integer.toString(NotebookStructureImageResource.RECTANGLE.height));
            tag.put("style", style);
        }
    }
}

