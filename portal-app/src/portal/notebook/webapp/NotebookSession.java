package portal.notebook.webapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.client.NotebookVariableClient;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import portal.SessionContext;
import portal.notebook.api.*;
import portal.notebook.service.Execution;
import portal.notebook.service.PortalService;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private NotebookInfo currentNotebookInfo;
    private Long currentNotebookEditableId;
    private List<CellDefinition> cellDefinitionList;
    @Inject
    private NotebookVariableClient notebookVariableClient;
    @Inject
    private SessionContext sessionContext;
    @Inject
    private CellDefinitionRegistry cellDefinitionRegistry;
    @Inject
    private PortalService portalService;
    @Inject
    private Datasets datasets;

    public List<NotebookInfo> listNotebookInfo() throws Exception {
        List<NotebookInfo> list = new ArrayList<>();
        for (NotebookDTO notebookDescriptor : notebookVariableClient.listNotebooks(sessionContext.getLoggedInUserDetails().getUserid())) {
            NotebookInfo notebookInfo = NotebookInfo.fromNotebookDTO(notebookDescriptor);
            list.add(notebookInfo);
        }
        return list;
    }

    public NotebookInfo findNotebookInfo(Long id) throws Exception {
        for (NotebookDTO notebookDTO : notebookVariableClient.listNotebooks(sessionContext.getLoggedInUserDetails().getUserid())) {
            if (notebookDTO.getId().equals(id)) {
                throw new Exception("Test");
                //return NotebookInfo.fromNotebookDTO(notebookDTO);
            }
        }
        return null;
    }

    public Long createNotebook(String name, String description, Boolean shared) throws Exception {
        NotebookDTO notebookDTO = notebookVariableClient.createNotebook(sessionContext.getLoggedInUserDetails().getUserid(), name, description);
        notebookVariableClient.createEditable(notebookDTO.getId(), null, sessionContext.getLoggedInUserDetails().getUserid());
        if (shared) {
            notebookVariableClient.addNotebookToLayer(notebookDTO.getId(), "public");
        }
        return notebookDTO.getId();
    }

    public void updateNotebook(Long id, String name, String description, Boolean shared) throws Exception {
        notebookVariableClient.updateNotebook(id, name, description);
        notebookVariableClient.removeNotebookFromLayer(id, "public");
        if (shared) {
            notebookVariableClient.addNotebookToLayer(id, "public");
        }
    }

    public void removeNotebook(Long notebookId) throws Exception {
        notebookVariableClient.deleteNotebook(notebookId);
    }

    private NotebookEditableDTO findDefaultNotebookEditable(Long descriptorId) throws Exception {
        List<NotebookEditableDTO> editables = notebookVariableClient.listEditables(descriptorId, sessionContext.getLoggedInUserDetails().getUserid());
        return editables.isEmpty() ? null : editables.get(0);
    }

    public void loadCurrentNotebook(Long id) throws Exception {
        currentNotebookInfo = findNotebookInfo(id);
        NotebookEditableDTO currentNotebookEditable = findDefaultNotebookEditable(currentNotebookInfo.getId());
        if (currentNotebookEditable == null) {
            currentNotebookInstance = new NotebookInstance();
            currentNotebookEditableId = null;
        } else {
            NotebookCanvasDTO notebookCanvasDTO = currentNotebookEditable.getCanvasDTO();
            currentNotebookInstance = new NotebookInstance();
            if (notebookCanvasDTO != null) {
                currentNotebookInstance.loadNotebookCanvasDTO(notebookCanvasDTO, cellDefinitionRegistry);
            }
            if (currentNotebookInstance == null) { // is this needed?
                currentNotebookInstance = new NotebookInstance();
            }
            currentNotebookEditableId = currentNotebookEditable.getId();
        }
    }

    public void reloadCurrentNotebook() throws Exception {
        loadCurrentNotebook(currentNotebookInfo.getId());
    }

    public NotebookInstance getCurrentNotebookInstance() {
        return currentNotebookInstance;
    }

    public NotebookInfo getCurrentNotebookInfo() {
        return currentNotebookInfo;
    }

    public void storeCurrentNotebook() throws Exception {
        if (currentNotebookEditableId == null) {
            NotebookEditableDTO currentNotebookEditable = notebookVariableClient.createEditable(currentNotebookInfo.getId(), null, sessionContext.getLoggedInUserDetails().getUserid());
            currentNotebookEditableId = currentNotebookEditable.getId();
        } else {
            NotebookCanvasDTO notebookCanvasDTO = new NotebookCanvasDTO(currentNotebookInstance.getLastCellId());
            currentNotebookInstance.storeNotebookCanvasDTO(notebookCanvasDTO);
            notebookVariableClient.updateEditable(currentNotebookInfo.getId(), currentNotebookEditableId, notebookCanvasDTO);
        }
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

    public void executeCell(Long cellId) throws Exception {
        storeCurrentNotebook();
        CellInstance cell = currentNotebookInstance.findCellInstanceById(cellId);
        if (cell.getCellDefinition().getExecutable()) {
            portalService.executeCell(currentNotebookInstance, currentNotebookInfo.getId(), currentNotebookEditableId, cellId);
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
        return portalService.findExecution(currentNotebookInfo.getId(), cellId);
    }


    public IDatasetDescriptor loadDatasetFromVariable(VariableInstance variableInstance) throws Exception {
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

    public IDatasetDescriptor loadDatasetFromFileVariable(VariableInstance variableInstance) throws Exception {
        List<MoleculeObject> list = parseMoleculesFromFileVariable(variableInstance);
        return datasets.createDatasetFromMolecules(list, variableInstance.getCellId() + "." + variableInstance.getVariableDefinition().getName());
    }

    public List<MoleculeObject> parseMoleculesFromFileVariable(VariableInstance variableInstance) throws Exception {
        String fileName = notebookVariableClient.readTextValue(currentNotebookInfo.getId(), currentNotebookEditableId, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
        int x = fileName.lastIndexOf(".");
        String ext = fileName.toLowerCase().substring(x + 1);
        InputStream inputStream = notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), currentNotebookEditableId, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
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

    public IDatasetDescriptor loadDatasetFromDatasetVariable(VariableInstance variableInstance) throws Exception {
        List<MoleculeObject> list = squonkDatasetAsMolecules(variableInstance);
        return datasets.createDatasetFromMolecules(list, variableInstance.getCellId() + "." + variableInstance.getVariableDefinition().getName());
    }

    public List<MoleculeObject> squonkDatasetAsMolecules(VariableInstance variableInstance) throws Exception {
        try (InputStream inputStream = notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), currentNotebookEditableId, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null)) {
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(gzipInputStream, new TypeReference<List<MoleculeObject>>() {
            });
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

    public void writeTextValue(VariableInstance variableInstance, Object value) throws Exception {
        notebookVariableClient.writeTextValue(currentNotebookInfo.getId(), currentNotebookEditableId, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), value == null ? null : value.toString());
    }

    public String readTextValue(VariableInstance variableInstance) throws Exception {
        return notebookVariableClient.readTextValue(currentNotebookInfo.getId(), currentNotebookEditableId, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
    }

    public void writeStreamValue(VariableInstance variableInstance, InputStream inputStream) throws Exception {
        notebookVariableClient.writeStreamValue(currentNotebookInfo.getId(), currentNotebookEditableId, variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), inputStream);
    }

    public void resetCurrentNotebook() {
        currentNotebookEditableId = null;
        currentNotebookInstance = null;
        currentNotebookInfo = null;
    }

    public MoleculeObject findMoleculeObjectByRow(Long datasetDescriptorId, UUID uuid) {
        return datasets.findMoleculeObject(datasetDescriptorId, uuid);
    }

    public void writeMoleculeValue(VariableInstance variableInstance, MoleculeObject moleculeObject) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(byteArrayOutputStream, moleculeObject);
        byteArrayOutputStream.flush();
        writeTextValue(variableInstance, new String(byteArrayOutputStream.toByteArray()));
    }

    public MoleculeObject readMoleculeValue(VariableInstance variableInstance) throws Exception {
        String json = readTextValue(variableInstance);
        return new ObjectMapper().readValue(json, MoleculeObject.class);
    }

}

