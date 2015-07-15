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
import java.util.List;
import java.util.stream.Stream;

/**
 * @author simetrias
 */
@SessionScoped
public class DatasetsSession implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DatasetsSession.class.getName());

    private DatasetClient datasetClient;

    public DatasetsSession() {
        this.datasetClient = new DatasetClient();
    }

    public List<IDatasetDescriptor> listDatasetDescriptors(DatasetFilterData datasetFilterData) {
        List<IDatasetDescriptor> result = new ArrayList<>();
        try {
            Stream<DataItem> all = datasetClient.getAll();
            all.forEach(dataItem -> {
                result.add(new DatasetDescriptor(dataItem));
            });
        } catch (IOException e) {
            logger.error(null, e);
        }
        return result;
    }

    public void createDataset(String name, InputStream content) {
        try {
            datasetClient.create(name, content);
        } catch (IOException e) {
            logger.error(null, e);
        }
    }
}
