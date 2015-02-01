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
import toolkit.wicket.marvin4js.MarvinSketcher;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class DatasetGridViewPanel extends Panel {

    private static final String LARGE_FILE_TEXT_OUTLINE_LINK_ICON = "large file text outline link icon";
    private static final String LARGE_COUNTERCLOCKWISE_ROTATED_SITEMAP_LINK_ICON = "large counterclockwise rotated sitemap link icon";
    @Inject
    private DatasetService service;
    private UploadModalPanel uploadModalPanel;
    private MarvinSketcher marvinSketcher;
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

        marvinSketcher = new MarvinSketcher("marvinSketcher", "modalElement");
        marvinSketcher.setCallbackHandler(new MarvinSketcher.CallbackHandler() {

            @Override
            public void onAcceptAction(AjaxRequestTarget ajaxRequestTarget) {
                marvinSketcher.hideModal();
            }

            @Override
            public void onCancelAction(AjaxRequestTarget ajaxRequestTarget) {
                marvinSketcher.hideModal();
            }
        });
        add(marvinSketcher);
    }

    private void addDatasetDescriptorGrid() {
        descriptorList = service.listDatasetDescriptor(new ListDatasetDescriptorFilter());
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
        addActions();
    }

    private void refreshDatasetDescriptorsGrid() {
        descriptorList = service.listDatasetDescriptor(new ListDatasetDescriptorFilter());
        grid.resetData();
        getRequestCycle().find(AjaxRequestTarget.class).add(grid);
    }

    private void addActions() {
        add(new AjaxLink("addFromFile") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadModalPanel.showModal();
            }
        });

        add(new AjaxLink("addFromDatamart") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                marvinSketcher.showModal();
            }
        });
    }
}
