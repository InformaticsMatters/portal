package portal.service.datamart;

import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.search.JChemSearchOptions;
import chemaxon.util.ConnectionHandler;
import portal.service.api.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.List;

/**
 * @author simetrias
 */
@ApplicationScoped
@Alternative
public class DatamartService implements DatasetService {

    @Override
    public DatasetDescriptor createFromStructureSearch(StructureSearch structureSearch) {
        try {
            return doCreateFromStructureSearch(structureSearch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DatasetDescriptor doCreateFromStructureSearch(StructureSearch structureSearch) throws Exception {
        ConnectionHandler connectionHandler = createDatamartConnectionHandler();
        try {
            JChemSearchOptions searchOptions = new JChemSearchOptions(JChemSearch.SUBSTRUCTURE);
            JChemSearch searcher = new JChemSearch();
            searcher.setConnectionHandler(connectionHandler);
            searcher.setSearchOptions(searchOptions);
            searcher.setQueryStructure(structureSearch.getStructure());
            searcher.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE);
            searcher.run();
            int[] hits = searcher.getResults();
            return null;
        } finally {
            connectionHandler.close();
        }
    }

    private ConnectionHandler createDatamartConnectionHandler() {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        return connectionHandler;
    }

    @Override
    public DatasetDescriptor importFromStream(ImportFromStreamData data) {
        return null;
    }

    @Override
    public List<DatasetDescriptor> listDatasetDescriptor(ListDatasetDescriptorFilter filter) {
        return null;
    }

    @Override
    public List<Row> listRow(ListRowFilter filter) {
        return null;
    }

    @Override
    public Row findRowById(Long datasetDescriptorId, Long rowId) {
        return null;
    }

    @Override
    public DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        return null;
    }

    @Override
    public void removeDatasetDescriptor(Long datasetDescriptorId) {

    }

    @Override
    public RowDescriptor createRowDescriptor(Long datasetDescriptorId, RowDescriptor rowDescriptor) {
        return null;
    }

    @Override
    public void removeRowDescriptor(Long datasetDescriptorId, Long rowDescriptorId) {

    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, PropertyDescriptor propertyDescriptor) {
        return null;
    }

    @Override
    public void removePropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, Long propertyDescriptorId) {

    }
}
