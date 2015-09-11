package portal.webapp.workflow;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.dataset.IDatasetDescriptor;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author simetrias
 */
public class DatasetsPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "dataset";

    private Form<SearchDatasetData> searchForm;
    private WebMarkupContainer datasetsContainer;

    private ListView<IDatasetDescriptor> listView;
    @Inject
    private DatasetsSession datasetsSession;
    private UploadModalPanel uploadModalPanel;
    private DatasetPopupPanel datasetPopupPanel;

    public DatasetsPanel(String id) {
        super(id);
        addSearchForm();
        addDatasets();
        addUploadSupport();
        refreshDatasets();
    }

    private void addSearchForm() {
        searchForm = new Form<>("form");
        searchForm.setModel(new CompoundPropertyModel<>(new SearchDatasetData()));
        searchForm.setOutputMarkupId(true);
        add(searchForm);

        TextField<String> patternField = new TextField<>("pattern");
        searchForm.add(patternField);

        AjaxSubmitLink searchAction = new IndicatingAjaxSubmitLink("search") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                refreshDatasets();
            }
        };
        searchForm.add(searchAction);

        AjaxLink uploadAction = new AjaxLink("addFromFile") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadModalPanel.showModal();
            }
        };
        searchForm.add(uploadAction);
    }

    private void addDatasets() {
        datasetsContainer = new WebMarkupContainer("datasetsContainer");
        datasetsContainer.setOutputMarkupId(true);

        listView = new ListView<IDatasetDescriptor>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<IDatasetDescriptor> listItem) {
                IDatasetDescriptor datasetDescriptor = listItem.getModelObject();
                listItem.add(new DatasetPanel("dataset", datasetDescriptor, DatasetsPanel.this::refreshDatasets));
                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_ID, datasetDescriptor.getId().toString()));
            }
        };
        datasetsContainer.add(listView);

        add(datasetsContainer);
    }

    private void addUploadSupport() {
        uploadModalPanel = new UploadModalPanel("uploadFilePanel", "modalElement");
        uploadModalPanel.setCallbacks(new UploadModalPanel.Callbacks() {

            @Override
            public void onSubmit(String name, InputStream inputStream) {
                datasetsSession.createDataset(name, inputStream);
                refreshDatasets();
            }

            @Override
            public void onCancel() {
            }
        });
        add(uploadModalPanel);
    }

    public void refreshDatasets() {
        DatasetFilterData datasetFilterData = new DatasetFilterData();
        SearchDatasetData searchDatasetData = searchForm.getModelObject();
        datasetFilterData.setPattern(searchDatasetData.getPattern());
        listView.setList(datasetsSession.listDatasetDescriptors(datasetFilterData));
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(datasetsContainer);
        }
    }
}
