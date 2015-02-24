package portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.treegrid.TreeGrid;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.model.Model;
import portal.service.api.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TreeGridVisualizer extends TreeGrid<TreeGridVisualizerModel, TreeGridVisualizerNode, String> implements IPageable {

    private static final int ROWS_PER_PAGE = 50;
    private long currentPage = 0;
    private DatasetDescriptor datasetDescriptor;
    private List<Long> allIds;
    @Inject
    private DatasetService datasetService;

    public TreeGridVisualizer(String id, DatasetDescriptor datasetDescriptor) {
        super(id, new TreeGridVisualizerModel(new TreeGridVisualizerNode()), buildColumns(datasetDescriptor));
        setOutputMarkupId(true);
        getTree().setRootLess(true);
        this.datasetDescriptor = datasetDescriptor;
        allIds = datasetService.listAllRowIds(datasetDescriptor.getId());
        setCurrentPage(0);
    }

    private static List<IGridColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String>> buildColumns(DatasetDescriptor datasetDescriptor) {
        List<IGridColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String>> columns = new ArrayList<>();
        TreeGridVisualizerTreeColumn treeColumn = new TreeGridVisualizerTreeColumn("id", Model.of("Structure"), datasetDescriptor);
        columns.add(treeColumn);
        for (RowDescriptor rowDescriptor : datasetDescriptor.getAllRowDescriptors()) {
            for (PropertyDescriptor propertyDescriptor : rowDescriptor.listAllPropertyDescriptors()) {
                if (!isStructureProperty(rowDescriptor, propertyDescriptor)) {
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
        return propertyDescriptor.getId().equals(rowDescriptor.getStructurePropertyDescriptor().getId());
    }

    private void buildNodeHierarchy(TreeGridVisualizerNode rootNode, List<Row> rowList) {
        for (Row row : rowList) {
            TreeGridVisualizerNode childNode = new TreeGridVisualizerNode();
            childNode.setUserObject(row);
            rootNode.add(childNode);
            if (row.getChildren() != null && row.getChildren().size() > 0) {
                buildNodeHierarchy(childNode, row.getChildren());
            }
        }
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
        List<Row> rowList = datasetService.listRow(listRowFilter);
        TreeGridVisualizerNode rootNode = new TreeGridVisualizerNode();
        buildNodeHierarchy(rootNode, rowList);
        TreeGridVisualizerModel treeGridVisualizerModel = new TreeGridVisualizerModel(rootNode);
        getTree().setModelObject(treeGridVisualizerModel);
    }

    @Override
    public long getPageCount() {
        return (allIds.size() / ROWS_PER_PAGE) + 1;
    }


}
