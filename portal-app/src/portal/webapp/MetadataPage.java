package portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.column.tree.PropertyTreeColumn;
import com.inmethod.grid.treegrid.TreeGrid;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import portal.service.api.*;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public class MetadataPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;
    private TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String> treeGrid;
    private DatasetDescriptor datasetDescriptor;

    public MetadataPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addDescriptorsTreeTable();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(WorkflowPage.class, "resources/lac.css")));
    }

    private void addDescriptorsTreeTable() {
        List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> columns = new ArrayList<>();

        PropertyTreeColumn<DefaultTreeModel, DefaultMutableTreeNode, String, String> treeColumn = new PropertyTreeColumn<>(Model.of("Description"), "userObject.description");
        treeColumn.setInitialSize(200);
        columns.add(treeColumn);

        DefaultTreeModel model = createTreeModel();
        treeGrid = new TreeGrid<>("grid", model, columns);
        treeGrid.getTree().setRootLess(true);

        add(treeGrid);
    }

    private DefaultTreeModel createTreeModel() {
        List<DatasetDescriptor> datasetDescriptorList = service.listDatasetDescriptor(new ListDatasetDescriptorFilter());

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

        for (DatasetDescriptor datasetDescriptor : datasetDescriptorList) {
            createDatasetDescriptorNode(rootNode, datasetDescriptor);
        }

        return new DefaultTreeModel(rootNode);
    }

    private void createDatasetDescriptorNode(DefaultMutableTreeNode rootNode, DatasetDescriptor datasetDescriptor) {

        this.datasetDescriptor = datasetDescriptor; // sacar

        DefaultMutableTreeNode datasetNode = new DefaultMutableTreeNode();
        DescriptorNodeData data = new DescriptorNodeData(datasetDescriptor);
        datasetNode.setUserObject(data);
        rootNode.add(datasetNode);

        List<RowDescriptor> rowDescriptorList = datasetDescriptor.getAllRowDescriptors();
        for (RowDescriptor rowDescriptor : rowDescriptorList) {
            createRowDescriptorNode(datasetNode, rowDescriptor);
        }
    }

    private void createRowDescriptorNode(DefaultMutableTreeNode datasetNode, RowDescriptor rowDescriptor) {
        DefaultMutableTreeNode rowNode = new DefaultMutableTreeNode();
        DescriptorNodeData data = new DescriptorNodeData(rowDescriptor);
        rowNode.setUserObject(data);
        datasetNode.add(rowNode);

        List<PropertyDescriptor> propertyDescriptorList = rowDescriptor.listAllPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptorList) {
            createPropertyDescriptorNode(rowNode, propertyDescriptor);
        }
    }

    private void createPropertyDescriptorNode(DefaultMutableTreeNode rowNode, PropertyDescriptor propertyDescriptor) {
        DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode();
        DescriptorNodeData data = new DescriptorNodeData(propertyDescriptor);
        propertyNode.setUserObject(data);
        rowNode.add(propertyNode);
    }
}
