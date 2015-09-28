package portal.webapp.notebook;


import javax.enterprise.context.SessionScoped;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@SessionScoped
public class NotebooksSession implements Serializable {

    private static final Notebook POC_DESCRIPTOR = createPocDescriptor();

    private static Notebook createPocDescriptor() {
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
            Cell cell = new FileUploadCell();
            cell.setName("File upload 1");
            notebook.addCell(cell);
            cell = new CodeCell();
            cell.setName("CODE 1");
            notebook.addCell(cell);
            cell = new NotebookDebugCell();
            cell.setName("NOTEBOOK_DEBUG 1");
            notebook.addCell(cell);
            return notebook;
        }
    }


    public Notebook retrievePocNotebookDescriptor() {
        return POC_DESCRIPTOR;
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

    public List<CellDescriptor> listCellDescriptor() {
        return Arrays.asList(new CodeCellDescriptor(), new NotebookDebugCellDescriptor(), new FileUploadCellDescriptor());
    }


    public void uploadFile(String clientFileName, InputStream inputStream) {
        try {
            OutputStream outputStream = new FileOutputStream("files/" + clientFileName);
            try {
                byte[] buffer = new byte[4096];
                int r = inputStream.read(buffer, 0, buffer.length);
                while (r > -1) {
                    outputStream.write(buffer, 0, r);
                    r = inputStream.read(buffer);
                }
              outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
