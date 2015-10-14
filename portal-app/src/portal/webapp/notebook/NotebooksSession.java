package portal.webapp.notebook;


import com.im.lac.types.MoleculeObject;
import portal.dataset.DatasetDescriptor;
import portal.dataset.IDatasetDescriptor;

import javax.enterprise.context.SessionScoped;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

@SessionScoped
public class NotebooksSession implements Serializable {

    private static final Notebook POC_NOTEBOOK = createPocNotebook();
    private final Map<String, Map<UUID, MoleculeObject>> fileObjectsMap = new HashMap<>();

    private static Notebook createPocNotebook() {
        File file = new File("PoC.dat");
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    return (Notebook) objectInputStream.readObject();
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
            return parseTSV(fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadTSV(String fileName) {
        try {
            List<MoleculeObject> objects = parseTSV(fileName);
            Map<UUID, MoleculeObject> objectMap = new HashMap<>();
            objects.forEach(moleculeObject -> {
                objectMap.put(moleculeObject.getUUID(), moleculeObject);
            });
            fileObjectsMap.put(fileName, objectMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<MoleculeObject> parseTSV(String fileName) throws IOException {
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


}
