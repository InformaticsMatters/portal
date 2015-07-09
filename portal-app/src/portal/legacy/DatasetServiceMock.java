package portal.legacy;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.jchem.db.JChemSearch;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordReader;
import chemaxon.sss.search.JChemSearchOptions;
import chemaxon.struc.MProp;
import chemaxon.util.ConnectionHandler;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.chemcentral.ChemcentralConfig;
import portal.chemcentral.ChemcentralEntityManagerProducer;
import portal.chemcentral.ChemcentralSearch;
import portal.chemcentral.StructureSearch;
import portal.dataset.*;
import portal.service.api.ImportFromStreamData;
import portal.service.api.ListDatasetDescriptorFilter;
import portal.service.api.ListRowFilter;
import toolkit.services.PU;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author simetrias
 */
@ApplicationScoped
@Alternative
public class DatasetServiceMock implements DatasetService {

    private static final String STRUCTURE_FIELD_NAME = "structure_as_text"; // TODO decide how to best handle this
    private static final Long STRUCTURE_PROPERTY_ID = 0l;
    private static final Long HIERARCHICAL_PROPERTY_ID = 0l;
    private static final Logger logger = LoggerFactory.getLogger(DatasetServiceMock.class.getName());

    private long nextId = 0;
    private Map<Long, DatasetMock> datasetMockMap = new HashMap<Long, DatasetMock>();
    private Map<Long, DatasetDescriptorMock> datasetDescriptorMockMap = new HashMap<Long, DatasetDescriptorMock>();

    @Inject
    private ChemcentralConfig chemcentralConfig;
    @Inject
    @PU(puName = ChemcentralEntityManagerProducer.CHEMCENTRAL_PU_NAME)
    private EntityManager entityManager;

    @Override
    public DatasetDescriptor createForTreeGridTest() {
        long datasetMockId = getNextId();

        DatasetDescriptorMock ddm = new DatasetDescriptorMock();
        ddm.setDescription("Test for treegrid");
        DatasetDescriptorMock datasetDescriptorMock = (DatasetDescriptorMock) createDatasetDescriptor(ddm);
        datasetDescriptorMock.setDatasetMockId(datasetMockId);
        DatasetMock datasetMock = new DatasetMock();
        datasetMock.setId(datasetMockId);

        RowDescriptorMock rdm;
        rdm = new RowDescriptorMock();
        rdm.setDescription("Structure Row Descriptor");
        RowDescriptorMock structureRowDescriptorMock = (RowDescriptorMock) createRowDescriptor(datasetDescriptorMock.getId(), rdm);
        rdm = new RowDescriptorMock();
        rdm.setDescription("Batch Row Descriptor");
        RowDescriptorMock batchRowDescriptorMock = (RowDescriptorMock) createRowDescriptor(datasetDescriptorMock.getId(), rdm);

        PropertyDescriptorMock pdm;
        pdm = new PropertyDescriptorMock();
        pdm.setDescription("Batch Id");
        PropertyDescriptorMock batchPropertyDescriptor = (PropertyDescriptorMock) createPropertyDescriptor(datasetDescriptorMock.getId(), batchRowDescriptorMock.getId(), pdm);
        pdm = new PropertyDescriptorMock();
        pdm.setDescription("Structure Id");
        PropertyDescriptorMock structurePropertyDescriptor = (PropertyDescriptorMock) createPropertyDescriptor(datasetDescriptorMock.getId(), structureRowDescriptorMock.getId(), pdm);

        structureRowDescriptorMock.setHierarchicalPropertyId(structurePropertyDescriptor.getId());
        batchRowDescriptorMock.setHierarchicalPropertyId(batchPropertyDescriptor.getId());

        RowMock structureRowMock = new RowMock();
        structureRowMock.setRowDescriptor(structureRowDescriptorMock);
        long structureRowId = 1l;
        structureRowMock.setId(structureRowId);
        structureRowMock.setProperty(structurePropertyDescriptor, "Structure 1");
        datasetMock.addRow(structureRowId, structureRowMock);

        String batchPropertyValue = "Batch 1";
        RowMock batchRowMock = structureRowMock.createChild();
        batchRowMock.setRowDescriptor(batchRowDescriptorMock);
        batchRowMock.setProperty(batchPropertyDescriptor, batchPropertyValue);
        long batchRowId = 2l;
        batchRowMock.setId(batchRowId);

        datasetMockMap.put(datasetMockId, datasetMock);
        datasetDescriptorMock.setRowCount(datasetMock.getRowCount());

        logger.info("Created Chemcentral dataset, " + datasetMock.getRowCount() + " records.");

        return datasetDescriptorMock;
    }

    @Override
    public DatasetDescriptor createFromChemcentralSearch(ChemcentralSearch chemcentralSearch) {
        Query query = entityManager.createNativeQuery("select id, source_id, structure_id, batch_id, property_data " +
                "from chemcentral_01.structure_props " +
                "where source_id = 1 " +
                "order by structure_id, batch_id");
        query.setMaxResults(20);
        List resultList = query.getResultList();

        long datasetMockId = getNextId();

        DatasetDescriptorMock ddm = new DatasetDescriptorMock();
        ddm.setDescription(chemcentralSearch.getDescription());
        DatasetDescriptorMock datasetDescriptorMock = (DatasetDescriptorMock) createDatasetDescriptor(ddm);
        datasetDescriptorMock.setDatasetMockId(datasetMockId);
        DatasetMock datasetMock = new DatasetMock();
        datasetMock.setId(datasetMockId);

        RowDescriptorMock rdm;
        rdm = new RowDescriptorMock();
        rdm.setDescription("Structure Row Descriptor");
        RowDescriptorMock structureRowDescriptorMock = (RowDescriptorMock) createRowDescriptor(datasetDescriptorMock.getId(), rdm);

        rdm = new RowDescriptorMock();
        rdm.setDescription("Batch Row Descriptor");
        RowDescriptorMock batchRowDescriptorMock = (RowDescriptorMock) createRowDescriptor(datasetDescriptorMock.getId(), rdm);

        PropertyDescriptorMock pdm;
        pdm = new PropertyDescriptorMock();
        pdm.setDescription("Batch Id");
        PropertyDescriptorMock batchPropertyDescriptor = (PropertyDescriptorMock) createPropertyDescriptor(datasetDescriptorMock.getId(), batchRowDescriptorMock.getId(), pdm);

        pdm = new PropertyDescriptorMock();
        pdm.setDescription("Structure Id");
        PropertyDescriptorMock structurePropertyDescriptor = (PropertyDescriptorMock) createPropertyDescriptor(datasetDescriptorMock.getId(), structureRowDescriptorMock.getId(), pdm);

        pdm = new PropertyDescriptorMock();
        pdm.setDescription("Properties data");
        PropertyDescriptorMock propertiesDataPropertyDescriptor = (PropertyDescriptorMock) createPropertyDescriptor(datasetDescriptorMock.getId(), structureRowDescriptorMock.getId(), pdm);

        pdm = new PropertyDescriptorMock();
        pdm.setDescription("Molecule");
        PropertyDescriptorMock moleculePropertyDescriptor = (PropertyDescriptorMock) createPropertyDescriptor(datasetDescriptorMock.getId(), structureRowDescriptorMock.getId(), pdm);

        structureRowDescriptorMock.setHierarchicalPropertyId(moleculePropertyDescriptor.getId());
        structureRowDescriptorMock.setStructurePropertyId(moleculePropertyDescriptor.getId());
        batchRowDescriptorMock.setHierarchicalPropertyId(batchPropertyDescriptor.getId());

        Iterator iterator = resultList.iterator();
        Object[] result = null;
        if (iterator.hasNext()) {
            result = (Object[]) iterator.next();
        }
        while (result != null) {
            Integer structurePropertyValue = (Integer) result[2];
            RowMock structureRowMock = new RowMock();
            structureRowMock.setRowDescriptor(structureRowDescriptorMock);
            Integer structureRowId = (Integer) result[0];
            structureRowMock.setId(structureRowId.longValue());
            structureRowMock.setProperty(structurePropertyDescriptor, structurePropertyValue);
            PGobject property_data = (PGobject) result[4];
            String propertiesDataValue = property_data.getValue();
            structureRowMock.setProperty(propertiesDataPropertyDescriptor, propertiesDataValue);
            int cdId = findCdIdInPropertiesData(propertiesDataValue);
            structureRowMock.setProperty(moleculePropertyDescriptor, lookupChemcentralStructureData(cdId));
            while (result != null && structurePropertyValue.equals(result[2])) {
                String batchPropertyValue = (String) result[3];
                RowMock batchRowMock = structureRowMock.createChild();
                batchRowMock.setRowDescriptor(batchRowDescriptorMock);
                batchRowMock.setProperty(batchPropertyDescriptor, batchPropertyValue);
                Integer batchRowId = (Integer) result[0];
                batchRowMock.setId(batchRowId.longValue());

                result = iterator.hasNext() ? (Object[]) iterator.next() : null;
            }
            datasetMock.addRow(structureRowId.longValue(), structureRowMock);
        }

        datasetMockMap.put(datasetMockId, datasetMock);
        datasetDescriptorMock.setRowCount(datasetMock.getRowCount());
        return datasetDescriptorMock;
    }

    @Override
    public DatasetDescriptor createFromStructureSearch(StructureSearch structureSearch) {
        try {
            doCreateFromStructureSearch(structureSearch);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doCreateFromStructureSearch(StructureSearch structureSearch) throws Exception {
        ConnectionHandler connectionHandler = getStandaloneConnectionHandler();
        connectionHandler.connectToDatabase();
        try {
            JChemSearchOptions searchOptions = new JChemSearchOptions(JChemSearch.SUBSTRUCTURE);
            JChemSearch searcher = new JChemSearch();
            searcher.setConnectionHandler(connectionHandler);
            searcher.setSearchOptions(searchOptions);
            searcher.setQueryStructure(structureSearch.getStructure());
            searcher.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE);
            searcher.setStructureTable("chemcentral_01.structures");
            searcher.run();
            int[] hits = searcher.getResults();
        } finally {
            connectionHandler.close();
        }
    }

    private ConnectionHandler getStandaloneConnectionHandler() throws Exception {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        connectionHandler.setPropertyTable("chemcentral_01.jchemproperties");
        Properties properties = chemcentralConfig.getChemcentralPersistenceProperties();
        connectionHandler.setDriver(properties.getProperty("javax.persistence.jdbc.driver"));
        connectionHandler.setUrl(properties.getProperty("javax.persistence.jdbc.url"));
        connectionHandler.setLoginName(properties.getProperty("javax.persistence.jdbc.user"));
        connectionHandler.setPassword(properties.getProperty("javax.persistence.jdbc.password"));
        connectionHandler.connectToDatabase();
        return connectionHandler;
    }

    @Override
    public DatasetDescriptor importFromStream(ImportFromStreamData data) {
        try {
            long datasetMockId = getNextId();

            DatasetDescriptorMock ddm = new DatasetDescriptorMock();
            ddm.setDescription(data.getDescription());
            DatasetDescriptorMock datasetDescriptorMock = (DatasetDescriptorMock) createDatasetDescriptor(ddm);
            datasetDescriptorMock.setDatasetMockId(datasetMockId);

            InputStream inputStream = data.getInputStream();
            Map<String, Class> fieldConfig = data.getFieldConfigMap();
            DatasetMock datasetMock = parseSdf(inputStream, fieldConfig, datasetDescriptorMock.getId());
            datasetMock.setId(datasetMockId);

            datasetMockMap.put(datasetMockId, datasetMock);

            datasetDescriptorMock.setRowCount(datasetMock.getRowCount());
            return datasetDescriptorMock;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read file", ex);
        }
    }

    private DatasetMock parseSdf(InputStream inputStream, Map<String, Class> fieldConfig, Long datasetMockId)
            throws Exception {
        MRecordReader recordReader = null;
        DatasetMock datasetMock = new DatasetMock();
        try {
            recordReader = MFileFormatUtil.createRecordReader(inputStream, null, null, null);
            long count = 0;
            logger.info("Parsing file");

            RowDescriptorMock rowDescriptorMock = new RowDescriptorMock();
            rowDescriptorMock.setDescription("Level 1");
            rowDescriptorMock.setHierarchicalPropertyId(HIERARCHICAL_PROPERTY_ID);
            rowDescriptorMock.setStructurePropertyId(STRUCTURE_PROPERTY_ID);
            rowDescriptorMock = (RowDescriptorMock) createRowDescriptor(datasetMockId, rowDescriptorMock);

            while (true) {
                count++;
                logger.debug("Reading record");
                MRecord rec = recordReader.nextRecord();
                if (rec == null) {
                    break;
                } else {
                    RowMock rowMock = new RowMock();
                    rowMock.setId(count);
                    rowMock.setRowDescriptor(rowDescriptorMock);
                    PropertyDescriptorMock propertyDescriptorMock = new PropertyDescriptorMock();
                    propertyDescriptorMock.setId(STRUCTURE_PROPERTY_ID);
                    propertyDescriptorMock.setDescription(STRUCTURE_FIELD_NAME);
                    propertyDescriptorMock = (PropertyDescriptorMock) createPropertyDescriptor(datasetMockId, rowDescriptorMock.getId(), propertyDescriptorMock);
                    rowMock.setProperty(propertyDescriptorMock, rec.getString());
                    String[] fields = rec.getPropertyContainer().getKeys();
                    List<MProp> values = rec.getPropertyContainer().getPropList();
                    for (int x = 0; x < fields.length; x++) {
                        String prop = fields[x];
                        String strVal = MPropHandler.convertToString(values.get(x), null);
                        Object objVal = convert(strVal, fieldConfig.get(prop));
                        logger.trace("Generated value for field " + prop + " of " + objVal + " type " + objVal.getClass().getName());
                        PropertyDescriptorMock ps = new PropertyDescriptorMock();
                        ps.setId(x + 1l);
                        ps.setDescription(prop);
                        createPropertyDescriptor(datasetMockId, rowMock.getDescriptor().getId(), ps);
                        rowMock.setProperty(ps, objVal);
                    }
                    datasetMock.addRow(count, rowMock);
                }
            }
        } finally {
            if (recordReader != null) {
                try {
                    recordReader.close();
                } catch (IOException ioe) {
                    logger.warn("Failed to close MRecordReader", ioe);
                }
            }
        }
        logger.info("File processed " + datasetMock.getAllRows().size() + " records handled");
        return datasetMock;
    }

    private Object convert(String value, Class cls) {
        if (cls == null || cls == String.class) {
            return value;
        } else if (cls == Integer.class) {
            return new Integer(value);
        } else if (cls == Float.class) {
            return new Float(value);
        } else {
            throw new IllegalArgumentException("Unsupported conversion: " + cls.getName());
        }
    }

    private long getNextId() {
        return ++nextId;
    }

    @Override
    public List<DatasetDescriptor> listDatasetDescriptor(ListDatasetDescriptorFilter filter) {
        return new ArrayList<>(datasetDescriptorMockMap.values());
    }

    @Override
    public List<Row> listRow(ListRowFilter filter) {
        DatasetDescriptorMock datasetDescriptorMock = datasetDescriptorMockMap.get(filter.getDatasetDescriptorId());
        DatasetMock datasetMock = datasetMockMap.get(datasetDescriptorMock.getDatasetMockId());
        if (filter.getRowIdList() == null || filter.getRowIdList().size() == 0) {
            return datasetMock.getAllRows();
        } else {
            return datasetMock.getRowList(filter.getRowIdList());
        }
    }

    @Override
    public Row findRowById(Long datasetDescriptorId, Long rowId) {
        DatasetDescriptorMock datasetDescriptorMock = datasetDescriptorMockMap.get(datasetDescriptorId);
        DatasetMock datasetMock = datasetMockMap.get(datasetDescriptorMock.getDatasetMockId());
        return datasetMock.getRowById(rowId);
    }

    @Override
    public DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        DatasetDescriptorMock datasetDescriptorMock = (DatasetDescriptorMock) datasetDescriptor;
        datasetDescriptorMock.setId(getNextId());
        datasetDescriptorMockMap.put(datasetDescriptor.getId(), datasetDescriptorMock);
        return datasetDescriptor;
    }

    @Override
    public void removeDatasetDescriptor(Long datasetDescriptorId) {
        DatasetDescriptorMock datasetDescriptorMock = datasetDescriptorMockMap.get(datasetDescriptorId);
        datasetDescriptorMockMap.remove(datasetDescriptorId);
        datasetMockMap.remove(datasetDescriptorMock.getDatasetMockId());
    }

    @Override
    public RowDescriptor createRowDescriptor(Long datasetDescriptorId, RowDescriptor rowDescriptor) {
        RowDescriptorMock rowDescriptorMock = (RowDescriptorMock) rowDescriptor;
        rowDescriptorMock.setId(getNextId());
        datasetDescriptorMockMap.get(datasetDescriptorId).addRowDescriptor(rowDescriptorMock);
        return rowDescriptor;
    }

    @Override
    public void removeRowDescriptor(Long datasetDescriptorId, Long rowDescriptorId) {
        DatasetDescriptorMock datasetDescriptorMock = datasetDescriptorMockMap.get(datasetDescriptorId);
        datasetDescriptorMock.removeRowDescriptor(rowDescriptorId);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, PropertyDescriptor propertyDescriptor) {
        PropertyDescriptorMock propertyDescriptorMock = (PropertyDescriptorMock) propertyDescriptor;
        if (propertyDescriptorMock.getId() == null) {
            propertyDescriptorMock.setId(getNextId());
        }
        DatasetDescriptorMock datasetDescriptor = datasetDescriptorMockMap.get(datasetDescriptorId);
        RowDescriptorMock rowDescriptorMock = (RowDescriptorMock) datasetDescriptor.getRowDescriptorById(rowDescriptorId);
        rowDescriptorMock.addPropertyDescriptor(propertyDescriptorMock);
        return propertyDescriptor;
    }

    @Override
    public void removePropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, Long propertyDescriptorId) {
        DatasetDescriptorMock datasetDescriptor = datasetDescriptorMockMap.get(datasetDescriptorId);
        RowDescriptorMock rowDescriptorMock = (RowDescriptorMock) datasetDescriptor.getRowDescriptorById(rowDescriptorId);
        rowDescriptorMock.removePropertyDescriptor(propertyDescriptorId);
    }

    @Override
    public List<Long> listAllRowIds(Long datasetDescriptorId) {
        DatasetDescriptorMock datasetDescriptorMock = datasetDescriptorMockMap.get(datasetDescriptorId);
        DatasetMock datasetMock = datasetMockMap.get(datasetDescriptorMock.getDatasetMockId());
        Set<Long> idSet = datasetMock.getAllRowIds();
        return new ArrayList<>(idSet);
    }

    private String lookupChemcentralStructureData(int cdId) {
        Query query = entityManager.createNativeQuery("select cd_structure from chemcentral_01.structures where cd_id=?");
        query.setParameter(1, cdId);
        Object result = query.getSingleResult();
        return new String((byte[]) result);
    }

    private int findCdIdInPropertiesData(String propertiesData) {
        String cdIdStr = propertiesData.substring(9, propertiesData.indexOf(','));
        return Integer.valueOf(cdIdStr.trim());
    }
}
