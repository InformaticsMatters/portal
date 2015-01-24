package portal.webapp;

import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import portal.service.api.DatasetDescriptor;
import portal.service.api.PropertyDescriptor;
import portal.service.api.Row;
import portal.service.api.RowDescriptor;


public class TreeGridVisualizerTreeColumn extends BaseTreeColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String> {

    private DatasetDescriptor datasetDescriptor;

    public TreeGridVisualizerTreeColumn(String columnId, IModel<String> headerModel, DatasetDescriptor datasetDescriptor) {
        super(columnId, headerModel);
        this.datasetDescriptor = datasetDescriptor;
        setInitialSize(DynamicStructureImageResource.RECTANGLE.width + 36);
    }

    @Override
    protected Component newNodeComponent(String id, IModel<TreeGridVisualizerNode> model) {
        Row row = model.getObject().getUserObject();
        RowDescriptor rowDescriptor = row.getDescriptor();
        PropertyDescriptor hierarchicalPropertyDescriptor = rowDescriptor.getHierarchicalPropertyDescriptor();
        PropertyDescriptor structurePropertyDescriptor = rowDescriptor.getStructurePropertyDescriptor();
        if (hierarchicalPropertyDescriptor.getId() == structurePropertyDescriptor.getId()) {
            return new TreeGridVisualizerStructurePanel(id, datasetDescriptor.getId(), row.getId());
        } else {
            Object value = row.getProperty(hierarchicalPropertyDescriptor);
            return new Label(id, value.toString());
        }
    }

    @Override
    protected Icon getIcon(IModel<TreeGridVisualizerNode> defaultMutableTreeNodeIModel) {
        return null;
    }

}
