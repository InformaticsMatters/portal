package portal.webapp.notebook;


import javax.enterprise.context.SessionScoped;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@SessionScoped
public class NotebooksSession implements Serializable {

    private static final Notebook POC_NOTEBOOK = createPocNotebook();

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


    public byte[] retrieveFileContentAsMolecules(String fileName) {
        try {
            File file = new File("files/" + fileName);
            InputStream inputStream = new FileInputStream(file);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int r = inputStream.read(buffer, 0, buffer.length);
                while (r > -1) {
                    byteArrayOutputStream.write(buffer, 0, r);
                    r = inputStream.read(buffer, 0, buffer.length);
                }
                byteArrayOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
