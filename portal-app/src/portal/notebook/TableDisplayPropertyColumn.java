package portal.notebook;

import com.inmethod.grid.column.AbstractColumn;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import portal.dataset.IPropertyDescriptor;
import portal.dataset.IRow;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class TableDisplayPropertyColumn extends AbstractColumn<DefaultTreeModel, DefaultMutableTreeNode, String> {

    private Long propertyId;

    public TableDisplayPropertyColumn(String columnId, IModel<String> headerModel, Long propertyId) {
        super(columnId, headerModel);
        this.propertyId = propertyId;
    }

    @Override
    public Component newCell(WebMarkupContainer parent, String componentId, IModel<DefaultMutableTreeNode> rowModel) {
        IRow row = (IRow) rowModel.getObject().getUserObject();
        IPropertyDescriptor propertyDescriptor = row.getDescriptor().findPropertyDescriptorById(propertyId);
        Serializable propertyValue = (Serializable) row.getProperty(propertyDescriptor);
        if (propertyValue == null) {
            propertyValue = "";
        }
        return new Label(componentId, propertyValue);
    }
}
