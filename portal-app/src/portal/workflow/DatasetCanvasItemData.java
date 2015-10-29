package portal.workflow;

import portal.dataset.IDatasetDescriptor;

/**
 * @author simetrias
 */
public class DatasetCanvasItemData extends AbstractCanvasItemData {

    private IDatasetDescriptor datasetDescriptor;

    public IDatasetDescriptor getDatasetDescriptor() {
        return datasetDescriptor;
    }

    public void setDatasetDescriptor(IDatasetDescriptor datasetDescriptor) {
        this.datasetDescriptor = datasetDescriptor;
    }
}
