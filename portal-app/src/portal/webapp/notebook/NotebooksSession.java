package portal.webapp.notebook;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import portal.dataset.*;

import javax.enterprise.context.SessionScoped;
import java.io.*;
import java.util.*;

@SessionScoped
public class NotebooksSession implements Serializable {

    private static final Notebook POC_NOTEBOOK = createPocNotebook();
    private final Map<Long, Map<UUID, MoleculeObject>> fileObjectsMap = new HashMap<>();
    private final Map<Long, IDatasetDescriptor> datasetDescriptorMap = new HashMap<>();
    private long lastDatasetId = 0;

    public NotebooksSession() {
        fileObjectsMap.put(0l, new HashMap<>());
    }

    private static Notebook createPocNotebook() {
        File file = new File("PoC.dat");
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    Notebook notebook = (Notebook)objectInputStream.readObject();
                    return notebook;
                } finally {
                    inputStream.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Notebook notebook = new Notebook();
            notebook.setName("PoC");
            /**
            Cell cell = new FileUploadCell();
            cell.setName("File upload 1");
            notebook.addCell(cell);
            cell = new ScriptCell();
            cell.setName("CODE 1");
            notebook.addCell(cell);
            cell = new PropertyCalculateCell();
            cell.setName("Property calculate 1");
            notebook.addCell(cell);
            cell = new NotebookDebugCell();
            cell.setName("NOTEBOOK_DEBUG 1");
            notebook.addCell(cell);
             **/
            return notebook;
        }
    }


    public Notebook retrievePocNotebook() {
        return POC_NOTEBOOK;
    }

    public void saveNotebook(Notebook notebook) {
        try {
            OutputStream outputStream = new FileOutputStream(notebook.getName() + ".dat");
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(notebook);
                objectOutputStream.flush();
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Cell> listCell() {
        return Arrays.asList(new ScriptCell(), new NotebookDebugCell());
    }

    public List<CellDescriptor> listCellDescriptor() {
        return Arrays.asList(new ScriptCellDescriptor(), new NotebookDebugCellDescriptor(), new FileUploadCellDescriptor(), new PropertyCalculateCellDescriptor(), new TableDiplayCellDescriptor());
    }


    public List<MoleculeObject> retrieveFileContentAsMolecules(String fileName) {
        try {
            return parseFile(fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<Variable> listAvailableInputVariablesFor(Cell cell, Notebook notebook) {
        List<Variable> list = new ArrayList<>();
        for (Variable variable : notebook.getVariableList()) {
            if (!variable.getProducer().equals(cell)) {
               list.add(variable);
            }
        }

        Collections.sort(list, new Comparator<Variable>() {
            @Override
            public int compare(Variable o1, Variable o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });
        return list;
    }


    public List<UUID> listAllUuids(IDatasetDescriptor datasetDescriptor) {
        Map<UUID, MoleculeObject> map = fileObjectsMap.get(datasetDescriptor.getId());
        return new ArrayList<>(map.keySet());
    }

    public IDatasetDescriptor createDatasetFromMolecules(List<MoleculeObject> list, String name) {
        Map<UUID, MoleculeObject> objectMap = new HashMap<>();
        for (MoleculeObject moleculeObject : list) {
            objectMap.put(moleculeObject.getUUID(), moleculeObject);
        }
        Long datasetId = nextDatasetId();
        fileObjectsMap.put(datasetId, objectMap);

        TableDisplayDescriptor datasetDescriptor = new TableDisplayDescriptor(datasetId, name, list.size());

        RowDescriptor rowDescriptor = new RowDescriptor();
        datasetDescriptor.addRowDescriptor(rowDescriptor);
        PropertyDescriptor structurePropertyDescriptor = new PropertyDescriptor();
        structurePropertyDescriptor.setDescription("Structure property");
        structurePropertyDescriptor.setId(1l);
        rowDescriptor.addPropertyDescriptor(structurePropertyDescriptor);
        rowDescriptor.setStructurePropertyId(structurePropertyDescriptor.getId());
        rowDescriptor.setHierarchicalPropertyId(structurePropertyDescriptor.getId());
        datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptor);
        return datasetDescriptor;
    }

    public IDatasetDescriptor createDatasetFromStrings(Strings value, String name) {
        List<MoleculeObject> list = new ArrayList<>();
        for (String smile : value.getStrings()) {
            MoleculeObject moleculeObject = new MoleculeObject(smile);
            list.add(moleculeObject);
        }
        return createDatasetFromMolecules(list, name);
    }

    public IDatasetDescriptor loadDatasetFromFile(String fileName) {
        try {
            List<MoleculeObject> list = parseFile(fileName);
            return createDatasetFromMolecules(list, fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<MoleculeObject> parseFile(String fileName) throws Exception {
        int x = fileName.lastIndexOf(".");
        String ext = fileName.toLowerCase().substring(x + 1);
        if (ext.equals("json")) {
            return parseJson(fileName);
        } else if (ext.equals("tab")) {
            return parseTsv(fileName);
        } else {
            return new ArrayList<>();
        }
    }

    private List<MoleculeObject> parseTsv(String fileName) throws IOException {
        File file = new File("files/" + fileName);
        InputStream inputStream = new FileInputStream(file);
        try {
            List<MoleculeObject> list = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            while (line != null) {
                line = line.trim();
                String[] columns = line.split("\t");
                String value = columns[0].trim();
                String smile = value.substring(1, value.length() - 1);
                MoleculeObject object = new MoleculeObject(smile);
                list.add(object);
                line = bufferedReader.readLine();
            }
            return list;
        } finally {
            inputStream.close();
        }
    }

    private synchronized Long nextDatasetId() {
        lastDatasetId++;
        return lastDatasetId;
    }

    private List<MoleculeObject> parseJson(String fileName) throws Exception {
        File file = new File("files/" + fileName);
        InputStream inputStream = new FileInputStream(file);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, new TypeReference<List<MoleculeObject>>() {});
        } finally {
            inputStream.close();
        }
    }

    public List<IRow> listRow(IDatasetDescriptor datasetDescriptor, List<UUID> uuidList) {
        Map<UUID, MoleculeObject> datasetContents = fileObjectsMap.get(datasetDescriptor.getId());

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


}
