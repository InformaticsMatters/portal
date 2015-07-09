package portal.dataset;

import portal.chemcentral.ChemcentralSearch;
import portal.chemcentral.StructureSearch;
import portal.service.api.ImportFromStreamData;
import portal.service.api.ListDatasetDescriptorFilter;
import portal.service.api.ListRowFilter;

import java.util.List;

/**
 * @author simetrias
 */
public interface DatasetService {

    DatasetDescriptor createForTreeGridTest();

    DatasetDescriptor createFromChemcentralSearch(ChemcentralSearch chemcentralSearch);

    DatasetDescriptor createFromStructureSearch(StructureSearch structureSearch);

    DatasetDescriptor importFromStream(ImportFromStreamData data);

    List<DatasetDescriptor> listDatasetDescriptor(ListDatasetDescriptorFilter filter);

    List<Row> listRow(ListRowFilter filter);

    Row findRowById(Long datasetDescriptorId, Long rowId);

    DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor);

    void removeDatasetDescriptor(Long datasetDescriptorId);

    RowDescriptor createRowDescriptor(Long datasetDescriptorId, RowDescriptor rowDescriptor);

    void removeRowDescriptor(Long datasetDescriptorId, Long rowDescriptorId);

    PropertyDescriptor createPropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, PropertyDescriptor propertyDescriptor);

    void removePropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, Long propertyDescriptorId);

    List<Long> listAllRowIds(Long datasetDescriptorId);
}
