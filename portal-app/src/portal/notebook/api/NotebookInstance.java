package portal.notebook.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.notebook.api.*;
import org.squonk.options.OptionDescriptor;
import portal.notebook.cells.SimpleCellDefinition;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

@XmlRootElement
public class NotebookInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private static final Logger LOGGER = Logger.getLogger(NotebookInstance.class.getName());
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


    public NotebookCanvasDTO toNotebookCanvasDTO() {
        NotebookCanvasDTO dto = new NotebookCanvasDTO(getLastCellId());
        for (CellInstance cell : getCellInstanceList()) {
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
            for (BindingInstance b :cell.getBindingInstanceMap().values()) {
                VariableInstance variableInstance = b.getVariableInstance();
                cellDTO.addBinding(new NotebookCanvasDTO.BindingDTO(
                        b.getName(), // this is correct. It is NOT hte variable key. dto prop name should be "name"
                        variableInstance == null ? null : variableInstance.getCellId(), // Long producerId
                        variableInstance == null ? null : variableInstance.getVariableDefinition().getName() // String producerVariableName TODO - is this correct?
                        ));
            }
            for (OptionInstance option : cell.getOptionInstanceMap().values()) {
                cellDTO.addOption(new NotebookCanvasDTO.OptionDTO(
                        option.getOptionDescriptor().getkey(), // String key
                        option.getValue() // Object value
                ));
            }
        }
        return dto;
    }

    public static NotebookInstance fromNotebookCanvasDTO(NotebookCanvasDTO notebookCanvasDTO, CellDefinitionRegistry cellDefinitionRegistry) {
        NotebookInstance notebookInstance = new NotebookInstance(notebookCanvasDTO.getLastCellId());
        for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
            CellDefinition cellDefinition = cellDefinitionRegistry.findCellDefinition(cellDTO.getKey());
            CellInstance cellInstance = notebookInstance.addCellInstance(cellDefinition);
            cellInstance.setId(cellDTO.getId());
            cellInstance.setName(cellDTO.getName());
            cellInstance.setPositionLeft(cellDTO.getLeft());
            cellInstance.setPositionTop(cellDTO.getTop());
            cellInstance.setSizeHeight(cellDTO.getHeight());
            cellInstance.setSizeWidth(cellDTO.getWidth());
            for (NotebookCanvasDTO.OptionDTO optionDTO : cellDTO.getOptions()) {
                OptionInstance optionInstance = cellInstance.getOptionInstanceMap().get(optionDTO.getKey());
                if (optionInstance != null) {
                    optionInstance.setValue(optionDTO.getValue());
                }
            }
        }

        for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
            CellInstance cellInstance = notebookInstance.findCellInstanceById(cellDTO.getId());
            for (NotebookCanvasDTO.BindingDTO bindingDTO : cellDTO.getBindings()) {
                BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(bindingDTO.getVariableKey());
                if (bindingInstance != null && bindingDTO.getProducerVariableName() != null) {
                    VariableInstance variableInstance = notebookInstance.findVariableByCellId(bindingDTO.getProducerId(), bindingDTO.getProducerVariableName());
                    bindingInstance.setVariableInstance(variableInstance);
                }
            }
        }
        return notebookInstance;
    }

    public Long getLastCellId() {
        return lastCellId;
    }


}
