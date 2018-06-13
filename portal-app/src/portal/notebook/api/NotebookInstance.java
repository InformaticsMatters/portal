package portal.notebook.api;


import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.options.OptionDescriptor;
import portal.notebook.webapp.NotebookSession;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;

@XmlRootElement
public class NotebookInstance implements Serializable {
    private final static long serialVersionUID = 1l;

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(NotebookInstance.class.getName());
    private final List<Long> removedCellIdList = new ArrayList<>();
    private final List<CellInstance> cellInstanceList = new ArrayList<>();
    private Long lastCellId;
    private boolean editable;
    private String versionDescription;
    private Integer canvasWidth;
    private Integer canvasHeight;
    private final NotebookSession notebookSession;

    public NotebookInstance(NotebookSession notebookSession) {
        this.notebookSession = notebookSession;
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

    private VariableInstance findVariableByCellId(Long producerId, String name) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getId().equals(producerId)) {
                return cell.getVariableInstanceMap().get(name);
            }
        }
        return null;
    }

    private OptionInstance findOptionByCellId(Long producerId, String key) {
        for (CellInstance cell : cellInstanceList) {
            if (cell.getId().equals(producerId)) {
                return cell.getOptionInstanceMap().get(key);
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
        applyDefaultValues(cellInstance);
        cellInstanceList.add(cellInstance);
        return cellInstance;
    }

    private void applyDefaultValues(CellInstance cellInstance) {
        for (OptionInstance optionInstance : cellInstance.getOptionInstanceMap().values()) {
            optionInstance.setValue(optionInstance.getOptionDescriptor().getDefaultValue());
        }
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
        for (IODescriptor iod : cellDefinition.getVariableDefinitionList()) {
            VariableInstance variable = new VariableInstance();
            variable.setVariableDefinition(iod);
            variable.setCellId(cellInstance.getId());
            cellInstance.getVariableInstanceMap().put(iod.getName(), variable);
        }
        for (BindingDefinition bindingDefinition : cellDefinition.getBindingDefinitionList()) {
            BindingInstance binding = new BindingInstance();
            binding.setBindingDefinition(bindingDefinition);
            cellInstance.getBindingInstanceMap().put(bindingDefinition.getName(), binding);
        }
        for (OptionBindingDefinition optionBindingDefinition : cellDefinition.getOptionBindingDefinitionList()) {
            OptionBindingInstance binding = new OptionBindingInstance();
            binding.setOptionBindingDefinition(optionBindingDefinition);
            cellInstance.getOptionBindingInstanceMap().put(optionBindingDefinition.getKey(), binding);
        }
        for (OptionDescriptor optionDescriptor : cellDefinition.getOptionDefinitionList()) {
            OptionInstance option = new OptionInstance();
            option.setOptionDescriptor(optionDescriptor);
            option.setCellId(cellInstance.getId());
            cellInstance.getOptionInstanceMap().put(optionDescriptor.getKey(), option);
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
            for (OptionBindingInstance optionBindingInstance : otherCellInstance.getOptionBindingInstanceMap().values()) {
                OptionInstance optionInstance = optionBindingInstance.getOptionInstance();
                if (optionInstance != null && optionInstance.getCellId().equals(id)) {
                    optionBindingInstance.setOptionInstance(null);
                }
            }
        }
    }


    public void storeNotebookCanvasDTO(NotebookCanvasDTO notebookCanvasDTO) {
        notebookCanvasDTO.putProperty("canvasWidth", canvasWidth);
        notebookCanvasDTO.putProperty("canvasHeight", canvasHeight);
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
            cellDTO.getSettings().putAll(cell.getSettings());

            notebookCanvasDTO.addCell(cellDTO);
            for (BindingInstance b : cell.getBindingInstanceMap().values()) {
                VariableInstance variableInstance = b.getVariableInstance();
                NotebookCanvasDTO.BindingDTO bindingDTO = new NotebookCanvasDTO.BindingDTO(
                        b.getName(), // this is correct. It is NOT the variable key. dto prop name should be "name"
                        variableInstance == null ? null : variableInstance.getCellId(), // Long producerId
                        variableInstance == null ? null : variableInstance.getVariableDefinition().getName() // String producerVariableName TODO - is this correct?
                );
                cellDTO.addBinding(bindingDTO);
            }
            for (OptionInstance option : cell.getOptionInstanceMap().values()) {
                cellDTO.addOption(option.getOptionDescriptor().getKey(), option.getValue());
            }
            for (OptionBindingInstance b : cell.getOptionBindingInstanceMap().values()) {
                OptionInstance optionInstance = b.getOptionInstance();
                NotebookCanvasDTO.OptionBindingDTO bindingDTO = new NotebookCanvasDTO.OptionBindingDTO(
                        b.getKey(),
                        optionInstance == null ? null : optionInstance.getCellId(),
                        optionInstance == null ? null : optionInstance.getOptionDescriptor().getKey()
                );
                cellDTO.addOptionBinding(bindingDTO);
            }
        }
    }

    public void loadNotebookCanvasDTO(NotebookCanvasDTO notebookCanvasDTO, CellDefinitionRegistry cellDefinitionRegistry) {
        if (notebookCanvasDTO == null) {
            // TODO: this shouldn't be necessary here. Means need to take care of it somewhere else. We want it to crash to signal possible bug.
            // new notebook - no contents
            return;
        } else {
            canvasWidth = (Integer) notebookCanvasDTO.getProperty("canvasWidth");
            canvasHeight = (Integer) notebookCanvasDTO.getProperty("canvasHeight");
            setLastCellId(notebookCanvasDTO.getLastCellId());
            for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
                // TODO - error handling
                // if cell can't be created for any reason replace it with a "error" cell that contains
                // as much info as is reasonable so that user can try to remedy the problem
                LOG.fine("Restoring cell " + cellDTO.getKey() + "/" + cellDTO.getName());
                // cellDTO.getKey() is the name that identifies the cell e.g. RDKitConformers
                // cellDTO.getName() is the display name that has been given to that cell instance e.g. RDKitConformers1
                CellDefinition cellDefinition = notebookSession.findCellByName(cellDTO.getKey());
                if (cellDefinition == null) {
                    LOG.log(Level.WARNING, "Unknown cell definition: " + cellDTO.getKey());
                } else {
                    CellInstance cellInstance = new CellInstance();
                    cellInstance.setCellDefinition(cellDefinition);
                    cellInstance.setId(cellDTO.getId());
                    cellInstance.setName(cellDTO.getName());
                    cellInstance.setPositionLeft(cellDTO.getLeft());
                    cellInstance.setPositionTop(cellDTO.getTop());
                    cellInstance.setSizeHeight(cellDTO.getHeight());
                    cellInstance.setSizeWidth(cellDTO.getWidth());
                    cellInstance.getSettings().putAll(cellDTO.getSettings());

                    configureCellInstance(cellInstance);
                    cellInstanceList.add(cellInstance);
                    for (Map.Entry<String, Object> e : cellDTO.getOptions().entrySet()) {
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
                            if (variableInstance != null && variableInstance.getVariableDefinition() != null) {
                                LOG.fine("Initializing variable binding of " + bindingInstance.getBindingDefinition().getName() + " to " + variableInstance.getVariableDefinition().getName());
                                bindingInstance.setVariableInstance(variableInstance);
                            } else {
                                LOG.warning("Could not bind variable for " + bindingInstance.getName());
                            }
                        }
                    }
                    for (NotebookCanvasDTO.OptionBindingDTO bindingDTO : cellDTO.getOptionBindings()) {
                        OptionBindingInstance bindingInstance = cellInstance.getOptionBindingInstanceMap().get(bindingDTO.getOptionKey());
                        if (bindingInstance != null && bindingDTO.getProducerKey() != null) {
                            OptionInstance optionInstance = findOptionByCellId(bindingDTO.getProducerId(), bindingDTO.getProducerKey());
                            if (optionInstance != null) {
                                bindingInstance.setOptionInstance(optionInstance);
                            } else {
                                LOG.warning("Could not bind option for " + bindingInstance.getName());
                            }
                        }
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

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }


    public String getVersionDescription() {
        return versionDescription;
    }

    public void setVersionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
    }

    public Integer getCanvasWidth() {
        return canvasWidth;
    }

    public void setCanvasWidth(Integer canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    public Integer getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasHeight(Integer canvasHeight) {
        this.canvasHeight = canvasHeight;
    }

    /**
     * Find the cell (if any) bound to the specified cell using the specified variable name
     *
     * @param cellInstance
     * @param varname
     * @return
     */
    public CellInstance findCellBoundToVariable(CellInstance cellInstance, String varname) {

        VariableInstance variableInstance = cellInstance.getBindingInstanceMap().get(varname).getVariableInstance();
        if (variableInstance != null) {
            Long upstreamCellId = variableInstance.getCellId();
            return findCellInstanceById(upstreamCellId);
        }

        return null;
    }

}
