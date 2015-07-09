package portal.webapp;

import portal.dataset.DatasetDescriptor;

/**
 * @author simetrias
 */
public class DatasetCanvasItemData extends AbstractCanvasItemData {

    private DatasetDescriptor datasetDescriptor;

    public DatasetDescriptor getDatasetDescriptor() {
        return datasetDescriptor;
    }

    public void setDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        this.datasetDescriptor = datasetDescriptor;
    }
}
