package portal.notebook.service;

import org.squonk.notebook.api.BindingDefinition;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.api.OptionDefinition;
import org.squonk.notebook.api.VariableDefinition;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotebookContents implements Serializable {
    private final List<Cell> cellList = new ArrayList<>();
    private Long lastCellId;

    public static NotebookContents fromBytes(byte[] bytes) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (NotebookContents) objectInputStream.readObject();
    }

    public List<Cell> getCellList() {
        return cellList;
    }

    public Variable findVariable(String producerName, String name) {
        for (Cell cell : cellList) {
            if (cell.getName().equals(producerName)) {
                return cell.getOutputVariableMap().get(name);
            }
        }
        return null;
    }

    public Cell addCell(CellType cellType) {
        Cell cell = createCell(cellType);
        cell.setName(calculateCellName(cell));
        cellList.add(cell);
        if (lastCellId == null) {
            cell.setId(1L);
        } else {
            cell.setId(lastCellId + 1L);
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
            variable.setDisplayName(variableDefinition.getDisplayName());
            variable.setVariableType(variableDefinition.getVariableType());
            variable.setValue(variableDefinition.getDefaultValue());
            variable.setProducerCell(cell);
            cell.getOutputVariableMap().put(variableDefinition.getName(), variable);
        }
        for (BindingDefinition bindingDefinition : cellType.getBindingDefinitionList()) {
            Binding binding = new Binding();
            binding.getAcceptedVariableTypeList().addAll(bindingDefinition.getAcceptedVariableTypeList());
            binding.setDisplayName(bindingDefinition.getDisplayName());
            binding.setName(bindingDefinition.getName());
            cell.getBindingMap().put(bindingDefinition.getName(), binding);
        }
        for (OptionDefinition optionDefinition : cellType.getOptionDefinitionList()) {
            Option<Object> option = new Option<>();
            option.setName(optionDefinition.getName());
            option.setDisplayName(optionDefinition.getDisplayName());
            option.setValue(optionDefinition.getDefaultValue());
            if (optionDefinition.getPicklistValueList() != null) {
                for (Object value : optionDefinition.getPicklistValueList()) {
                    option.addPickListValue(value);
                }
            }
            cell.getOptionMap().put(option.getName(), option);
        }
        return cell;
    }

    public void removeCell(String name) {
        for (Cell cell : cellList) {
            if (cell.getName().equals(name)) {
                cellList.remove(cell);
                break;
            }
        }
    }
}
