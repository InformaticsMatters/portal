package portal.notebook;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NotebookContents implements Serializable {
    private final List<Cell> cellList = new ArrayList<>();
    private final List<Variable> variableList = new ArrayList<>();

    public List<Cell> getCellList() {
        return cellList;
    }

    public List<Variable> getVariableList() {
        return variableList;
    }

    public Variable findVariable(String producerName, String name) {
        for (Variable variable : variableList) {
            if (variable.getProducerCell().getName().equals(producerName) && variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }

    public static NotebookContents fromBytes(byte[] bytes) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (NotebookContents)objectInputStream.readObject();
    }

    public byte[] toBytes() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
        byteArrayOutputStream.flush();
        return  byteArrayOutputStream.toByteArray();
    }

}
