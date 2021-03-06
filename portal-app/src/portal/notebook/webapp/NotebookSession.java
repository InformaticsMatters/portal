package portal.notebook.webapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.client.NotebookVariableClient;
import org.squonk.core.client.StructureIOClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.*;
import org.squonk.types.AbstractStreamType;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SessionScoped
public class NotebookSession implements Serializable {

    private static final Logger LOG = Logger.getLogger(NotebookSession.class.getName());
    private final DateFormat versionDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private NotebookInstance currentNotebookInstance;
    private NotebookInfo currentNotebookInfo;
    private Long currentNotebookVersionId;
    private Collection<CellDefinition> cellDefinitionList;
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
    @Inject
    private StructureIOClient structureIOClient;

    public List<NotebookInfo> listNotebookInfo() throws Exception {
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
        LOG.fine("Loading notebook instance " + versionDTO.getNotebookId() + "/" + versionId);
        currentNotebookInstance = new NotebookInstance(this);
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
            LOG.warning("Not editable version");
        }
    }

    private void doStoreCurrentEditable() throws Exception {
        NotebookCanvasDTO notebookCanvasDTO = new NotebookCanvasDTO(currentNotebookInstance.getLastCellId());
        currentNotebookInstance.storeNotebookCanvasDTO(notebookCanvasDTO);
        notebookVariableClient.updateEditable(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), notebookCanvasDTO);
    }

    public List<CellDefinition> listCellDefinition(CellDefinitionFilterData cellDefinitionFilterData) {
        if (cellDefinitionList == null) {
            // first time in the session we need to load the list of cells
            cellDefinitionList = cellDefinitionRegistry.getCellDefinitions();
        }
        Collection<CellDefinition> filteredCellDefinitions = listFilteredCellDefinition(cellDefinitionFilterData);
        List<CellDefinition> results = new ArrayList<>();
        results.addAll(filteredCellDefinitions);
        return results;
    }

    private Collection<CellDefinition> listFilteredCellDefinition(CellDefinitionFilterData filter) {
        Collection<CellDefinition> filteredList = new ArrayList<>();
        for (CellDefinition cellDefinition : cellDefinitionList) {
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

    /** Find the cell by its 'name' property. This name is the name associated with the cell definition, not the display name
     * that has been assigned to a cell instance.
     *
     * @param cellName
     * @return
     */
    public CellDefinition findCellByName(String cellName) {
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
        IODescriptor iod = variableInstance.getVariableDefinition();
        if (Dataset.class.isAssignableFrom(iod.getPrimaryType())) {
            return loadDatasetFromDatasetVariable(variableInstance);
        } else if (AbstractStreamType.class.isAssignableFrom(iod.getPrimaryType())) {
            return loadDatasetFromFileVariable(variableInstance);
        } else if (iod.getPrimaryType() == String.class) {
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
                LOG.warning("Unrecognised format: " + ext);
                return new ArrayList<>();
            }
        } finally {
            inputStream.close();
        }
    }

    public IDatasetDescriptor loadDatasetFromDatasetVariable(VariableInstance variableInstance) throws Exception {
        List<MoleculeObject> list = squonkDatasetAsMolecules(variableInstance);
        LOG.fine("Read dataset of size " + list.size());
        return datasets.createDatasetFromMolecules(list, variableInstance.getCellId() + "." + variableInstance.getVariableDefinition().getName());
    }

    /**
     * @deprecated Use squonkDataset() instead as it provides more goodies and handles things other than molecules.
     */
    @Deprecated()
    public List<MoleculeObject> squonkDatasetAsMolecules(VariableInstance variableInstance) throws Exception {

        String metaJson = notebookVariableClient.readTextValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);
        DatasetMetadata<MoleculeObject> meta = JsonHandler.getInstance().objectFromJson(metaJson, new TypeReference<DatasetMetadata<MoleculeObject>>() {});
        try (InputStream inputStream = notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null)) {
            InputStream gunzippedInputStream = IOUtils.getGunzippedInputStream(inputStream);
            Dataset<MoleculeObject> dataset = new Dataset<>(gunzippedInputStream, meta);
            return dataset.getItems();
        }
    }

    @SuppressWarnings("unchecked")
    public DatasetMetadata squonkDatasetMetadata(VariableInstance variableInstance) throws Exception {
        String metaJson = notebookVariableClient.readTextValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);

        if (metaJson == null) {
            return null;
        } else {
            return JsonHandler.getInstance().objectFromJson(metaJson, DatasetMetadata.class);
        }
    }

    /** Retrieve Dataset for this variable.
     * This is based on an InputStream that is read, and must be closed once finished e.g. by closing the Stream.
     *
     * @param variableInstance
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public Dataset<? extends BasicObject> squonkDataset(VariableInstance variableInstance) throws Exception {

        String metaJson = notebookVariableClient.readTextValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);
        if (metaJson == null) {
            return null;
        }
        DatasetMetadata meta = JsonHandler.getInstance().objectFromJson(metaJson, DatasetMetadata.class);
        InputStream inputStream = notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName(), null);
        InputStream gunzippedInputStream = IOUtils.getGunzippedInputStream(inputStream);
        return new Dataset<>(gunzippedInputStream, meta);
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

    public InputStream readStreamValue(VariableInstance variableInstance) throws Exception {
        return notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
    }

    public <T> T readStreamValueAs(VariableInstance variableInstance, Class<T> type) throws Exception {
        try {
            Constructor<T> constr = type.getConstructor(InputStream.class);
            InputStream is = notebookVariableClient.readStreamValue(currentNotebookInfo.getId(), getCurrentNotebookVersionId(), variableInstance.getCellId(), variableInstance.getVariableDefinition().getName());
            return constr.newInstance(is);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException| IllegalArgumentException| InvocationTargetException e) {
            LOG.log(Level.WARNING, "Failed to instantiate " + type.getSimpleName(), e);
        }
        return null;
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

    public StructureIOClient getStructureIOClient() {
        // we might allow which client is used (e.g. CDK vs. ChemAxon) to be customised by the user
        // for now we just have one
        return structureIOClient;
    }
}

