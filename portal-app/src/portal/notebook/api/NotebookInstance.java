package portal.notebook.api;

import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@XmlRootElement
public class NotebookInstance implements Serializable {

    private static final Logger LOG = Logger.getLogger(NotebookInstance.class.getName());
    private transient final List<Long> removedCellIdList = new ArrayList<>();
    private final List<CellInstance> cellList = new ArrayList<>();
    private Long lastCellId;

    public static NotebookInstance fromBytes(byte[] bytes) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (NotebookInstance) objectInputStream.readObject();
    }

    public List<CellInstance> getCellList() {
        return cellList;
    }

    public VariableInstance findVariable(String producerName, String name) {
        for (CellInstance cell : cellList) {
            if (cell.getName().equals(producerName)) {
                return cell.getOutputVariableMap().get(name);
            }
        }
        return null;
    }

    public VariableInstance findVariable(Long producerId, String name) {
        for (CellInstance cell : cellList) {
            if (cell.getId().equals(producerId)) {
                return cell.getOutputVariableMap().get(name);
            }
        }
        return null;
    }

    public CellInstance addCell(CellDefinition cellType) {
        CellInstance cell = createCell(cellType);
        cell.setName(calculateCellName(cell));
        cellList.add(cell);
        return cell;
    }

    private String calculateCellName(CellInstance cell) {
        int typeCount = 0;
        Set<String> nameSet = new HashSet<String>();
        for (CellInstance item : cellList) {
            if (item.getCellDefinition().equals(cell.getCellDefinition())) {
                typeCount++;
            }
            nameSet.add(item.getName());
        }
        int suffix = typeCount + 1;
        String newName = cell.getCellDefinition().getName() + suffix;
        while (nameSet.contains(newName)) {
            suffix++;
            newName = cell.getCellDefinition().getName() + suffix;
        }
        return newName;
    }

    public CellInstance findCellByName(String name) {
        for (CellInstance cell : cellList) {
            if (cell.getName().equals(name)) {
                return cell;
            }
        }
        return null;
    }

    public CellInstance findCellById(Long id) {
        for (CellInstance cell : cellList) {
            if (cell.getId().equals(id)) {
                return cell;
            }
        }
        return null;
    }

    private CellInstance createCell(CellDefinition cellDefinition) {
        CellInstance cell = new CellInstance();
        cell.setCellDefinition(cellDefinition);
        cell.setId(lastCellId == null ? 1L : lastCellId + 1L);
        lastCellId = cell.getId();
        for (VariableDefinition variableDefinition : cellDefinition.getOutputVariableDefinitionList()) {
            VariableInstance variable = new VariableInstance();
            variable.setName(variableDefinition.getName());
            variable.setDisplayName(variableDefinition.getDisplayName());
            variable.setVariableType(variableDefinition.getVariableType());
            variable.setValue(variableDefinition.getDefaultValue());
            variable.setCellId(cell.getId());
            cell.getOutputVariableMap().put(variableDefinition.getName(), variable);
        }
        for (BindingDefinition bindingDefinition : cellDefinition.getBindingDefinitionList()) {
            BindingInstance binding = new BindingInstance();
            binding.getAcceptedVariableTypeList().addAll(bindingDefinition.getAcceptedVariableTypeList());
            binding.setDisplayName(bindingDefinition.getDisplayName());
            binding.setName(bindingDefinition.getName());
            cell.getBindingMap().put(bindingDefinition.getName(), binding);
        }
        for (OptionDescriptor optionDefinition : cellDefinition.getOptionDefinitionList()) {
            OptionInstance<Object> option = new OptionInstance<>();
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

    public void removeCell(Long id) {
        for (CellInstance cell : cellList) {
            if (cell.getId().equals(id)) {
                cellList.remove(cell);
                removedCellIdList.add(id);
                break;
            }
        }
    }

    public byte[] toBytes() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
        byteArrayOutputStream.flush();
        return  byteArrayOutputStream.toByteArray();
    }

    public List<Long> getRemovedCellIdList() {
        return removedCellIdList;
    }


    public void applyChangesFrom(NotebookInstance notebookInstance) {
        for (Long cellId : notebookInstance.removedCellIdList) {
            removeCell(cellId);
        }
        for (CellInstance cellInstance : notebookInstance.cellList) {
            CellInstance localCellInstance = findCellById(cellInstance.getId());
            if (localCellInstance == null) {
                cellList.add(cellInstance);
                lastCellId = cellInstance.getId();
            }  else {
                localCellInstance.applyChangesFrom(cellInstance);
            }
        }
    }

}
