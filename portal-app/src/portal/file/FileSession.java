package portal.file;

import chemaxon.formats.MFileFormatUtil;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.marvin.io.MRecordReader;
import chemaxon.struc.MProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.dataset.DatasetDescriptor;
import portal.dataset.PropertyDescriptor;
import portal.dataset.RowDescriptor;
import portal.service.api.ImportFromStreamData;

import javax.enterprise.context.SessionScoped;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
@SessionScoped
public class FileSession implements Serializable {

    private static final String STRUCTURE_FIELD_NAME = "structure_as_text"; // TODO decide how to best handle this
    private static final Long STRUCTURE_PROPERTY_ID = 0l;
    private static final Long HIERARCHICAL_PROPERTY_ID = 0l;
    private static final Logger logger = LoggerFactory.getLogger(FileSession.class.getName());

    private long nextId = 0;
    private Map<Long, FileDataset> fileDatasetMap = new HashMap<>();
    private Map<Long, FileDatasetDescriptor> fileDatasetDescriptorMap = new HashMap<>();

    public DatasetDescriptor importFromStream(ImportFromStreamData data) {
        try {
            long fileDatasetId = getNextId();

            FileDatasetDescriptor fdd = new FileDatasetDescriptor();
            fdd.setDescription(data.getDescription());
            FileDatasetDescriptor fileDatasetDescriptor = (FileDatasetDescriptor) createDatasetDescriptor(fdd);
            fileDatasetDescriptor.setFileDatasetId(fileDatasetId);

            InputStream inputStream = data.getInputStream();
            Map<String, Class> fieldConfig = data.getFieldConfigMap();
            FileDataset fileDataset = parseSdf(inputStream, fieldConfig, fileDatasetDescriptor.getId());
            fileDataset.setId(fileDatasetId);

            fileDatasetMap.put(fileDatasetId, fileDataset);

            fileDatasetDescriptor.setRowCount(fileDataset.getRowCount());
            return fileDatasetDescriptor;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read file", ex);
        }
    }

    private FileDataset parseSdf(InputStream inputStream, Map<String, Class> fieldConfig, Long fileDatasetId)
            throws Exception {
        MRecordReader recordReader = null;
        FileDataset fileDataset = new FileDataset();
        try {
            recordReader = MFileFormatUtil.createRecordReader(inputStream, null, null, null);
            long count = 0;
            logger.info("Parsing file");

            FileRowDescriptor fileRowDescriptor = new FileRowDescriptor();
            fileRowDescriptor.setDescription("Level 1");
            fileRowDescriptor.setHierarchicalPropertyId(HIERARCHICAL_PROPERTY_ID);
            fileRowDescriptor.setStructurePropertyId(STRUCTURE_PROPERTY_ID);
            fileRowDescriptor = (FileRowDescriptor) createRowDescriptor(fileDatasetId, fileRowDescriptor);

            while (true) {
                count++;
                logger.debug("Reading record");
                MRecord rec = recordReader.nextRecord();
                if (rec == null) {
                    break;
                } else {
                    FileRow fileRow = new FileRow();
                    fileRow.setId(count);
                    fileRow.setRowDescriptor(fileRowDescriptor);
                    FilePropertyDescriptor filePropertyDescriptor = new FilePropertyDescriptor();
                    filePropertyDescriptor.setId(STRUCTURE_PROPERTY_ID);
                    filePropertyDescriptor.setDescription(STRUCTURE_FIELD_NAME);
                    filePropertyDescriptor = (FilePropertyDescriptor) createPropertyDescriptor(fileDatasetId, fileRowDescriptor.getId(), filePropertyDescriptor);
                    fileRow.setProperty(filePropertyDescriptor, rec.getString());
                    String[] fields = rec.getPropertyContainer().getKeys();
                    List<MProp> values = rec.getPropertyContainer().getPropList();
                    for (int x = 0; x < fields.length; x++) {
                        String prop = fields[x];
                        String strVal = MPropHandler.convertToString(values.get(x), null);
                        Object objVal = convert(strVal, fieldConfig.get(prop));
                        logger.trace("Generated value for field " + prop + " of " + objVal + " type " + objVal.getClass().getName());
                        FilePropertyDescriptor ps = new FilePropertyDescriptor();
                        ps.setId(x + 1l);
                        ps.setDescription(prop);
                        createPropertyDescriptor(fileDatasetId, fileRow.getDescriptor().getId(), ps);
                        fileRow.setProperty(ps, objVal);
                    }
                    fileDataset.addRow(count, fileRow);
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
        logger.info("File processed " + fileDataset.getAllRows().size() + " records handled");
        return fileDataset;
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

    public DatasetDescriptor createDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        FileDatasetDescriptor fileDatasetDescriptor = (FileDatasetDescriptor) datasetDescriptor;
        fileDatasetDescriptor.setId(getNextId());
        fileDatasetDescriptorMap.put(datasetDescriptor.getId(), fileDatasetDescriptor);
        return datasetDescriptor;
    }

    public PropertyDescriptor createPropertyDescriptor(Long datasetDescriptorId, Long rowDescriptorId, PropertyDescriptor propertyDescriptor) {
        FilePropertyDescriptor filePropertyDescriptor = (FilePropertyDescriptor) propertyDescriptor;
        if (filePropertyDescriptor.getId() == null) {
            filePropertyDescriptor.setId(getNextId());
        }
        FileDatasetDescriptor datasetDescriptor = fileDatasetDescriptorMap.get(datasetDescriptorId);
        FileRowDescriptor fileRowDescriptor = (FileRowDescriptor) datasetDescriptor.getRowDescriptorById(rowDescriptorId);
        fileRowDescriptor.addPropertyDescriptor(filePropertyDescriptor);
        return propertyDescriptor;
    }

    public RowDescriptor createRowDescriptor(Long datasetDescriptorId, RowDescriptor rowDescriptor) {
        FileRowDescriptor fileRowDescriptor = (FileRowDescriptor) rowDescriptor;
        fileRowDescriptor.setId(getNextId());
        fileDatasetDescriptorMap.get(datasetDescriptorId).addRowDescriptor(fileRowDescriptor);
        return rowDescriptor;
    }

    private long getNextId() {
        return ++nextId;
    }

}
