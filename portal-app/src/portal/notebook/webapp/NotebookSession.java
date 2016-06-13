package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.client.NotebookVariableClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.notebook.api.*;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import portal.SessionContext;
import portal.notebook.api.*;
import portal.notebook.service.Execution;
import portal.notebook.service.PortalService;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SessionScoped
public class NotebookSession implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookSession.class);
    private final DateFormat versionDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private NotebookInstance currentNotebookInstance;
    private NotebookInfo currentNotebookInfo;
    private Long currentNotebookVersionId;
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
        LOGGER.info("list");
        List<NotebookInfo> list = new ArrayList<>();
        for (NotebookDTO notebookDTO : notebookVariableClient.listNotebooks(sessionContext.getLoggedInUserDetails().getUserid())) {
            NotebookInfo notebookInfo = NotebookInfo.fromNotebookDTO(notebookDTO);
            list.add(notebookInfo);
        }
        return list;
    }

    public NotebookInfo findNotebookInfo(Long id) throws Exception {
        for (NotebookDTO notebookDTO : notebookVariableClient.listNotebooks(sessionContext.getLoggedInUserDetails().getUserid())) {
            if (notebookDTO.getId().equals(id)) {
                return NotebookInfo.fromNotebookDTO(notebookDTO);
            }
        }
        return null;
    }

    public Long createNotebook(String name, String description, Boolean shared) throws Exception {
        NotebookDTO notebookDTO = notebookVariableClient.createNotebook(sessionContext.getLoggedInUserDetails().getUserid(), name, description);
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

    public NotebookEditableDTO findDefaultNotebookEditable(Long notebookId) throws Exception {
        List<NotebookEditableDTO> editables = notebookVariableClient.listEditables(notebookId, sessionContext.getLoggedInUserDetails().getUserid());
        return editables.isEmpty() ? null : editables.get(0);
    }

    public void loadCurrentNotebook(Long id) throws Exception {
        currentNotebookInfo = findNotebookInfo(id);
        NotebookEditableDTO editable = findDefaultNotebookEditable(currentNotebookInfo.getId());
        if (editable == null && !currentNotebookInfo.getOwner().equals(sessionContext.getLoggedInUserDetails().getUserid())) {
            NotebookSavepointDTO savepoint = notebookVariableClient.listSavepoints(currentNotebookInfo.getId()).get(0);
            editable = notebookVariableClient.createEditable(currentNotebookInfo.getId(), savepoint.getId(), sessionContext.getLoggedInUserDetails().getUserid());
        }
        if (editable == null) {
            throw new Exception("No default editable");
        }
        loadCurrentVersion(editable.getId());
    }


    public void loadCurrentVersion(Long versionId) throws Exception {
        Map<Long, AbstractNotebookVersionDTO> map = buildCurrentNotebookVersionMap();
        AbstractNotebookVersionDTO versionDTO = map.get(versionId);
        if (versionDTO instanceof NotebookSavepointDTO) {
                versionDTO = notebookVariableClient.createEditable(currentNotebookInfo.getId(), versionId, sessionContext.getLoggedInUserDetails().getUserid());
        }
        currentNotebookInstance = new NotebookInstance();
        currentNotebookInstance.loadNotebookCanvasDTO(versionDTO.getCanvasDTO(), cellDefinitionRegistry);
        currentNotebookInstance.setEditable(versionDTO instanceof NotebookEditableDTO);
        currentNotebookInstance.setVersionDescription(buildVersionDescription(versionDTO));
        setCurrentNotebookVersionId(versionDTO.getId());
    }

    private String buildVersionDescription(AbstractNotebookVersionDTO versionDTO) {
        if (versionDTO instanceof NotebookEditableDTO) {
            return versionDateFormat.format(versionDTO.getCreatedDate());
        } else {
            return ((NotebookSavepointDTO)versionDTO).getDescription();
        }
    }

    public void reloadCurrentVersion() throws Exception {
        loadCurrentVersion(getCurrentNotebookVersionId());
    }

    public NotebookInstance getCurrentNotebookInstance() {
        return currentNotebookInstance;
    }

    public NotebookInfo getCurrentNotebookInfo() {
        return currentNotebookInfo;
    }

    public void storeCurrentEditable() throws Exception {
        if (currentNotebookInstance.isEditable()) {
            doStoreCurrentEditable();
            reloadCurrentVersion();
        } else {
            LOGGER.warn("Not editable version");
        }
    }

    private void doStoreCurrentEditable() throws Exception {
        NotebookCanvasDTO notebookCanvasDTO = new NotebookCanvasDTO(currentNotebookInstance.getLastCellId());
        currentNotebookInstance.storeNotebookCanvasDTO(notebookCanvasDTO);
        notebookVariableClient.updateEditable(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), notebookCanvasDTO);
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
        storeCurrentEditable();
        CellInstance cell = currentNotebookInstance.findCellInstanceById(cellId);
        if (cell.getCellDefinition().getExecutable()) {
            portalService.executeCell(currentNotebookInstance, currentNotebookInfo.getId(), getCurrentNotebookVersionId(), cellId);
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
        String fileName = notebookVariableClient.readTextValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
        int x = fileName.lastIndexOf(".");
        String ext = fileName.toLowerCase().substring(x + 1);
        InputStream inputStream = notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
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

        String metaJson = notebookVariableClient.readTextValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);
        DatasetMetadata meta = JsonHandler.getInstance().objectFromJson(metaJson, DatasetMetadata.class);
        try (InputStream inputStream = notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null)) {
            InputStream gunzippedInputStream = IOUtils.getGunzippedInputStream(inputStream);
            Dataset<MoleculeObject> dataset = new Dataset<>(MoleculeObject.class, gunzippedInputStream, meta);
            return dataset.getItems();
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
        if (currentNotebookInstance.isEditable()) {
            notebookVariableClient.writeTextValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), value == null ? null : value.toString());
        }
    }

    public String readTextValue(VariableInstance variableInstance) throws Exception {
        return notebookVariableClient.readTextValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
    }

    public void writeStreamValue(VariableInstance variableInstance, InputStream inputStream) throws Exception {
        if (currentNotebookInstance.isEditable()) {
            notebookVariableClient.writeStreamValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), inputStream);
        }
    }

    public void resetCurrentNotebook() {
        setCurrentNotebookVersionId(null);
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

    private Map<Long, AbstractNotebookVersionDTO> buildCurrentNotebookVersionMap() throws Exception {
        Map<Long, AbstractNotebookVersionDTO> versionMap = new HashMap<>();
        List<NotebookEditableDTO> editableList = notebookVariableClient.listEditables(currentNotebookInfo.getId(), sessionContext.getLoggedInUserDetails().getUserid());
        for (NotebookEditableDTO dto : editableList) {
            versionMap.put(dto.getId(), dto);
        }
        List<NotebookSavepointDTO> savepointList = notebookVariableClient.listSavepoints(currentNotebookInfo.getId());
        for (NotebookSavepointDTO dto : savepointList) {
            versionMap.put(dto.getId(), dto);
        }
        return versionMap;
    }

    public HistoryTree buildCurrentNotebookHistoryTree() throws Exception {
        Map<Long, AbstractNotebookVersionDTO> versionMap = buildCurrentNotebookVersionMap();
        HistoryTree tree = new HistoryTree();
        tree.setName(currentNotebookInfo.getName());
        tree.loadVersionMap(versionMap);
        return tree;
    }

    public void createSavepointFromCurrentEditable(String description) throws Exception {
        NotebookEditableDTO editable = notebookVariableClient.createSavepoint(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), description);
        loadCurrentVersion(editable.getId());
    }

    public void createEditableFromCurrentSavePoint() throws Exception {

        NotebookEditableDTO editable = notebookVariableClient.createEditable(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), sessionContext.getLoggedInUserDetails().getUserid());
        loadCurrentVersion(editable.getId());
    }

    public Long getCurrentNotebookVersionId() {
        return currentNotebookVersionId;
    }

    public void setCurrentNotebookVersionId(Long currentNotebookVersionId) {
        this.currentNotebookVersionId = currentNotebookVersionId;
    }

    public boolean hasSavepoints(Long id) throws Exception {
        return !notebookVariableClient.listSavepoints(id).isEmpty();
    }
}

