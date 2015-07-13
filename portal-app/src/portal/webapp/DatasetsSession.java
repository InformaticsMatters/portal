package portal.webapp;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.client.DatasetClient;
import portal.dataset.IDatasetDescriptor;

import javax.enterprise.context.SessionScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * @author simetrias
 */
@SessionScoped
public class DatasetsSession implements Serializable {

    private DatasetClient datasetClient;

    public DatasetsSession() {
        this.datasetClient = new DatasetClient();
    }

    public void testGetAll() {
        try {
            Stream<DataItem> all = datasetClient.getAll();
            all.forEach(dataItem1 -> System.out.println(dataItem1.getName()));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IDatasetDescriptor createDatasetDescriptorFromDataItem(DataItem dataItem) {
        return null;
    }
}
