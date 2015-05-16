package portal.webapp;

import portal.service.api.DatasetDescriptor;

/**
 * @author simetrias
 */
public class DatasetCanvasItemModel extends AbstractCanvasItemModel {

    private DatasetDescriptor datasetDescriptor;

    public DatasetDescriptor getDatasetDescriptor() {
        return datasetDescriptor;
    }

    public void setDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        this.datasetDescriptor = datasetDescriptor;
    }
}
