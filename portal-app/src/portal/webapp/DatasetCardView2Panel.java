package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import portal.service.api.DatasetDescriptor;

import java.util.ArrayList;
import java.util.List;

public class DatasetCardView2Panel extends Panel {

    private List<DatasetDescriptor> datasetDescriptorList;
    private ListView<DatasetDescriptor> listView;

    public DatasetCardView2Panel(String id) {
        super(id);
        datasetDescriptorList = new ArrayList<>();
        addCards();
    }

    private void addCards() {
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
            }
        };
        add(listView);
    }

    public void setDatasetDescriptorList(List<DatasetDescriptor> datasetDescriptorList) {
        this.datasetDescriptorList = datasetDescriptorList;
        listView.setList(datasetDescriptorList);
    }
}

