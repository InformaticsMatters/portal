package portal.notebook.service;


import portal.notebook.api.CellType;
import portal.notebook.api.VariableDefinition;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotebookContents implements Serializable {
    private Long lastCellId;
    private final List<Cell> cellList = new ArrayList<>();

    public List<Cell> getCellList() {
        return cellList;
    }

    public Variable findVariable(String producerName, String name) {
        for (Cell cell : cellList) {
            if (cell.getName().equals(producerName)) {
                for (Variable variable : cell.getOutputVariableList()) {
                    if (variable.getName().equals(name)) {
                        return variable;
                    }
                }
            }
        }
        return null;
    }

    public Cell addCell(CellType cellType) {
        Cell cell = createCell(cellType);
        cell.setName(calculateCellName(cell));
        cellList.add(cell);
        if (lastCellId == null) {
            cell.setId(1l);
        } else {
            cell.setId(lastCellId + 1l);
        }
        lastCellId = cell.getId();
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
        String newName = cell.getCellType().getName() + suffix;
        while (nameSet.contains(newName)) {
            suffix++;
            newName = cell.getCellType().getName() + suffix;
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

    public Cell findCell(String name) {
        for (Cell cell : cellList) {
            if (cell.getName().equals(name)) {
                return cell;
            }
        }
        return null;
    }

    private Cell createCell(CellType cellType) {
        Cell cell = new Cell();
        cell.setCellType(cellType);
        for (VariableDefinition variableDefinition : cellType.getOutputVariableDefinitionList()) {
            Variable variable = new Variable();
            variable.setName(variableDefinition.getName());
            variable.setVariableType(variableDefinition.getVariableType());
            variable.setValue(variableDefinition.getDefaultValue());
            variable.setProducerCell(cell);
            cell.getOutputVariableList().add(variable);
        }
        return cell;
    }

}
