package portal.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import portal.integration.DatamartSession;
import portal.service.api.DatasetDescriptor;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class DatasetsPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "service";

    private ListView<DatasetDescriptor> listView;
    @Inject
    private DatamartSession datamartSession;

    public DatasetsPanel(String id) {
        super(id);
        addDatasets();
    }

    private void addDatasets() {
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
                listItem.add(new AttributeModifier("drop-data", datasetDescriptor.getId().toString()));
            }
        };
        add(listView);
    }
}
