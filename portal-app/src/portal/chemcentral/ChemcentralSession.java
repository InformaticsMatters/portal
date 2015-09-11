package portal.chemcentral;

import portal.dataset.IDatasetDescriptor;
import portal.dataset.IRow;
import portal.dataset.IRowDescriptor;
import portal.webapp.workflow.DatasetFilterData;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

/**
 * @author simetrias
 */
@SessionScoped
public class ChemcentralSession implements Serializable {

    @Inject
    private ChemcentralClient client;
    private Map<Long, IDatasetDescriptor> datasetDescriptorMap;

    private void loadDatamartDatasetList() {
        datasetDescriptorMap = new HashMap<>();
        List<Hitlist> result = client.listHitlist();
        for (Hitlist hitlist : result) {
            IDatasetDescriptor datasetDescriptor = newDatasetDescriptorFromHitlist(hitlist);
            datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptor);
        }
    }

    private IDatasetDescriptor newDatasetDescriptorFromHitlist(Hitlist hitlist) {
        ChemcentralDatasetDescriptor ddd = new ChemcentralDatasetDescriptor(hitlist);

        ChemcentralPropertyDescriptor dpd = new ChemcentralPropertyDescriptor();
        dpd.setId(1l);
        dpd.setDescription("Structure");

        ChemcentralRowDescriptor drd = new ChemcentralRowDescriptor();
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

    public List<IRow> listRow(Long datasetDescriptorId, List<Long> structureIdList) {
        IDatasetDescriptor datasetDescriptor = datasetDescriptorMap.get(datasetDescriptorId);

        // Discuss: I'm forced to match each Row to the only known metadata!
        ChemcentralRowDescriptor drd = (ChemcentralRowDescriptor) datasetDescriptor.getAllRowDescriptors().get(0);
        ChemcentralPropertyDescriptor dpd = (ChemcentralPropertyDescriptor) drd.getStructurePropertyDescriptor();

        ArrayList<IRow> rows = new ArrayList<>(structureIdList.size());
        List<Structure> structures = client.listStructure(structureIdList);
        for (Structure structure : structures) {
            ChemcentralRow chemcentralRow = new ChemcentralRow();
            chemcentralRow.setId(Long.valueOf(structure.getCdId()));
            chemcentralRow.setDescriptor(drd);
            chemcentralRow.setProperty(dpd, structure.getCdStructure());
            rows.add(chemcentralRow);
        }
        return rows;
    }

    public List<PropertyData> listPropertyData(IDatasetDescriptor datasetDescriptor, PropertyDefinition propertyDefinition) {
        return client.listPropertyData(datasetDescriptor.getId(), propertyDefinition.getOriginalId());
    }

    public void addPropertyToDataset(IDatasetDescriptor datasetDescriptor, String jsonParameterName) {
        List<IRowDescriptor> allRowDescriptors = datasetDescriptor.getAllRowDescriptors();
        for (IRowDescriptor rowDescriptor : allRowDescriptors) {
            Long id = (long) rowDescriptor.listAllPropertyDescriptors().size();
            ChemcentralPropertyDescriptor chemcentralPropertyDescriptor = new ChemcentralPropertyDescriptor();
            chemcentralPropertyDescriptor.setDescription(jsonParameterName);
            chemcentralPropertyDescriptor.setId(id + 1);
            ChemcentralRowDescriptor chemcentralRowDescriptor = (ChemcentralRowDescriptor) rowDescriptor;
            chemcentralRowDescriptor.addPropertyDescriptor(chemcentralPropertyDescriptor);
        }
    }

    public IDatasetDescriptor findDatasetDescriptorById(Long id) {
        return datasetDescriptorMap.get(id);
    }

    public List<IDatasetDescriptor> listDatasets(DatasetFilterData datasetFilterData) {
        if (datasetFilterData != null) {
            System.out.println("Searching " + datasetFilterData.getPattern());
        }

        if (datasetDescriptorMap == null) {
            loadDatamartDatasetList();
        }

        return new ArrayList<>(datasetDescriptorMap.values());
    }
}
