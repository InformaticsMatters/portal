package portal.integration;

import portal.service.api.DatasetDescriptor;
import portal.service.api.Row;
import portal.service.api.RowDescriptor;
import portal.webapp.DatasetFilterData;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

/**
 * @author simetrias
 */
@SessionScoped
public class DatasetSession implements Serializable {

    @Inject
    private IntegrationClient client;
    private Map<Long, DatasetDescriptor> datasetDescriptorMap;

    private void loadDatamartDatasetList() {
        datasetDescriptorMap = new HashMap<>();
        List<Hitlist> result = client.listHitlist();
        for (Hitlist hitlist : result) {
            DatasetDescriptor datasetDescriptor = newDatasetDescriptorFromHitlist(hitlist);
            datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptor);
        }
    }

    private DatasetDescriptor newDatasetDescriptorFromHitlist(Hitlist hitlist) {
        DatamartDatasetDescriptor ddd = new DatamartDatasetDescriptor(hitlist);

        DatamartPropertyDescriptor dpd = new DatamartPropertyDescriptor();
        dpd.setId(1l);
        dpd.setDescription("Structure");

        DatamartRowDescriptor drd = new DatamartRowDescriptor();
        drd.setId(1l);
        drd.setStructurePropertyId(dpd.getId());
        drd.setHierarchicalPropertyId(dpd.getId());
        drd.addPropertyDescriptor(dpd);

        ddd.addRowDescriptor(drd);
        return ddd;
    }

    public List<Long> listAllRowIds(Long datasetDescriptorId) {
        Hitlist hitlist = client.loadHitlist(datasetDescriptorId);
        return Arrays.asList(hitlist.getItems());
    }

    public List<Row> listRow(Long datasetDescriptorId, List<Long> structureIdList) {
        DatasetDescriptor datasetDescriptor = datasetDescriptorMap.get(datasetDescriptorId);

        // Discuss: I'm forced to match each Row to the only known metadata!
        DatamartRowDescriptor drd = (DatamartRowDescriptor) datasetDescriptor.getAllRowDescriptors().get(0);
        DatamartPropertyDescriptor dpd = (DatamartPropertyDescriptor) drd.getStructurePropertyDescriptor();

        ArrayList<Row> rows = new ArrayList<>(structureIdList.size());
        List<Structure> structures = client.listStructure(structureIdList);
        for (Structure structure : structures) {
            DatamartRow datamartRow = new DatamartRow();
            datamartRow.setId(Long.valueOf(structure.getCdId()));
            datamartRow.setDescriptor(drd);
            datamartRow.setProperty(dpd, structure.getCdStructure());
            rows.add(datamartRow);
        }
        return rows;
    }

    public List<PropertyData> listPropertyData(DatasetDescriptor datasetDescriptor, PropertyDefinition propertyDefinition) {
        return client.listPropertyData(datasetDescriptor.getId(), propertyDefinition.getOriginalId());
    }

    public void addPropertyToDataset(DatasetDescriptor datasetDescriptor, String jsonParameterName) {
        List<RowDescriptor> allRowDescriptors = datasetDescriptor.getAllRowDescriptors();
        for (RowDescriptor rowDescriptor : allRowDescriptors) {
            Long id = (long) rowDescriptor.listAllPropertyDescriptors().size();
            DatamartPropertyDescriptor datamartPropertyDescriptor = new DatamartPropertyDescriptor();
            datamartPropertyDescriptor.setDescription(jsonParameterName);
            datamartPropertyDescriptor.setId(id + 1);
            DatamartRowDescriptor datamartRowDescriptor = (DatamartRowDescriptor) rowDescriptor;
            datamartRowDescriptor.addPropertyDescriptor(datamartPropertyDescriptor);
        }
    }

    public DatasetDescriptor findDatasetDescriptorById(Long id) {
        return datasetDescriptorMap.get(id);
    }

    public List<DatasetDescriptor> listDatasets(DatasetFilterData datasetFilterData) {
        if (datasetFilterData != null) {
            System.out.println("Searching " + datasetFilterData.getPattern());
        }

        if (datasetDescriptorMap == null) {
            loadDatamartDatasetList();
        }

        return new ArrayList<>(datasetDescriptorMap.values());
    }
}
