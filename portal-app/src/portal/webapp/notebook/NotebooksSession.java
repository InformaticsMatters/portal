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
            Cell cell = new QndProducerCell();
            notebook.registerCell(cell);
            cell = new CodeCell();
            cell.setName("CODE 1");
            notebook.registerCell(cell);
            cell = new NotebookDebugCell();
            cell.setName("NOTEBOOK_DEBUG 1");
            notebook.registerCell(cell);
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

    public List<Cell> listCellDescriptor() {
        return Arrays.asList(new CodeCell(), new NotebookDebugCell());
    }

    // palette items
    public List<CellTemplate> listCellTemplate() {
        return Arrays.asList(new CodeCellTemplate(), new NotebookDebugCellTemplate());
    }


}
