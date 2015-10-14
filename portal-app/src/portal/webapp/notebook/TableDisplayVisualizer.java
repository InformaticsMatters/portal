package portal.webapp.notebook;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.treegrid.TreeGrid;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.model.Model;
import portal.dataset.IDatasetDescriptor;
import portal.dataset.IPropertyDescriptor;
import portal.dataset.IRow;
import portal.dataset.IRowDescriptor;

import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TableDisplayVisualizer extends TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String> implements IPageable {

    private static final int ROWS_PER_PAGE = 50;
    private long currentPage = 0;
    private IDatasetDescriptor datasetDescriptor;
    private List<UUID> allUuids;

    @Inject
    private NotebooksSession notebooksSession;

    public TableDisplayVisualizer(String id, IDatasetDescriptor datasetDescriptor) {
        super(id, new DefaultTreeModel(new DefaultMutableTreeNode()), buildColumns(datasetDescriptor));
        setOutputMarkupId(true);
        getTree().setRootLess(true);
        this.datasetDescriptor = datasetDescriptor;
        allUuids = notebooksSession.listAllUuids(datasetDescriptor);
        setCurrentPage(0);
    }

    private static List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> buildColumns(IDatasetDescriptor datasetDescriptor) {
        List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> columns = new ArrayList<>();
        TableDisplayTreeColumn treeColumn = new TableDisplayTreeColumn("treeColumnId", Model.of("Structure"), datasetDescriptor);
        columns.add(treeColumn);
        for (IRowDescriptor rowDescriptor : datasetDescriptor.getAllRowDescriptors()) {
            for (IPropertyDescriptor propertyDescriptor : rowDescriptor.listAllPropertyDescriptors()) {
                if (!isStructureProperty(rowDescriptor, propertyDescriptor) && !isHierarchicalProperty(rowDescriptor, propertyDescriptor)) {
                    Long propertyId = propertyDescriptor.getId();
                    String columnId = propertyId.toString();
                    Model<String> headerModel = Model.of(propertyDescriptor.getDescription());
                    columns.add(new TableDisplayPropertyColumn(columnId, headerModel, propertyId));
                }
            }
        }
        return columns;
    }

    private static boolean isStructureProperty(IRowDescriptor rowDescriptor, IPropertyDescriptor propertyDescriptor) {
        IPropertyDescriptor structurePropertyDescriptor = rowDescriptor.getStructurePropertyDescriptor();
        return structurePropertyDescriptor != null && propertyDescriptor.getId().equals(structurePropertyDescriptor.getId());
    }

    private static boolean isHierarchicalProperty(IRowDescriptor rowDescriptor, IPropertyDescriptor propertyDescriptor) {
        IPropertyDescriptor hierarchicalPropertyDescriptor = rowDescriptor.getHierarchicalPropertyDescriptor();
        return hierarchicalPropertyDescriptor != null && propertyDescriptor.getId().equals(hierarchicalPropertyDescriptor.getId());
    }

    @Override
    public long getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
        int start = (int) (currentPage * ROWS_PER_PAGE);
        int end = start + ROWS_PER_PAGE;
        if (end > allUuids.size()) {
            end = allUuids.size();
        }

        List<UUID> uuidList = allUuids.subList(start, end);
        List<IRow> rowList = notebooksSession.listRow(datasetDescriptor, uuidList);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        buildNodeHierarchy(rootNode, rowList);
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(rootNode);
        setDefaultModelObject(defaultTreeModel);
    }

    @Override
    public long getPageCount() {
        int size = allUuids.size();
        long result = allUuids.size() / ROWS_PER_PAGE;
        if ((size % ROWS_PER_PAGE) > 0) {
            result++;
        }
        return result;
    }

    private void buildNodeHierarchy(DefaultMutableTreeNode parentNode, List<IRow> rowList) {
        for (IRow row : rowList) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode();
            childNode.setUserObject(row);
            parentNode.add(childNode);
            if (row.getChildren() != null && row.getChildren().size() > 0) {
                buildNodeHierarchy(childNode, row.getChildren());
            }
        }
    }

    @Override
    protected void onJunctionLinkClicked(AjaxRequestTarget target, Object node) {
        if (getTreeState().isNodeExpanded(node)) {
            getTree().invalidateAll();
        }
    }
}
