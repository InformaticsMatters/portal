package portal.notebook.api;


import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.options.OptionDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;

@XmlRootElement
public class NotebookInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(NotebookInstance.class.getName());
    private final List<Long> removedCellIdList = new ArrayList<>();
    private final List<CellInstance> cellInstanceList = new ArrayList<>();
    private Long lastCellId;


    public NotebookInstance() {

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

    public CellInstance addCellInstance(CellDefinition cellDefinition) {
        CellInstance cellInstance = new CellInstance();
        cellInstance.setCellDefinition(cellDefinition);
        cellInstance.setId(getLastCellId() == null ? 1L : getLastCellId() + 1L);
        setLastCellId(cellInstance.getId());
        cellInstance.setName(calculateCellName(cellInstance));
        configureCellInstance(cellInstance);
        cellInstanceList.add(cellInstance);
        return cellInstance;
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

    private CellInstance configureCellInstance(CellInstance cellInstance) {
        CellDefinition cellDefinition = cellInstance.getCellDefinition();
        for (VariableDefinition variableDefinition : cellDefinition.getVariableDefinitionList()) {
            VariableInstance variable = new VariableInstance();
            variable.setVariableDefinition(variableDefinition);
            variable.setCellId(cellInstance.getId());
            cellInstance.getVariableInstanceMap().put(variableDefinition.getName(), variable);
        }
        for (BindingDefinition bindingDefinition : cellDefinition.getBindingDefinitionList()) {
            BindingInstance binding = new BindingInstance();
            binding.setBindingDefinition(bindingDefinition);
            cellInstance.getBindingInstanceMap().put(bindingDefinition.getName(), binding);
        }
        for (OptionDescriptor optionDescriptor : cellDefinition.getOptionDefinitionList()) {
            OptionInstance option = new OptionInstance();
            option.setOptionDescriptor(optionDescriptor);
            cellInstance.getOptionInstanceMap().put(optionDescriptor.getkey(), option);
        }
        return cellInstance;
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


    public void storeNotebookCanvasDTO(NotebookCanvasDTO notebookCanvasDTO) {
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
            notebookCanvasDTO.addCell(cellDTO);
            for (BindingInstance b :cell.getBindingInstanceMap().values()) {
                VariableInstance variableInstance = b.getVariableInstance();
                NotebookCanvasDTO.BindingDTO bindingDTO = new NotebookCanvasDTO.BindingDTO(
                        b.getName(), // this is correct. It is NOT hte variable key. dto prop name should be "name"
                        variableInstance == null ? null : variableInstance.getCellId(), // Long producerId
                        variableInstance == null ? null : variableInstance.getVariableDefinition().getName() // String producerVariableName TODO - is this correct?
                );
                cellDTO.addBinding(bindingDTO);
            }
            for (OptionInstance option : cell.getOptionInstanceMap().values()) {
                cellDTO.addOption(option.getOptionDescriptor().getkey(),option.getValue());
            }
        }
    }

    public void loadNotebookCanvasDTO(NotebookCanvasDTO notebookCanvasDTO, CellDefinitionRegistry cellDefinitionRegistry) {
        setLastCellId(notebookCanvasDTO.getLastCellId());
        for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
            // TODO - error handling
            // if cell can't be created for any reason replace it with a "error" cell that contains
            // as much info as is reasonable so that user can try to remedy the problem
            CellDefinition cellDefinition = cellDefinitionRegistry.findCellDefinition(cellDTO.getKey());
            if (cellDefinition == null) {
                LOGGER.log(Level.WARNING, "Unknown cell definition: " + cellDTO.getKey());
            } else {
                CellInstance cellInstance = new CellInstance();
                cellInstance.setCellDefinition(cellDefinition);
                cellInstance.setId(cellDTO.getId());
                cellInstance.setName(cellDTO.getName());
                cellInstance.setPositionLeft(cellDTO.getLeft());
                cellInstance.setPositionTop(cellDTO.getTop());
                cellInstance.setSizeHeight(cellDTO.getHeight());
                cellInstance.setSizeWidth(cellDTO.getWidth());
                configureCellInstance(cellInstance);
                cellInstanceList.add(cellInstance);
                for (Map.Entry<String,Object> e: cellDTO.getOptions().entrySet()) {
                    OptionInstance optionInstance = cellInstance.getOptionInstanceMap().get(e.getKey());
                    if (optionInstance != null) {
                        optionInstance.setValue(e.getValue());
                    }
                }
            }
        }

        for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
            CellInstance cellInstance = findCellInstanceById(cellDTO.getId());
            if (cellInstance != null) {
                for (NotebookCanvasDTO.BindingDTO bindingDTO : cellDTO.getBindings()) {
                    BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(bindingDTO.getVariableKey());
                    if (bindingInstance != null && bindingDTO.getProducerVariableName() != null) {
                        VariableInstance variableInstance = findVariableByCellId(bindingDTO.getProducerId(), bindingDTO.getProducerVariableName());
                        bindingInstance.setVariableInstance(variableInstance);
                    }
                }
            }
        }
    }

    public Long getLastCellId() {
        return lastCellId;
    }


    public void setLastCellId(Long lastCellId) {
        this.lastCellId = lastCellId;
    }
}
