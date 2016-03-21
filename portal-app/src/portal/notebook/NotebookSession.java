package portal.notebook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.client.NotebookClient;
import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.client.CellClient;
import portal.SessionContext;
import portal.dataset.IDatasetDescriptor;
import portal.dataset.IRow;
import portal.notebook.api.*;
import portal.notebook.service.Execution;
import portal.notebook.service.ExecutionService;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

@SessionScoped
public class NotebookSession implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookSession.class);
    private NotebookInstance currentNotebookInstance;
    private NotebookDescriptor currentNotebookDescriptor;
    private NotebookEditable currentNotebookEditable;
    private List<CellDefinition> cellDefinitionList;
    @Inject
    private NotebookClient notebookClient;
    @Inject
    private CellClient cellClient;
    @Inject
    private SessionContext sessionContext;
    @Inject
    private CellDefinitionRegistry cellDefinitionRegistry;
    @Inject
    private ExecutionService executionService;
    @Inject
    private Datasets datasets;

    public List<NotebookDescriptor> listNotebookDescriptor() {
        try {
            return notebookClient.listNotebooks(sessionContext.getLoggedInUserDetails().getUserid());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public NotebookDescriptor findNotebookDescriptor(Long id) {
        List<NotebookDescriptor> descriptors = listNotebookDescriptor();
        for (NotebookDescriptor notebookDescriptor : descriptors) {
            if (notebookDescriptor.getId().equals(id)) {
                return notebookDescriptor;
            }
        }
        return null;
    }

    public Long createNotebook(String name, String description) {
        try {
            NotebookDescriptor notebookDescriptor = notebookClient.createNotebook(sessionContext.getLoggedInUserDetails().getUserid(), name, description);
            return notebookDescriptor.getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateNotebook(Long id, String name, String description) {
        try {
            notebookClient.updateNotebook(id, name, description);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeNotebook(Long notebookId) {

    }

    public NotebookEditable findLastNotebookEditable(Long descriptorId) {
        try {
            List<NotebookEditable> editables = notebookClient.listEditables(descriptorId, sessionContext.getLoggedInUserDetails().getUserid());
            return editables.isEmpty() ? null : editables.get(editables.size() - 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadCurrentNotebook(Long id) {
        try {
            currentNotebookDescriptor = findNotebookDescriptor(id);
            currentNotebookEditable = findLastNotebookEditable(currentNotebookDescriptor.getId());
            if (currentNotebookEditable == null) {
                currentNotebookInstance = new NotebookInstance();
            } else {
                currentNotebookInstance = new ObjectMapper().readValue(currentNotebookEditable.getContent(), NotebookInstance.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadCurrentNotebook() {
        loadCurrentNotebook(currentNotebookDescriptor.getId());
    }

    public NotebookInstance getCurrentNotebookInstance() {
        return currentNotebookInstance;
    }

    public NotebookDescriptor getCurrentNotebookDescriptor() {
        return currentNotebookDescriptor;
    }

    public void storeCurrentNotebook() {
        try {
            String json = currentNotebookInstance.toJsonString();
            if (currentNotebookEditable == null) {
                notebookClient.createEditable(currentNotebookDescriptor.getId(), null, json);
            } else {
                notebookClient.updateEditable(currentNotebookDescriptor.getId(), currentNotebookEditable.getId(), json);
            }
            reloadCurrentNotebook();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public void executeCell(Long cellId) {
        storeCurrentNotebook();
        CellInstance cell = currentNotebookInstance.findCellById(cellId);
        if (cell.getCellDefinition().getExecutable()) {
            executionService.executeCell(currentNotebookInstance, currentNotebookDescriptor.getId(), cellId);
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
        return executionService.findExecution(currentNotebookDescriptor.getId(), cellId);
    }

    public void storeTemporaryFileForVariable(VariableInstance variableInstance, InputStream inputStream) {

    }

    public void commitFileForVariable(VariableInstance variableInstance) {

    }

    public IDatasetDescriptor loadDatasetFromVariable(VariableInstance variableInstance) {
        VariableType variableType = variableInstance.getVariableDefinition().getVariableType();
        if (variableType.equals(VariableType.DATASET)) {
            return loadDatasetFromDatasetVariable(variableInstance);
        } else if (variableType.equals(VariableType.FILE)) {
            return loadDatasetFromFileVariable(variableInstance);
        } else if (variableType.equals(VariableType.STRING)) {
            return loadDatasetFromFileVariable(variableInstance);
        } else {
            return null;
        }
    }

    public IDatasetDescriptor loadDatasetFromFileVariable(VariableInstance variableInstance) {
        try {
            List<MoleculeObject> list = parseMoleculesFromFileVariable(variableInstance);
            return datasets.createDatasetFromMolecules(list, variableInstance.getCellId() + "." + variableInstance.getVariableDefinition().getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<MoleculeObject> parseMoleculesFromFileVariable(VariableInstance variableInstance) throws Exception {
        String fileName = notebookClient.readTextValue(currentNotebookDescriptor.getId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);
        int x = fileName.lastIndexOf(".");
        String ext = fileName.toLowerCase().substring(x + 1);
        InputStream inputStream = notebookClient.readStreamValue(currentNotebookDescriptor.getId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);
        try {
            if (ext.equals("json")) {
                return Datasets.parseJson(inputStream);
            } else if (ext.equals("tab")) {
                return Datasets.parseTsv(inputStream);
            } else {
                LOGGER.warn("Unrecognised format: " + ext);
                return new ArrayList<>();
            }
        } finally {
            inputStream.close();
        }
    }

    public IDatasetDescriptor loadDatasetFromDatasetVariable(VariableInstance variableInstance) {
        try {
            List<MoleculeObject> list = squonkDatasetAsMolecules(variableInstance);
            return datasets.createDatasetFromMolecules(list, variableInstance.getCellId() + "." + variableInstance.getVariableDefinition().getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<MoleculeObject> squonkDatasetAsMolecules(VariableInstance variableInstance) {
        try {
            InputStream inputStreaam = notebookClient.readStreamValue(currentNotebookDescriptor.getId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);
            try {
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStreaam);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(gzipInputStream, new TypeReference<List<MoleculeObject>>() {
                });
            } finally {
                inputStreaam.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public List<UUID> listAllDatasetUuids(IDatasetDescriptor datasetDescriptor) {
        return datasets.listAllDatasetUuids(datasetDescriptor);
    }

    public List<IRow> listDatasetRow(IDatasetDescriptor datasetDescriptor, List<UUID> uuidList) {
        return datasets.listdatasetRow(datasetDescriptor, uuidList);
    }

    public IDatasetDescriptor findDatasetDescriptorById(Long datasetDescriptorId) {
        return datasets.findDatasetDescriptorById(datasetDescriptorId);
    }
}

