package portal.webapp;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.client.DatasetClient;
import portal.dataset.DatasetDescriptor;
import portal.dataset.IDatasetDescriptor;

import javax.enterprise.context.SessionScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author simetrias
 */
@SessionScoped
public class DatasetsSession implements Serializable {

    private DatasetClient datasetClient;
    private Map<Long, IDatasetDescriptor> datasetDescriptorMap;

    public DatasetsSession() {
        this.datasetClient = new DatasetClient();
    }

    private void loadDatasetList() {
        try {
            Stream<DataItem> all = datasetClient.getAll();
            datasetDescriptorMap = new HashMap<>();
            all.forEach(dataItem -> {
                IDatasetDescriptor datasetDescriptor = newDatasetDescriptorFromDataItem(dataItem);
                datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptor);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IDatasetDescriptor newDatasetDescriptorFromDataItem(DataItem dataItem) {
        DatasetDescriptor datasetDescriptor = new DatasetDescriptor(dataItem);
        return datasetDescriptor;
    }

    public List<IDatasetDescriptor> listDatasets(DatasetFilterData datasetFilterData) {
        if (datasetFilterData != null) {
            System.out.println("Searching " + datasetFilterData.getPattern());
        }

        if (datasetDescriptorMap == null) {
            loadDatasetList();
        }

        return new ArrayList<>(datasetDescriptorMap.values());
    }
}
