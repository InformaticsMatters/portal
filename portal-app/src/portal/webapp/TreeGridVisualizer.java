package portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.treegrid.TreeGrid;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.model.Model;
import portal.datamart.DatamartSession;
import portal.service.api.*;

import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public class TreeGridVisualizer extends TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String> implements IPageable {

    private static final int ROWS_PER_PAGE = 50;
    private long currentPage = 0;
    private DatasetDescriptor datasetDescriptor;
    private List<Long> allIds;
    @Inject
    private DatamartSession datamartSession;

    public TreeGridVisualizer(String id, DatasetDescriptor datasetDescriptor) {
        super(id, new DefaultTreeModel(new DefaultMutableTreeNode()), buildColumns(datasetDescriptor));
        setOutputMarkupId(true);
        getTree().setRootLess(true);
        this.datasetDescriptor = datasetDescriptor;
        // allIds = datasetService.listAllRowIds(datasetDescriptor.getId());
        allIds = datamartSession.listAllRowIds(datasetDescriptor.getId());
        setCurrentPage(0);
    }

    private static List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> buildColumns(DatasetDescriptor datasetDescriptor) {
        List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> columns = new ArrayList<>();
        TreeGridVisualizerTreeColumn treeColumn = new TreeGridVisualizerTreeColumn("treeColumnId", Model.of("Structure"), datasetDescriptor);
        columns.add(treeColumn);
        for (RowDescriptor rowDescriptor : datasetDescriptor.getAllRowDescriptors()) {
            for (PropertyDescriptor propertyDescriptor : rowDescriptor.listAllPropertyDescriptors()) {
                if (!isStructureProperty(rowDescriptor, propertyDescriptor) && !isHierarchicalProperty(rowDescriptor, propertyDescriptor)) {
                    Long propertyId = propertyDescriptor.getId();
                    String columnId = propertyId.toString();
                    Model<String> headerModel = Model.of(propertyDescriptor.getDescription());
                    columns.add(new TreeGridVisualizerPropertyColumn(columnId, headerModel, propertyId));
                }
            }
        }
        return columns;
    }

    private static boolean isStructureProperty(RowDescriptor rowDescriptor, PropertyDescriptor propertyDescriptor) {
        PropertyDescriptor structurePropertyDescriptor = rowDescriptor.getStructurePropertyDescriptor();
        return structurePropertyDescriptor != null && propertyDescriptor.getId().equals(structurePropertyDescriptor.getId());
    }

    private static boolean isHierarchicalProperty(RowDescriptor rowDescriptor, PropertyDescriptor propertyDescriptor) {
        PropertyDescriptor hierarchicalPropertyDescriptor = rowDescriptor.getHierarchicalPropertyDescriptor();
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
        if (end > allIds.size()) {
            end = allIds.size();
        }
        List<Long> rowIdList = allIds.subList(start, end);
        ListRowFilter listRowFilter = new ListRowFilter();
        listRowFilter.setRowIdList(rowIdList);
        listRowFilter.setDatasetDescriptorId(datasetDescriptor.getId());

        // List<Row> rowList = datasetService.listRow(listRowFilter);
        List<Row> rowList = datamartSession.listRow(datasetDescriptor.getId(), rowIdList);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        buildNodeHierarchy(rootNode, rowList);
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(rootNode);
        setDefaultModelObject(defaultTreeModel);
    }

    @Override
    public long getPageCount() {
        return (allIds.size() / ROWS_PER_PAGE) + 1;
    }

    private void buildNodeHierarchy(DefaultMutableTreeNode parentNode, List<Row> rowList) {
        for (Row row : rowList) {
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
