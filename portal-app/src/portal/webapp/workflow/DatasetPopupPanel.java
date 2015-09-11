package portal.webapp.workflow;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.DatasetDescriptor;
import portal.dataset.IDatasetDescriptor;
import portal.webapp.visualizers.TreeGridVisualizerPage;

/**
 * @author simetrias
 */
public class DatasetPopupPanel extends Panel {

    private DatasetDescriptor datasetDescriptor;

    public DatasetPopupPanel(String id, IDatasetDescriptor descriptor) {
        super(id);
        this.datasetDescriptor = (DatasetDescriptor) descriptor;
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        add(new Label("description", this.datasetDescriptor.getDescription()));
        add(new Label("rowCount", this.datasetDescriptor.getRowCount()));
        add(new Label("owner", this.datasetDescriptor.getDataItem().getOwnerUsername()));
        add(new Label("created", this.datasetDescriptor.getDataItem().getCreated()));
        add(new Label("updated", this.datasetDescriptor.getDataItem().getUpdated()));

        add(new Link("open") {

            @Override
            public void onClick() {
                TreeGridVisualizerPage page = new TreeGridVisualizerPage(descriptor);
                setResponsePage(page);
            }
        });


    }
}
