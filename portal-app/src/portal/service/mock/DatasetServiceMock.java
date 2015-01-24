package portal.service.mock;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordReader;
import chemaxon.struc.MProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.service.api.*;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DatasetServiceMock implements DatasetService {

    private static final String STRUCTURE_FIELD_NAME = "structure_as_text"; // TODO decide how to best handle this
    private static final Long STRUCTURE_PROPERTY_ID = 0l;
    private static final Long HIERARCHICAL_PROPERTY_ID = 0l;
    private static final Logger logger = LoggerFactory.getLogger(DatasetServiceMock.class.getName());

    private long nextId = 0;
    private Map<Long, DatasetMock> datasetMockMap = new HashMap<Long, DatasetMock>();
    private Map<Long, DatasetDescriptorMock> datasetDescriptorMap = new HashMap<Long, DatasetDescriptorMock>();

    @Override
    public DatasetDescriptor importFromStream(ImportFromStreamData data) {
        try {
            DatasetDescriptorMock dd = new DatasetDescriptorMock();
            dd.setDescription(data.getDescription());
            DatasetDescriptorMock datasetDescriptor = (DatasetDescriptorMock) createDatasetDescriptor(dd);
            datasetDescriptor.setDatasetMockId(1l);

            DatasetStreamFormat format = data.getDatasetStreamFormat();
            InputStream inputStream = data.getInputStream();
            Map<String, Class> fieldConfig = data.getFieldConfigMap();
            DatasetMock datasetMock = parseSdf(format, inputStream, fieldConfig, datasetDescriptor.getId());
            datasetMock.setId(getNextId());
            datasetMockMap.put(1l, datasetMock);

            datasetDescriptor.setDatasetMockId(datasetMock.getId());
            datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptor);
            return datasetDescriptor;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read file", ex);
        }
    }

    private DatasetMock parseSdf(DatasetStreamFormat format, InputStream inputStream, Map<String, Class> fieldConfig, Long datasetMockId)
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
        logger.info("File processed " + datasetMock.getRowList().size() + " records handled");
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
        return new ArrayList<DatasetDescriptor>(datasetDescriptorMap.values());
    }

    @Override
    public List<Row> listRow(ListRowFilter filter) {
        DatasetMock datasetMock = datasetMockMap.get(filter.getDatasetId());
        if (datasetMock != null) {
            return datasetMock.getRowList();
        } else {
            return new ArrayList<Row>();
        }
    }

    @Override
    public Row findRowById(Long datasetDescriptorId, Long rowId) {
        DatasetMock datasetMock = datasetMockMap.get(datasetDescriptorId);
        return datasetMock.findRowById(rowId);
    }

    @Override
    public DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        DatasetDescriptorMock datasetDescriptorMock = (DatasetDescriptorMock) datasetDescriptor;
        datasetDescriptorMock.setId(getNextId());
        datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptorMock);
        return datasetDescriptor;
    }

    @Override
    public void removeDatasetDescriptor(Long datasetDescriptorId) {
        datasetDescriptorMap.remove(datasetDescriptorId);
        datasetMockMap.remove(datasetDescriptorId);
    }

    @Override
    public RowDescriptor createRowDescriptor(Long datasetDescriptorId, RowDescriptor rowDescriptor) {
        RowDescriptorMock rowDescriptorMock = (RowDescriptorMock) rowDescriptor;
        rowDescriptorMock.setId(getNextId());
        datasetDescriptorMap.get(datasetDescriptorId).addRowDescriptor(rowDescriptorMock);
        return rowDescriptor;
    }

    @Override
    public void removeRowDescriptor(Long datasetDescriptorId, Long rowDescriptorId) {
        DatasetDescriptorMock datasetDescriptor = datasetDescriptorMap.get(datasetDescriptorId);
        datasetDescriptor.removeRowDescriptor(rowDescriptorId);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, PropertyDescriptor propertyDescriptor) {
        PropertyDescriptorMock propertyDescriptorMock = (PropertyDescriptorMock) propertyDescriptor;
        if (propertyDescriptorMock.getId() == null) {
            propertyDescriptorMock.setId(getNextId());
        }
        DatasetDescriptorMock datasetDescriptor = datasetDescriptorMap.get(datasetDescriptorId);
        RowDescriptorMock rowDescriptorMock = (RowDescriptorMock) datasetDescriptor.findRowDescriptorById(rowDescriptorId);
        rowDescriptorMock.addPropertyDescriptor(propertyDescriptorMock);
        return propertyDescriptor;
    }

    @Override
    public void removePropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, Long propertyDescriptorId) {
        DatasetDescriptorMock datasetDescriptor = datasetDescriptorMap.get(datasetDescriptorId);
        RowDescriptorMock rowDescriptorMock = (RowDescriptorMock) datasetDescriptor.findRowDescriptorById(rowDescriptorId);
        rowDescriptorMock.removePropertyDescriptor(propertyDescriptorId);
    }

}
