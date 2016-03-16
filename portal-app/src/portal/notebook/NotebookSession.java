package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.notebook.client.CellClient;
import portal.SessionContext;
import portal.dataset.*;
import portal.notebook.api.*;
import portal.notebook.service.*;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

@SessionScoped
public class NotebookSession implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(NotebookSession.class);
    private final Map<Long, Map<UUID, MoleculeObject>> moleculeObjectMapMap = new HashMap<>();
    private final Map<Long, List<UUID>> uuidListMap = new HashMap<>();
    private final Map<Long, IDatasetDescriptor> datasetDescriptorMap = new HashMap<>();
    private long lastDatasetId = 0;
    private NotebookInstance currentNotebookInstance;
    private NotebookInfo currentNotebookInfo;
    private List<CellDefinition> cellDefinitionList;
    @Inject
    private NotebookService notebookService;
    @Inject
    private CellClient cellClient;
    @Inject
    private SessionContext sessionContext;
    @Inject
    private CellDefinitionRegistry cellDefinitionRegistry;

    public NotebookSession() {
        moleculeObjectMapMap.put(0L, new HashMap<>());
    }

    public NotebookInfo prepareDefaultNotebook() {
        List<NotebookInfo> list = notebookService.listNotebookInfo(sessionContext.getLoggedInUserDetails().getUserid());
        if (list.isEmpty()) {
            EditNotebookData data = new EditNotebookData();
            data.setName("Default notebook");
            data.setOwner(sessionContext.getLoggedInUserDetails().getUserid());
            data.setShared(false);
            Long id = notebookService.createNotebook(data);
            //System.out.println("Created notebook " + id);
            List<NotebookInfo> list2 = notebookService.listNotebookInfo(sessionContext.getLoggedInUserDetails().getUserid());
            //System.out.println("Now have " + list2.size() + "  notebooks");
            currentNotebookInfo = list2.get(0);
            NotebookInstance notebookInstance = new NotebookInstance();
            UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
            updateNotebookContentsData.setId(currentNotebookInfo.getId());
            updateNotebookContentsData.setNotebookInstance(notebookInstance);
            notebookService.updateNotebookContents(updateNotebookContentsData);
            list = notebookService.listNotebookInfo(sessionContext.getLoggedInUserDetails().getUserid());
        }
        return list.get(0);
    }

    public List<NotebookInfo> listNotebookInfo() {
        return notebookService.listNotebookInfo(sessionContext.getLoggedInUserDetails().getUserid());
    }

    public NotebookInfo retrieveNotebookInfo(Long id) {
        return notebookService.retrieveNotebookInfo(id);
    }

    public Long createNotebook(EditNotebookData editNotebookData) {
        return notebookService.createNotebook(editNotebookData);
    }

    public void updateNotebook(EditNotebookData editNotebookData) {
        notebookService.updateNotebook(editNotebookData);
        currentNotebookInfo = notebookService.retrieveNotebookInfo(editNotebookData.getId());
    }

    public void removeNotebook(Long notebookId) {
        notebookService.removeNotebook(notebookId);
        if (currentNotebookInfo != null && notebookId.equals(currentNotebookInfo.getId())) {
            currentNotebookInfo = null;
            currentNotebookInstance = null;
        }
    }

    public void loadCurrentNotebook(Long id) {
        currentNotebookInfo = notebookService.retrieveNotebookInfo(id);
        currentNotebookInstance = notebookService.findNotebookInstance(id);
    }

    public void reloadCurrentNotebook() {
        loadCurrentNotebook(currentNotebookInfo.getId());
    }

    public NotebookInstance getCurrentNotebookInstance() {
        return currentNotebookInstance;
    }

    public NotebookInfo getCurrentNotebookInfo() {
        return currentNotebookInfo;
    }

    public void storeCurrentNotebook() {
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(currentNotebookInfo.getId());
        updateNotebookContentsData.setNotebookInstance(currentNotebookInstance);
        notebookService.updateNotebookContents(updateNotebookContentsData);
        reloadCurrentNotebook();
    }

    public List<CellDefinition> listCellDefinition(CellDefinitionFilterData cellDefinitionFilterData) {
        List<CellDefinition> list = new ArrayList<>();
        Collection<CellDefinition> filteredCellDefinitions = listFilteredCellDefinition(cellDefinitionFilterData);
        list.addAll(filteredCellDefinitions);
        this.cellDefinitionList = list;
        return list;
    }

    private Collection<CellDefinition> listFilteredCellDefinition(CellDefinitionFilterData filter) {
        Collection<CellDefinition> filteredList = new ArrayList<>();
        for (CellDefinition cellDefinition : cellDefinitionRegistry.listCellDefinition()) {
            if (filter != null && filter.getPattern() != null) {
                if (cellDefinitionMatchesAllPatterns(cellDefinition, filter)) {
                    filteredList.add(cellDefinition);
                }
            } else {
                filteredList.add(cellDefinition);
            }
        }
        return filteredList;
    }

    private boolean cellDefinitionMatchesAllPatterns(CellDefinition cellDefinition, CellDefinitionFilterData filter) {
        boolean result = true;
        String[] patternList = filter.getPattern().split(" ");
        for (String pattern : patternList) {
            String cleanPattern = pattern.trim();
            if (!cellDefinitionMatchesPattern(cellDefinition, cleanPattern)) {
                result = false;
                break;
            }
        }
        return result;
    }

    private boolean cellDefinitionMatchesPattern(CellDefinition cellDefinition, String pattern) {
        boolean result = false;
        for (String tag : cellDefinition.getTags()) {
            if (tag.toLowerCase().startsWith(pattern.toLowerCase())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public List<UUID> listAllUuids(IDatasetDescriptor datasetDescriptor) {
        List<UUID> list = uuidListMap.get(datasetDescriptor.getId());
        if (list == null) {
            return new ArrayList<>();
        } else {
            return list;
        }
    }

    public IDatasetDescriptor createDatasetFromMolecules(List<MoleculeObject> list, String name) {
        Map<UUID, MoleculeObject> objectMap = new HashMap<>();
        List<UUID> uuidList = new ArrayList<>();
        for (MoleculeObject moleculeObject : list) {
            objectMap.put(moleculeObject.getUUID(), moleculeObject);
            uuidList.add(moleculeObject.getUUID());
        }
        Long datasetId = nextDatasetId();
        moleculeObjectMapMap.put(datasetId, objectMap);
        uuidListMap.put(datasetId, uuidList);

        TableDisplayDatasetDescriptor datasetDescriptor = new TableDisplayDatasetDescriptor(datasetId, name, list.size());

        RowDescriptor rowDescriptor = new RowDescriptor();

        PropertyDescriptor structurePropertyDescriptor = new PropertyDescriptor();
        structurePropertyDescriptor.setDescription("Structure property");
        structurePropertyDescriptor.setId(1l);
        rowDescriptor.addPropertyDescriptor(structurePropertyDescriptor);
        rowDescriptor.setStructurePropertyId(structurePropertyDescriptor.getId());
        rowDescriptor.setHierarchicalPropertyId(structurePropertyDescriptor.getId());
        long propertyCount = 1;
        Set<String> keySet = new HashSet<>();
        for (MoleculeObject moleculeObject : list) {
            for (String key : moleculeObject.getValues().keySet()) {
                if (!keySet.contains(key)) {
                    propertyCount++;
                    PropertyDescriptor plainPropertyDescriptor = new PropertyDescriptor();
                    plainPropertyDescriptor.setDescription(key);
                    plainPropertyDescriptor.setId(propertyCount);
                    rowDescriptor.addPropertyDescriptor(plainPropertyDescriptor);
                    keySet.add(key);
                }
            }
        }
        datasetDescriptor.addRowDescriptor(rowDescriptor);
        datasetDescriptorMap.put(datasetDescriptor.getId(), datasetDescriptor);
        return datasetDescriptor;
    }

    public IDatasetDescriptor createDatasetFromStrings(Strings value, String name) {
        List<MoleculeObject> list = new ArrayList<>();
        for (String smile : value.getStrings()) {
            MoleculeObject moleculeObject = new MoleculeObject(smile);
            list.add(moleculeObject);
        }
        return createDatasetFromMolecules(list, name);
    }

    public IDatasetDescriptor loadDatasetFromSquonkDataset(VariableInstance inputVariableInstance) {
        try {
            CellInstance producerCell = currentNotebookInstance.findCellById(inputVariableInstance.getCellId());
            List<MoleculeObject> list = notebookService.squonkDatasetAsMolecules(currentNotebookInfo.getId(), producerCell.getName(), inputVariableInstance.getVariableDefinition().getName());
            return createDatasetFromMolecules(list, producerCell.getName() + "." + inputVariableInstance.getVariableDefinition().getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public IDatasetDescriptor loadDatasetFromFile(String fileName) {
        try {
            List<MoleculeObject> list = notebookService.parseFile(fileName);
            return createDatasetFromMolecules(list, fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private synchronized Long nextDatasetId() {
        lastDatasetId++;
        return lastDatasetId;
    }

    public List<IRow> listRow(IDatasetDescriptor datasetDescriptor, List<UUID> uuidList) {
        Map<UUID, MoleculeObject> datasetContents = moleculeObjectMapMap.get(datasetDescriptor.getId());

        if (datasetContents.isEmpty()) {
            return new ArrayList<>();
        }

        RowDescriptor rowDescriptor = (RowDescriptor) datasetDescriptor.getAllRowDescriptors().get(0);
        PropertyDescriptor structurePropertyDescriptor = (PropertyDescriptor) rowDescriptor.getStructurePropertyDescriptor();

        List<IRow> result = new ArrayList<>();
        for (UUID uuid : uuidList) {
            MoleculeObject molecule = datasetContents.get(uuid);
            Row row = new Row();
            row.setUuid(molecule.getUUID());
            row.setDescriptor(rowDescriptor);
            row.setProperty(structurePropertyDescriptor, molecule.getSource());

            for (IPropertyDescriptor propertyDescriptor : rowDescriptor.listAllPropertyDescriptors()) {
                if (!propertyDescriptor.getId().equals(structurePropertyDescriptor.getId())) {
                    row.setProperty((PropertyDescriptor) propertyDescriptor, molecule.getValue(propertyDescriptor.getDescription()));
                }
            }

            result.add(row);
        }
        return result;
    }

    public IDatasetDescriptor findDatasetDescriptorById(Long datasetDescriptorId) {
        return datasetDescriptorMap.get(datasetDescriptorId);
    }


    public void executeCell(Long cellId) {
        storeCurrentNotebook();
        CellInstance cell = currentNotebookInstance.findCellById(cellId);
        if (cell.getCellDefinition().getExecutable()) {
            notebookService.executeCell(currentNotebookInfo.getId(), cellId);
        }
    }

    public CellDefinition findCellType(String cellName) {
        CellDefinition result = null;
        for (CellDefinition cellDefinition : cellDefinitionList) {
            if (cellDefinition.getName().equals(cellName)) {
                result = cellDefinition;
                break;
            }
        }
        return result;
    }

    public Execution findExecution(Long cellId) {
        return notebookService.findExecution(currentNotebookInfo.getId(), cellId);
    }

    public void storeTemporaryFileForVariable(VariableInstance variableInstance, InputStream inputStream) {
        notebookService.storeTemporaryFileForVariable(currentNotebookInfo.getId(), variableInstance, inputStream);
    }

    public void commitFileForVariable(VariableInstance variableInstance) {
        notebookService.commitFileForVariable(currentNotebookInfo.getId(), variableInstance);

    }

}

