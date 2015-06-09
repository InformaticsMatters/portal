package portal.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.integration.DatasetSession;
import portal.service.api.DatasetDescriptor;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author simetrias
 */
public class DatasetsPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "dataset";

    private Form<SearchDatasetData> searchDatasetForm;
    private WebMarkupContainer datasetsContainer;

    private ListView<DatasetDescriptor> listView;
    @Inject
    private DatasetSession datasetSession;

    public DatasetsPanel(String id) {
        super(id);
        addDatasets();
        addForm();
    }

    private void addForm() {
        searchDatasetForm = new Form<>("form");
        searchDatasetForm.setModel(new CompoundPropertyModel<>(new SearchDatasetData()));
        searchDatasetForm.setOutputMarkupId(true);
        add(searchDatasetForm);

        TextField<String> patternField = new TextField<>("pattern");
        searchDatasetForm.add(patternField);

        AjaxSubmitLink searchAction = new IndicatingAjaxSubmitLink("search") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                DatasetFilterData datasetFilterData = new DatasetFilterData();
                SearchDatasetData searchDatasetData = searchDatasetForm.getModelObject();
                datasetFilterData.setPattern(searchDatasetData.getPattern());
                listView.setList(datasetSession.listDatasets(datasetFilterData));
                getRequestCycle().find(AjaxRequestTarget.class).add(datasetsContainer);
            }
        };
        searchDatasetForm.add(searchAction);

    }

    private void addDatasets() {
        datasetsContainer = new WebMarkupContainer("datasetsContainer");
        datasetsContainer.setOutputMarkupId(true);

        listView = new ListView<DatasetDescriptor>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<DatasetDescriptor> listItem) {
                DatasetDescriptor datasetDescriptor = listItem.getModelObject();
                listItem.add(new Label("description", datasetDescriptor.getDescription()));
                listItem.add(new Label("rowCount", datasetDescriptor.getRowCount()));
                listItem.add(new IndicatingAjaxLink("open") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        TreeGridVisualizerPage page = new TreeGridVisualizerPage(datasetDescriptor);
                        setResponsePage(page);
                    }
                });

                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(WorkflowPage.DROP_DATA_ID, datasetDescriptor.getId().toString()));
            }
        };
        datasetsContainer.add(listView);

        add(datasetsContainer);
    }
}
