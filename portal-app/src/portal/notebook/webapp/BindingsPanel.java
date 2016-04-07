package portal.notebook.webapp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.options.OptionDescriptor;
import portal.notebook.api.*;


import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;

/**
 * @author simetrias
 */
public class BindingsPanel extends Panel {

    private static final Logger logger = LoggerFactory.getLogger(BindingsPanel.class);
    private CellInstance cellInstance;
    @Inject
    private NotebookSession notebookSession;
    private List<BindingInstance> bindingInstanceList;
    private WebMarkupContainer bindingListContainer;

    public BindingsPanel(String id, CellInstance cellInstance) {
        super(id);
        this.cellInstance = cellInstance;
        addBindingList();
    }

    private void addBindingList() {
        bindingListContainer = new WebMarkupContainer("bindingListContainer");
        bindingListContainer.setOutputMarkupId(true);

        bindingInstanceList = rebuildBindingInstanceList();
        ListView<BindingInstance> listView = new ListView<BindingInstance>("binding", bindingInstanceList) {

            @Override
            protected void populateItem(ListItem<BindingInstance> listItem) {
                final BindingInstance bindingInstance = listItem.getModelObject();
                listItem.add(new Label("targetName", bindingInstance.getDisplayName()));
                VariableInstance variableInstance = bindingInstance.getVariableInstance();
                String sourceDisplayName = resolveDisplayNameFor(variableInstance);
                listItem.add(new Label("variableName", sourceDisplayName));
                AjaxLink unassignLink = new AjaxLink("unassign") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        removeBinding(bindingInstance);
                        ajaxRequestTarget.add(BindingsPanel.this.getPage());
                    }
                };
                unassignLink.setVisible(sourceDisplayName != null);
                listItem.add(unassignLink);
            }
        };
        bindingListContainer.add(listView);
        add(bindingListContainer);
    }

    private void removeBinding(BindingInstance bindingInstance) {
        CellInstance boundCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellInstance.getId());
        BindingInstance boundBindingInstance = boundCellInstance.getBindingInstanceMap().get(bindingInstance.getName());
        boundBindingInstance.setVariableInstance(null);
        notebookSession.storeCurrentNotebook();
        cellInstance = boundCellInstance;
    }

    private List<BindingInstance> rebuildBindingInstanceList() {
        if (cellInstance == null) {
            return new ArrayList<>();
        } else {
            ArrayList<BindingInstance> list = new ArrayList<BindingInstance>(cellInstance.getBindingInstanceMap().values());
            Collections.sort(list, new Comparator<BindingInstance>() {

                @Override
                public int compare(BindingInstance o1, BindingInstance o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return list;
        }
    }

    private String resolveDisplayNameFor(VariableInstance variableInstance) {
        if (variableInstance == null) {
            return null;
        }
        CellInstance producerCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(variableInstance.getCellId());
        return producerCellInstance.getName() + " " + variableInstance.getVariableDefinition().getDisplayName();
    }

    @XmlRootElement
    public static class BindingInstance implements Serializable {
        private final static long serialVersionUID = 1l;
        private BindingDefinition bindingDefinition;
        private VariableInstance variableInstance;
        private boolean dirty = true;

        @JsonIgnore
        public String getName() {
            return bindingDefinition.getName();
        }

        @JsonIgnore
        public String getDisplayName() {
            return bindingDefinition.getDisplayName();
        }

        public VariableInstance getVariableInstance() {
            return variableInstance;
        }

        public void setVariableInstance(VariableInstance variableInstance) {
            dirty = true;
            this.variableInstance = variableInstance;
        }

        @JsonIgnore
        public boolean isDirty() {
            return dirty;
        }

        public void resetDirty() {
            dirty = false;
        }

        public BindingDefinition getBindingDefinition() {
            return bindingDefinition;
        }

        public void setBindingDefinition(BindingDefinition bindingDefinition) {
            this.bindingDefinition = bindingDefinition;
        }
    }

    @XmlRootElement
    public static class CellInstance implements Serializable {
        private final static long serialVersionUID = 1l;

        private CellDefinition cellDefinition;
        private final Map<String, BindingInstance> bindingInstanceMap = new LinkedHashMap<>();
        private final Map<String, VariableInstance> variableInstanceMap = new LinkedHashMap<>();
        private final Map<String, OptionInstance> optionInstanceMap = new LinkedHashMap<>();
        private Long id;
        private String name;
        private Integer positionLeft;
        private Integer positionTop;
        private Integer sizeWidth;
        private Integer sizeHeight;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public CellDefinition getCellDefinition() {
            return cellDefinition;
        }

        public void setCellDefinition(CellDefinition cellDefinition) {
            this.cellDefinition = cellDefinition;
        }

        public Map<String, BindingInstance> getBindingInstanceMap() {
            return bindingInstanceMap;
        }

        public Map<String, VariableInstance> getVariableInstanceMap() {
            return variableInstanceMap;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Map<String, OptionInstance> getOptionInstanceMap() {
            return optionInstanceMap;
        }

        public Integer getPositionLeft() {
            return positionLeft;
        }

        public void setPositionLeft(Integer positionLeft) {
            this.positionLeft = positionLeft;
        }

        public Integer getPositionTop() {
            return positionTop;
        }

        public void setPositionTop(Integer positionTop) {
            this.positionTop = positionTop;
        }

        public Integer getSizeWidth() {
            return sizeWidth;
        }

        public void setSizeWidth(Integer sizeWidth) {
            this.sizeWidth = sizeWidth;
        }

        public Integer getSizeHeight() {
            return sizeHeight;
        }

        public void setSizeHeight(Integer sizeHeight) {
            this.sizeHeight = sizeHeight;
        }

    }

    @XmlRootElement
    public static class NotebookInstance implements Serializable {
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
                    cellDTO.addOption(new NotebookCanvasDTO.OptionDTO(
                            option.getOptionDescriptor().getkey(), // String key
                            option.getValue() // Object value
                    ));
                }
            }
        }

        public void loadNotebookCanvasDTO(NotebookCanvasDTO notebookCanvasDTO, CellDefinitionRegistry cellDefinitionRegistry) {
            setLastCellId(notebookCanvasDTO.getLastCellId());
            for (NotebookCanvasDTO.CellDTO cellDTO : notebookCanvasDTO.getCells()) {
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
                    for (NotebookCanvasDTO.OptionDTO optionDTO : cellDTO.getOptions()) {
                        OptionInstance optionInstance = cellInstance.getOptionInstanceMap().get(optionDTO.getKey());
                        if (optionInstance != null) {
                            optionInstance.setValue(optionDTO.getValue());
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

    @XmlRootElement
    public static class OptionInstance implements Serializable {
        private final static long serialVersionUID = 1l;
        private OptionDescriptor optionDescriptor;
        private Object value;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public OptionDescriptor getOptionDescriptor() {
            return optionDescriptor;
        }

        public void setOptionDescriptor(OptionDescriptor optionDescriptor) {
            this.optionDescriptor = optionDescriptor;
        }
    }

    @XmlRootElement
    public static class VariableInstance implements Serializable {
        private final static long serialVersionUID = 1l;
        private Long cellId;
        private VariableDefinition variableDefinition;
        private boolean dirty = false;

        public Long getCellId() {
            return cellId;
        }

        public void setCellId(Long cellId) {
            this.cellId = cellId;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void resetDirty() {
            dirty = false;
        }

        public VariableDefinition getVariableDefinition() {
            return variableDefinition;
        }

        public void setVariableDefinition(VariableDefinition variableDefinition) {
            this.variableDefinition = variableDefinition;
        }

        public String calculateKey() {
            return cellId + "." + variableDefinition.getName();
        }

    }
}
