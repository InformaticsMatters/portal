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

/**
 * @author simetrias
 */
public class DatasetsPanel extends Panel {

    private List<DatasetDescriptor> datasetDescriptorList;
    private ListView<DatasetDescriptor> listView;

    public DatasetsPanel(String id) {
        super(id);
        addDatasets();
        datasetDescriptorList = new ArrayList<>();
    }

    private void addDatasets() {

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

            /*  List<DatasetData> datasetDataList = new ArrayList<DatasetData>();
        datasetDataList.add(new DatasetData());
        datasetDataList.add(new DatasetData());
        datasetDataList.add(new DatasetData());

        ListView<DatasetData> datasets = new ListView<DatasetData>("datasets", datasetDataList) {

            @Override
            protected void populateItem(ListItem<DatasetData> components) {
                components.add(new WebMarkupContainer("dataset"));
            }
        };
        add(datasets);   */
    }


    public void setDatasetDescriptorList(List<DatasetDescriptor> datasetDescriptorList) {
        this.datasetDescriptorList = datasetDescriptorList;
        listView.setList(datasetDescriptorList);
    }

   /* class DatasetData implements Serializable {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }  */
}
