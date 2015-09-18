package portal.webapp.notebook;


import javax.enterprise.context.SessionScoped;
import java.io.*;
import java.util.Arrays;
import java.util.List;

@SessionScoped
public class NotebooksSession implements Serializable {

    private static final NotebookDescriptor POC_DESCRIPTOR = createPocDescriptor();

    private static NotebookDescriptor createPocDescriptor() {
        File file = new File("PoC.dat");
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    return (NotebookDescriptor) objectInputStream.readObject();
                } finally {
                    inputStream.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            NotebookDescriptor notebookDescriptor = new NotebookDescriptor();
            notebookDescriptor.setName("PoC");
            CellDescriptor cellDescriptor = new CodeCellDescriptor();
            cellDescriptor.setName("CODE 1");
            notebookDescriptor.getCellDescriptorList().add(cellDescriptor);
            cellDescriptor = new NotebookDebugCellDescriptor();
            cellDescriptor.setName("DEBUG 1");
            notebookDescriptor.getCellDescriptorList().add(cellDescriptor);
            return notebookDescriptor;
        }
    }


    public NotebookDescriptor retrievePocNotebookDescriptor() {
        return POC_DESCRIPTOR;
    }

    public void saveNotebookDescriptor(NotebookDescriptor notebookDescriptor) {
        try {
            OutputStream outputStream = new FileOutputStream(notebookDescriptor.getName() + ".dat");
            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(notebookDescriptor);
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
        return Arrays.asList(new CodeCellDescriptor(), new NotebookDebugCellDescriptor());
    }

    // palette items
    public List<CellTemplate> listCellTemplate() {
        return Arrays.asList(new CodeCellTemplate(), new NotebookDebugCellTemplate());
    }


}
