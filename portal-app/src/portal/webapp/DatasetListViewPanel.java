package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import portal.service.api.DatasetDescriptor;
import toolkit.wicket.inmethod.EasyGrid;
import toolkit.wicket.inmethod.EasyGridBuilder;
import toolkit.wicket.inmethod.EasyListDataSource;
import toolkit.wicket.inmethod.RowActionsCallbackHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatasetListViewPanel extends Panel {

    private static final String LARGE_FILE_TEXT_OUTLINE_LINK_ICON = "large file text outline link icon";
    private static final String LARGE_COUNTERCLOCKWISE_ROTATED_SITEMAP_LINK_ICON = "large counterclockwise rotated sitemap link icon";
    private EasyGrid<DatasetDescriptor> grid;
    private List<DatasetDescriptor> datasetDescriptorList;

    public DatasetListViewPanel(String id) {
        super(id);
        datasetDescriptorList = new ArrayList<>();
        addDatasetDescriptorGrid();
    }

    private void addDatasetDescriptorGrid() {
        EasyGridBuilder<DatasetDescriptor> easyGridBuilder = new EasyGridBuilder<DatasetDescriptor>("datasetDescriptors");
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("Id", "id", "id"));
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("Description", "description", "description").setInitialSize(400));
        List<String> actionNameList = Arrays.asList(LARGE_COUNTERCLOCKWISE_ROTATED_SITEMAP_LINK_ICON, LARGE_FILE_TEXT_OUTLINE_LINK_ICON);
        easyGridBuilder.getColumnList().add(easyGridBuilder.newActionsColumn(actionNameList, new RowActionsCallbackHandler<DatasetDescriptor>() {

            @Override
            public void onAction(AjaxRequestTarget target, String name, DatasetDescriptor datasetDescriptor) {
                if (LARGE_COUNTERCLOCKWISE_ROTATED_SITEMAP_LINK_ICON.equals(name)) {
                    TreeGridVisualizerPage page = new TreeGridVisualizerPage(datasetDescriptor);
                    setResponsePage(page);
                } else if (LARGE_FILE_TEXT_OUTLINE_LINK_ICON.equals(name)) {
                    // show metadata
                }
            }
        }).setInitialSize(70));
        grid = easyGridBuilder.build(new EasyListDataSource<DatasetDescriptor>(DatasetDescriptor.class) {

            @Override
            public List<DatasetDescriptor> loadData() {
                return datasetDescriptorList;
            }
        });
        add(grid);
    }

    public void setDatasetDescriptorList(List<DatasetDescriptor> datasetDescriptorList) {
        this.datasetDescriptorList = datasetDescriptorList;
        grid.resetData();
    }
}
