package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetDetailsPanel extends Panel {

    private DatasetMetadataPanel metadataPanel;
    private final CompoundPropertyModel<DatasetMetadata> datasetMetadataModel = new CompoundPropertyModel<>(new DatasetMetadata(BasicObject.class));

    public DatasetDetailsPanel(String id) {
        super(id);
        addContent();

    }

    public DatasetMetadata getDatasetMetadata() {
        return datasetMetadataModel.getObject();
    }

    public void setDatasetMetadata(DatasetMetadata datasetMetadata) {
        datasetMetadataModel.setObject(datasetMetadata);
    }

    private void addContent() {

        metadataPanel = new DatasetMetadataPanel("metadata", datasetMetadataModel);
        add(metadataPanel);
    }
}