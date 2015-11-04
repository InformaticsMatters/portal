package portal.notebook;


import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public Cell addCell(Cell cell) {
        cell.setName(calculateCellName(cell));
        cellList.add(cell);
        for (Variable variable : cell.getOutputVariableList()) {
            variableList.add(variable);
        }
        return cell;
    }

    private String calculateCellName(Cell cell) {
        int typeCount = 0;
        Set<String> nameSet = new HashSet<String>();
        for (Cell item : cellList) {
            if (item.getCellType().equals(cell.getCellType())) {
                typeCount++;
            }
            nameSet.add(item.getName());
        }
        int suffix = typeCount + 1;
        String newName = cell.getCellType().name() + suffix;
        while (nameSet.contains(newName)) {
            suffix++;
            newName = cell.getCellType().name() + suffix;
        }
        return newName;
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
