package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@XmlRootElement
public class NotebookInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private static final Logger LOG = Logger.getLogger(NotebookInstance.class.getName());
    private final List<Long> removedCellIdList = new ArrayList<>();
    private final List<CellInstance> cellList = new ArrayList<>();
    private Long lastCellId;


    public NotebookInstance() {

    }

    public NotebookInstance(@JsonProperty("lastCellId") Long lastCellId) {
        this.lastCellId = lastCellId;
    }

    public List<CellInstance> getCellList() {
        return cellList;
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
            variable.setVariableDefinition(variableDefinition);
            variable.setValue(variableDefinition.getDefaultValue());
            variable.setCellId(cell.getId());
            cell.getOutputVariableMap().put(variableDefinition.getName(), variable);
        }
        for (BindingDefinition bindingDefinition : cellDefinition.getBindingDefinitionList()) {
            BindingInstance binding = new BindingInstance();
            binding.setBindingDefinition(bindingDefinition);
            cell.getBindingMap().put(bindingDefinition.getName(), binding);
        }
        for (OptionDescriptor optionDefinition : cellDefinition.getOptionDefinitionList()) {
            OptionInstance option = new OptionInstance();
            option.setOptionDescriptor(optionDefinition);
            cell.getOptionMap().put(optionDefinition.getName(), option);
        }
        return cell;
    }

    public void removeCell(Long id) {
        CellInstance cellInstance = findCellById(id);
        cellList.remove(cellInstance);
        removedCellIdList.add(id);
        for (CellInstance otherCellInstance : cellList) {
            for (BindingInstance bindingInstance : otherCellInstance.getBindingMap().values()) {
                VariableInstance variableInstance = bindingInstance.getVariable();
                if (variableInstance != null && variableInstance.getCellId().equals(id)) {
                    bindingInstance.setVariable(null);
                }
            }
        }
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
                applyCellChanges(cellInstance, localCellInstance);
            }
        }
    }

    private void applyCellChanges(CellInstance cellInstance, CellInstance localCellInstance) {
        if (cellInstance.isDirty()) {
           localCellInstance.setPositionLeft(cellInstance.getPositionLeft());
           localCellInstance.setPositionTop(cellInstance.getPositionTop());
           localCellInstance.setSizeHeight(cellInstance.getSizeHeight());
           localCellInstance.setSizeWidth(cellInstance.getSizeWidth());
        }
        for (OptionInstance optionInstance : cellInstance.getOptionMap().values()) {
            if (optionInstance.isDirty()) {
                localCellInstance.getOptionMap().get(optionInstance.getOptionDescriptor().getName()).setValue(optionInstance.getValue());
            }
        }
        for (VariableInstance variableInstance : cellInstance.getOutputVariableMap().values()) {
            if (variableInstance.isDirty()) {
                localCellInstance.getOutputVariableMap().get(variableInstance.getVariableDefinition().getName()).setValue(variableInstance.getValue());
            }
        }
        for (BindingInstance bindingInstance : cellInstance.getBindingMap().values()) {
            if (bindingInstance.isDirty()) {
                if (bindingInstance.getVariable() == null) {
                    localCellInstance.getBindingMap().get(bindingInstance.getName()).setVariable(null);
                } else {
                    VariableInstance variableInstance = findVariable(bindingInstance.getVariable().getCellId(), bindingInstance.getVariable().getVariableDefinition().getName());
                    localCellInstance.getBindingMap().get(bindingInstance.getName()).setVariable(variableInstance);
                }
            }
        }
    }

    public void resetDirty() {
        removedCellIdList.clear();
        for (CellInstance cellInstance : cellList) {
            cellInstance.resetDirty();
        }
    }

    public static NotebookInstance fromJsonString(String string) throws Exception {
        if (string == null) {
            return null;
        } else {
            return new ObjectMapper().readValue(string, NotebookInstance.class);
        }
    }

    public String toJsonString() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(byteArrayOutputStream, this);
        byteArrayOutputStream.flush();
        return new String(byteArrayOutputStream.toByteArray());
    }

    public Long getLastCellId() {
        return lastCellId;
    }


}
