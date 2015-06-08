package portal.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.integration.DatamartSession;
import portal.service.api.DatasetDescriptor;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class DatasetsPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "dataset";

    private Form<BusquedaDatasetsData> form;
    private WebMarkupContainer datasetsContainer;

    private ListView<DatasetDescriptor> listView;
    @Inject
    private DatamartSession datamartSession;

    public DatasetsPanel(String id) {
        super(id);
        addDatasets();
        addForm();
    }

    private void addForm() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new BusquedaDatasetsData()));
        form.setOutputMarkupId(true);
        add(form);

        TextField<String> nameField = new TextField<>("name");
        form.add(nameField);

    }

    private void addDatasets() {
        datasetsContainer = new WebMarkupContainer("datasetsContainer");
        datasetsContainer.setOutputMarkupId(true);

        datamartSession.loadDatamartDatasetList();
        listView = new ListView<DatasetDescriptor>("descriptors", datamartSession.getDatasetDescriptorList()) {

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
