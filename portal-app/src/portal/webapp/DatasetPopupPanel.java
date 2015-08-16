package portal.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import portal.dataset.IDatasetDescriptor;

/**
 * @author simetrias
 */
public class DatasetPopupPanel extends Panel {

    private IDatasetDescriptor datasetDescriptor;

    public DatasetPopupPanel(String id, IDatasetDescriptor datasetDescriptor) {
        super(id);
        this.datasetDescriptor = datasetDescriptor;
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
        add(new Label("description", this.datasetDescriptor.getDescription()));
        add(new Label("rowCount", this.datasetDescriptor.getRowCount()));

        add(new Link("open") {

            @Override
            public void onClick() {
                TreeGridVisualizerPage page = new TreeGridVisualizerPage(datasetDescriptor);
                setResponsePage(page);
            }
        });


    }
}
