package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import portal.service.api.DatasetDescriptor;
import portal.service.api.DatasetService;
import portal.service.api.ListDatasetDescriptorFilter;
import toolkit.wicket.inmethod.EasyGrid;
import toolkit.wicket.inmethod.EasyGridBuilder;
import toolkit.wicket.inmethod.EasyListDataSource;
import toolkit.wicket.inmethod.RowActionsCallbackHandler;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class DatasetGridViewPanel extends Panel {

    private static final String LARGE_ARROW_CIRCLE_OUTLINE_RIGHT_LINK_ICON = "large arrow circle outline right link icon";
    @Inject
    private DatasetService service;
    private UploadModalPanel uploadModalPanel;
    private EasyGrid<DatasetDescriptor> grid;
    private List<DatasetDescriptor> descriptorList;

    public DatasetGridViewPanel(String id) {
        super(id);
        addModals();
        addDatasetDescriptorGrid();
    }

    private void addModals() {
        uploadModalPanel = new UploadModalPanel("uploadFilePanel", "modalElement");
        uploadModalPanel.setCallbacks(new UploadModalPanel.Callbacks() {

            @Override
            public void onSubmit() {
                refreshDatasetDescriptorsGrid();
            }

            @Override
            public void onCancel() {
                uploadModalPanel.hideModal();
            }
        });
        add(uploadModalPanel);
    }

    private void addDatasetDescriptorGrid() {
        descriptorList = service.listDatasetDescriptor(new ListDatasetDescriptorFilter());
        EasyGridBuilder<DatasetDescriptor> easyGridBuilder = new EasyGridBuilder<DatasetDescriptor>("datasetDescriptors");
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("Id", "id", "id"));
        easyGridBuilder.getColumnList().add(easyGridBuilder.newPropertyColumn("Description", "description", "description"));
        List<String> actionNameList = Arrays.asList(LARGE_ARROW_CIRCLE_OUTLINE_RIGHT_LINK_ICON);
        easyGridBuilder.getColumnList().add(easyGridBuilder.newActionsColumn(actionNameList, new RowActionsCallbackHandler<DatasetDescriptor>() {

            @Override
            public void onAction(AjaxRequestTarget target, String name, DatasetDescriptor datasetDescriptor) {
                if (LARGE_ARROW_CIRCLE_OUTLINE_RIGHT_LINK_ICON.equals(name)) {
                    TreeGridVisualizerPage page = new TreeGridVisualizerPage(datasetDescriptor);
                    setResponsePage(page);
                }
            }
        }).setInitialSize(70));
        grid = easyGridBuilder.build(new EasyListDataSource<DatasetDescriptor>(DatasetDescriptor.class) {

            @Override
            public List<DatasetDescriptor> loadData() {
                return descriptorList;
            }
        });
        add(grid);
        addDatasetDescriptorGridActions();
    }

    private void refreshDatasetDescriptorsGrid() {
        descriptorList = service.listDatasetDescriptor(new ListDatasetDescriptorFilter());
        grid.resetData();
        getRequestCycle().find(AjaxRequestTarget.class).add(grid);
    }

    private void addDatasetDescriptorGridActions() {
        add(new AjaxLink("addFromSDF") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadModalPanel.showModal();
            }
        });
    }
}
