package portal.notebook.webapp;

import com.im.lac.dataset.DataItem;
import com.im.lac.dataset.client.DatasetClient;
import com.im.lac.types.MoleculeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author simetrias
 */
@SessionScoped
public class DatasetsSession implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DatasetsSession.class.getName());
    private Map<Long, Map<UUID, MoleculeObject>> datasetsContentsMap = new HashMap<>();

    @Inject
    private SessionContext sessionContext;

    private DatasetClient datasetClient;
    private Map<Long, DatasetDescriptor> datasetMap;

    public DatasetsSession() {
        this.datasetClient = new DatasetClient();
    }

    public List<IDatasetDescriptor> listDatasetDescriptors(DatasetFilterData datasetFilterData) {
        datasetMap = new HashMap<>();
        try {
            Stream<DataItem> all = datasetClient.getAll(sessionContext.getLoggedInUserDetails().getUserid());
            all.forEach(dataItem -> {
                DatasetDescriptor datasetDescriptor = new DatasetDescriptor(dataItem);

                // configure metadata
                RowDescriptor rowDescriptor = new RowDescriptor();
                datasetDescriptor.addRowDescriptor(rowDescriptor);
                long propertyCount = 0;
                // structure property
                propertyCount++;
                PropertyDescriptor structurePropertyDescriptor = new PropertyDescriptor();
                structurePropertyDescriptor.setDescription("Structure property");
                structurePropertyDescriptor.setId(propertyCount);
                rowDescriptor.addPropertyDescriptor(structurePropertyDescriptor);
                rowDescriptor.setStructurePropertyId(structurePropertyDescriptor.getId());
                rowDescriptor.setHierarchicalPropertyId(structurePropertyDescriptor.getId());
                // all other properties
                Map<String, Class> propertyTypes = dataItem.getMetadata().getPropertyTypes();
                for (String key : propertyTypes.keySet()) {
                    propertyCount++;
                    PropertyDescriptor plainPropertyDescriptor = new PropertyDescriptor();
                    plainPropertyDescriptor.setDescription(key);
                    plainPropertyDescriptor.setId(propertyCount);
                    rowDescriptor.addPropertyDescriptor(plainPropertyDescriptor);
                }

                datasetMap.put(datasetDescriptor.getId(), datasetDescriptor);
            });
        } catch (IOException e) {
            logger.error(null, e);
        }
        return new ArrayList<>(datasetMap.values());
    }

    public void createDataset(String name, InputStream content) {
        try {
            datasetClient.create(sessionContext.getLoggedInUserDetails().getUserid(), name, content);
        } catch (IOException e) {
            logger.error(null, e);
        }
    }

    public void deleteDataset(IDatasetDescriptor datasetDescriptor) {
        try {
            DatasetDescriptor dataset = (DatasetDescriptor) datasetDescriptor;
            datasetClient.delete(sessionContext.getLoggedInUserDetails().getUserid(), dataset.getDataItem().getId());
        } catch (IOException e) {
            logger.error(null, e);
        }
    }

    public IDatasetDescriptor findDatasetDescriptorById(Long id) {
        return datasetMap.get(id);
    }

    public void loadDatasetContents(IDatasetDescriptor datasetDescriptor) {
        try {
            DatasetDescriptor dataset = (DatasetDescriptor) datasetDescriptor;
            Stream<MoleculeObject> objects = datasetClient.getContentsAsObjects(sessionContext.getLoggedInUserDetails().getUserid(), dataset.getDataItem(), MoleculeObject.class);
            HashMap<UUID, MoleculeObject> datasetContents = new HashMap<>();
            objects.forEach(moleculeObject -> {
                datasetContents.put(moleculeObject.getUUID(), moleculeObject);
            });
            datasetsContentsMap.put(datasetDescriptor.getId(), datasetContents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UUID> listAllUuids(IDatasetDescriptor datasetDescriptor) {
        Map<UUID, MoleculeObject> datasetContents = datasetsContentsMap.get(datasetDescriptor.getId());
        return new ArrayList<>(datasetContents.keySet());
    }

    public List<IRow> listRow(IDatasetDescriptor datasetDescriptor, List<UUID> uuidList) {
        Map<UUID, MoleculeObject> datasetContents = datasetsContentsMap.get(datasetDescriptor.getId());

        // Discuss: I'm forced to match each Row to the only known metadata!
        RowDescriptor rowDescriptor = (RowDescriptor) datasetDescriptor.getAllRowDescriptors().get(0);
        PropertyDescriptor structurePropertyDescriptor = (PropertyDescriptor) rowDescriptor.getStructurePropertyDescriptor();

        List<IRow> result = new ArrayList<>();
        for (UUID uuid : uuidList) {
            MoleculeObject molecule = datasetContents.get(uuid);
            Row row = new Row();
            row.setUuid(molecule.getUUID());
            row.setDescriptor(rowDescriptor);
            row.setProperty(structurePropertyDescriptor, molecule.getSource());

            for (IPropertyDescriptor propertyDescriptor : rowDescriptor.listAllPropertyDescriptors()) {
                if (!propertyDescriptor.getId().equals(structurePropertyDescriptor.getId())) {
                    row.setProperty((PropertyDescriptor) propertyDescriptor, molecule.getValue(propertyDescriptor.getDescription()));
                }
            }

            result.add(row);
        }
        return result;
    }
}