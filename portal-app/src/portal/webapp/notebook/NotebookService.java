package portal.webapp.notebook;


import javax.enterprise.context.ApplicationScoped;
import java.io.*;

@ApplicationScoped
public class NotebookService {
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
            notebookDescriptor.getCellDescriptorList().add(new Poc1CellDescriptor());
            notebookDescriptor.getCellDescriptorList().add(new CodeCellDescriptor());
            notebookDescriptor.getCellDescriptorList().add(new TableDisplayCellDescriptor());
            notebookDescriptor.getCellDescriptorList().add(new NotebookDebugCellDescriptor());
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

}
