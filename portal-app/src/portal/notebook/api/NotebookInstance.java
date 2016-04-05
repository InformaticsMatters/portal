package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.notebook.api.*;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.io.JsonHandler;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

@XmlRootElement
public class NotebookInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private static final Logger LOG = Logger.getLogger(NotebookInstance.class.getName());
    private final List<Long> removedCellIdList = new ArrayList<>();
    private final List<CellInstance> cellInstanceList = new ArrayList<>();
    private Long lastCellId;


    public NotebookInstance() {

    }

    public NotebookInstance(@JsonProperty("lastCellId") Long lastCellId) {
        this.lastCellId = lastCellId;
    }

    public List<CellInstance> getCellInstanceList() {
        return cellInstanceList;
    }

    public VariableInstance findVariableByCellName(String producerName, String name) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getName().equals(producerName)) {
                return cell.getVariableInstanceMap().get(name);
            }
        }
        return null;
    }

    public VariableInstance findVariableByCellId(Long producerId, String name) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getId().equals(producerId)) {
                return cell.getVariableInstanceMap().get(name);
            }
        }
        return null;
    }

    public CellInstance addCellInstance(CellDefinition cellType) {
        CellInstance cell = createCellInstance(cellType);
        cell.setName(calculateCellName(cell));
        cellInstanceList.add(cell);
        return cell;
    }

    private String calculateCellName(CellInstance cell) {
        int typeCount = 0;
        Set<String> nameSet = new HashSet<String>();
        for (CellInstance item : cellInstanceList) {
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

    public CellInstance findCellInstanceByName(String name) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getName().equals(name)) {
                return cell;
            }
        }
        return null;
    }

    public CellInstance findCellInstanceById(Long id) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getId().equals(id)) {
                return cell;
            }
        }
        return null;
    }

    private CellInstance createCellInstance(CellDefinition cellDefinition) {
        CellInstance cell = new CellInstance();
        cell.setCellDefinition(cellDefinition);
        cell.setId(lastCellId == null ? 1L : lastCellId + 1L);
        lastCellId = cell.getId();
        for (VariableDefinition variableDefinition : cellDefinition.getVariableDefinitionList()) {
            VariableInstance variable = new VariableInstance();
            variable.setVariableDefinition(variableDefinition);
            variable.setCellId(cell.getId());
            cell.getVariableInstanceMap().put(variableDefinition.getName(), variable);
        }
        for (BindingDefinition bindingDefinition : cellDefinition.getBindingDefinitionList()) {
            BindingInstance binding = new BindingInstance();
            binding.setBindingDefinition(bindingDefinition);
            cell.getBindingInstanceMap().put(bindingDefinition.getName(), binding);
        }
        for (OptionDescriptor optionDescriptor : cellDefinition.getOptionDefinitionList()) {
            OptionInstance option = new OptionInstance();
            option.setOptionDescriptor(optionDescriptor);
            cell.getOptionInstanceMap().put(optionDescriptor.getkey(), option);
        }
        return cell;
    }

    public void removeCellInstance(Long id) {
        CellInstance cellInstance = findCellInstanceById(id);
        cellInstanceList.remove(cellInstance);
        removedCellIdList.add(id);
        for (CellInstance otherCellInstance : cellInstanceList) {
            for (BindingInstance bindingInstance : otherCellInstance.getBindingInstanceMap().values()) {
                VariableInstance variableInstance = bindingInstance.getVariableInstance();
                if (variableInstance != null && variableInstance.getCellId().equals(id)) {
                    bindingInstance.setVariableInstance(null);
                }
            }
        }
    }

    public void applyChangesFrom(NotebookInstance notebookInstance) {
        for (Long cellId : notebookInstance.removedCellIdList) {
            removeCellInstance(cellId);
        }
        for (CellInstance cellInstance : notebookInstance.cellInstanceList) {
            CellInstance localCellInstance = findCellInstanceById(cellInstance.getId());
            if (localCellInstance == null) {
                cellInstanceList.add(cellInstance);
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
        for (OptionInstance optionInstance : cellInstance.getOptionInstanceMap().values()) {
            if (optionInstance.isDirty()) {
                localCellInstance.getOptionInstanceMap().get(optionInstance.getOptionDescriptor().getkey()).setValue(optionInstance.getValue());
            }
        }
        for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
            if (bindingInstance.isDirty()) {
                if (bindingInstance.getVariableInstance() == null) {
                    localCellInstance.getBindingInstanceMap().get(bindingInstance.getName()).setVariableInstance(null);
                } else {
                    VariableInstance variableInstance = findVariableByCellId(bindingInstance.getVariableInstance().getCellId(), bindingInstance.getVariableInstance().getVariableDefinition().getName());
                    localCellInstance.getBindingInstanceMap().get(bindingInstance.getName()).setVariableInstance(variableInstance);
                }
            }
        }
    }

    public void resetDirty() {
        removedCellIdList.clear();
        for (CellInstance cellInstance : cellInstanceList) {
            cellInstance.resetDirty();
        }
    }

    public static NotebookInstance fromJsonString(String string) throws Exception {
        if (string == null) {
            return null;
        } else {
            NotebookCanvasDTO dto = JsonHandler.getInstance().objectFromJson(string, NotebookCanvasDTO.class);
            return fromCanvasDTO(dto);
        }
    }

    protected void fixReferences() {
        for (CellInstance cellInstance : cellInstanceList) {
            for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
                VariableInstance variableInstance = bindingInstance.getVariableInstance();
                 if (variableInstance != null) {
                     bindingInstance.setVariableInstance(findVariableByCellId(variableInstance.getCellId(), variableInstance.getVariableDefinition().getName()));
                 }
            }
        }

    }

    public String toJsonString() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(byteArrayOutputStream, this);
        byteArrayOutputStream.flush();
        return new String(byteArrayOutputStream.toByteArray());
    }

    public NotebookCanvasDTO toCanvasDTO() {
        NotebookCanvasDTO dto = new NotebookCanvasDTO(lastCellId);
        for (CellInstance cell : getCellInstanceList()) {

            // cells
            NotebookCanvasDTO.CellDTO cellDTO = new NotebookCanvasDTO.CellDTO(
                    cell.getId(),
                    1L, // TODO handle versioning of cells. For now everything is version 1
                    cell.getCellDefinition().getName(), // TODO should there be a specific key for the cell definition?
                    cell.getName(), // TODO allow user to change the cell name in UI
                    cell.getPositionTop(),
                    cell.getPositionLeft(),
                    cell.getSizeWidth(), // TODO set to null if cell is not resizable
                    cell.getSizeHeight() // TODO set to null if cell is not resizable
                    );

            dto.addCell(cellDTO);

            // bindings
            for (BindingInstance b :cell.getBindingInstanceMap().values()) {
                cellDTO.addBinding(new NotebookCanvasDTO.BindingDTO(
                        b.getName(), // String variableKey TODO - is this correct?
                        b.getVariableInstance().getCellId(), // Long producerId
                        b.getVariableInstance().getVariableDefinition().getName() // String producerVariableName TODO - is this correct?
                        ));
            }

            // options
            for (OptionInstance option : cell.getOptionInstanceMap().values()) {
                cellDTO.addOption(new NotebookCanvasDTO.OptionDTO(
                        option.getOptionDescriptor().getkey(), // String key
                        option.getValue() // Object value
                ));
            }
        }

        return dto;
    }

    public static NotebookInstance fromCanvasDTO(NotebookCanvasDTO dto) {
        if (dto == null) {
            // TODO - review this. Will it only ever be null the first time?
            return new NotebookInstance(1L);
        }

        NotebookInstance instance = new NotebookInstance(dto.getLastCellId());
        // TODO - handle the cells, bindings and options

        instance.fixReferences();
        return instance;
    }

    public Long getLastCellId() {
        return lastCellId;
    }


}
