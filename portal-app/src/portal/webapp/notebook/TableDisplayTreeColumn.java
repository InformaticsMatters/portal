package portal.webapp.notebook;

import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import portal.dataset.IDatasetDescriptor;
import portal.dataset.IPropertyDescriptor;
import portal.dataset.IRow;
import portal.dataset.IRowDescriptor;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


public class TableDisplayTreeColumn extends BaseTreeColumn<DefaultTreeModel, DefaultMutableTreeNode, String> {

    private IDatasetDescriptor datasetDescriptor;

    public TableDisplayTreeColumn(String columnId, IModel<String> headerModel, IDatasetDescriptor datasetDescriptor) {
        super(columnId, headerModel);
        this.datasetDescriptor = datasetDescriptor;
        setInitialSize(NotebookStructureImageResource.RECTANGLE.width + 36);
    }

    @Override
    protected Component newNodeComponent(String id, IModel<DefaultMutableTreeNode> model) {
        IRow row = (IRow) model.getObject().getUserObject();
        IRowDescriptor rowDescriptor = row.getDescriptor();
        IPropertyDescriptor hierarchicalPropertyDescriptor = rowDescriptor.getHierarchicalPropertyDescriptor();
        IPropertyDescriptor structurePropertyDescriptor = rowDescriptor.getStructurePropertyDescriptor();
        if (hierarchicalPropertyDescriptor == null) {
            throw new RuntimeException("No hierarchical property found. Can't render a TreeColumn.");
        }

        boolean rowHasStructureProperty = structurePropertyDescriptor != null;

        if (rowHasStructureProperty && hierarchicalPropertyDescriptor.getId().equals(structurePropertyDescriptor.getId())) {
            return new TableDisplayStructurePanel(id, datasetDescriptor.getId(), row.getUuid());
        } else {
            Object value = row.getProperty(hierarchicalPropertyDescriptor);
            return new Label(id, value.toString());
        }
    }

    @Override
    protected Icon getIcon(IModel<DefaultMutableTreeNode> defaultMutableTreeNodeIModel) {
        return null;
    }
}
