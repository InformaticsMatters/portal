package portal.notebook.webapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.types.MoleculeObject;

import javax.enterprise.context.SessionScoped;
import java.io.*;
import java.util.*;

@SessionScoped
public class Datasets implements Serializable {

    private final Map<Long, Map<UUID, MoleculeObject>> moleculeObjectMapMap = new HashMap<>();
    private final Map<Long, List<UUID>> uuidListMap = new HashMap<>();
    private final Map<Long, IDatasetDescriptor> datasetDescriptorMap = new HashMap<>();
    private long lastDatasetId = 0;

    public Datasets() {
        moleculeObjectMapMap.put(0L, new HashMap<>());
    }

    public IDatasetDescriptor createDatasetFromMolecules(List<MoleculeObject> list, String name) {
        Map<UUID, MoleculeObject> objectMap = new HashMap<>();
        List<UUID> uuidList = new ArrayList<>();
        for (MoleculeObject moleculeObject : list) {
            objectMap.put(moleculeObject.getUUID(), moleculeObject);
            uuidList.add(moleculeObject.getUUID());
        }
        Long datasetId = nextDatasetId();
        moleculeObjectMapMap.put(datasetId, objectMap);
        uuidListMap.put(datasetId, uuidList);

        TableDisplayDatasetDescriptor datasetDescriptor = new TableDisplayDatasetDescriptor(datasetId, name, list.size());

        RowDescriptor rowDescriptor = new RowDescriptor();

        PropertyDescriptor structurePropertyDescriptor = new PropertyDescriptor();
        structurePropertyDescriptor.setDescription("Structure property");
        structurePropertyDescriptor.setId(1l);
        rowDescriptor.addPropertyDescriptor(structurePropertyDescriptor);
        rowDescriptor.setStructurePropertyId(structurePropertyDescriptor.getId());
        rowDescriptor.setHierarchicalPropertyId(structurePropertyDescriptor.getId());
        long propertyCount = 1;
        Set<String> keySet = new HashSet<>();
        for (MoleculeObject moleculeObject : list) {
            for (String key : moleculeObject.getValues().keySet()) {
                if (!keySet.contains(key)) {
                    propertyCount++;
                    PropertyDescriptor plainPropertyDescriptor = new PropertyDescriptor();
                    plainPropertyDescriptor.setDescription(key);
                    plainPropertyDescriptor.setId(propertyCount);
                    rowDescriptor.addPropertyDescriptor(plainPropertyDescriptor);
                    keySet.add(key);
                }
            }
        }
        datasetDescriptor.addRowDescriptor(rowDescriptor);
        datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptor);
        return datasetDescriptor;
    }

    private synchronized Long nextDatasetId() {
        lastDatasetId++;
        return lastDatasetId;
    }

    public List<IRow> listdatasetRow(IDatasetDescriptor datasetDescriptor, List<UUID> uuidList) {
        Map<UUID, MoleculeObject> datasetContents = moleculeObjectMapMap.get(datasetDescriptor.getId());

        if (datasetContents.isEmpty()) {
            return new ArrayList<>();
        }

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

    public IDatasetDescriptor findDatasetDescriptorById(Long datasetDescriptorId) {
        return datasetDescriptorMap.get(datasetDescriptorId);
    }

    public List<UUID> listAllDatasetUuids(IDatasetDescriptor datasetDescriptor) {
        List<UUID> list = uuidListMap.get(datasetDescriptor.getId());
        if (list == null) {
            return new ArrayList<>();
        } else {
            return list;
        }
    }

    public static List<MoleculeObject> parseTsv(InputStream inputStream) throws IOException, IOException {
        List<MoleculeObject> list = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = bufferedReader.readLine();
        String[] headers = line.split("\t");
        for (int h = 0; h < headers.length; h++) {
            headers[h] = trim(headers[h]);
        }
        line = bufferedReader.readLine();
        while (line != null) {
            line = line.trim();
            String[] columns = line.split("\t");
            String value = columns[0].trim();
            String smile = value.substring(1, value.length() - 1);
            MoleculeObject object = new MoleculeObject(smile);
            for (int i = 1; i < columns.length; i++) {
                String name = headers[i];
                String prop = trim(columns[i]);
                object.putValue(name, prop);
            }
            list.add(object);
            line = bufferedReader.readLine();
        }
        return list;
    }

    public static List<MoleculeObject> parseJson(InputStream inputStream) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(inputStream, new TypeReference<List<MoleculeObject>>() {
        });
    }


    public static String trim(String v) {
        if (v.length() > 1 && v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
            return v.substring(1, v.length() - 1);
        } else {
            return v;
        }
    }

    public MoleculeObject findMoleculeObject(Long datasetDescriptorId, UUID rowId) {
        return moleculeObjectMapMap.get(datasetDescriptorId).get(rowId);
    }

}
