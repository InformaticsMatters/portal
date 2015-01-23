package portal.webapp;

import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import portal.service.api.DatasetDescriptor;
import portal.service.api.PropertyDescriptor;
import portal.service.api.Row;


public class TreeGridVisualizerTreeColumn extends BaseTreeColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String> {

    private DatasetDescriptor datasetDescriptor;
    private Long propertyId;

    public TreeGridVisualizerTreeColumn(String columnId, IModel<String> headerModel, DatasetDescriptor datasetDescriptor, Long propertyId) {
        super(columnId, headerModel);
        this.datasetDescriptor = datasetDescriptor;
        this.propertyId = propertyId;
        setInitialSize(DynamicStructureImageResource.RECTANGLE.width + 36);
    }

    @Override
    protected Component newNodeComponent(String id, IModel<TreeGridVisualizerNode> model) {
        Row row = model.getObject().getUserObject();
        if (isStructureProperty(propertyId)) {
            return new TreeGridVisualizerStructurePanel(id, datasetDescriptor.getId(), row.getId());
        } else {
            Object value = row.getProperty(row.getDescriptor().findPropertyDescriptorById(propertyId));
            return new Label(id, value.toString());
        }
    }

    @Override
    protected Icon getIcon(IModel<TreeGridVisualizerNode> defaultMutableTreeNodeIModel) {
        return null;
    }

    private boolean isStructureProperty(Long propertyId) {
        return propertyId == PropertyDescriptor.STRUCTURE_PROPERTY_ID;
    }

}
