package portal.webapp;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.client.DatasetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.dataset.DatasetDescriptor;
import portal.dataset.IDatasetDescriptor;

import javax.enterprise.context.SessionScoped;
import java.io.IOException;
import java.io.InputStream;
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

    private static final Logger logger = LoggerFactory.getLogger(DatasetsSession.class.getName());

    private DatasetClient datasetClient;
    private Map<Long, DatasetDescriptor> datasetMap;

    public DatasetsSession() {
        this.datasetClient = new DatasetClient();
    }

    public List<IDatasetDescriptor> listDatasetDescriptors(DatasetFilterData datasetFilterData) {
        datasetMap = new HashMap<>();
        try {
            Stream<DataItem> all = datasetClient.getAll();
            all.forEach(dataItem -> {
                DatasetDescriptor datasetDescriptor = new DatasetDescriptor(dataItem);
                datasetMap.put(datasetDescriptor.getId(), datasetDescriptor);
            });
        } catch (IOException e) {
            logger.error(null, e);
        }
        return new ArrayList<>(datasetMap.values());
    }

    public void createDataset(String name, InputStream content) {
        try {
            datasetClient.create(name, content);
        } catch (IOException e) {
            logger.error(null, e);
        }
    }

    public void deleteDataset(IDatasetDescriptor datasetDescriptor) {
        try {
            DatasetDescriptor dataset = (DatasetDescriptor) datasetDescriptor;
            datasetClient.delete(dataset.getDataItem().getId());
        } catch (IOException e) {
            logger.error(null, e);
        }
    }

    public IDatasetDescriptor findDatasetDescriptorById(Long id) {
        return datasetMap.get(id);
    }
}
