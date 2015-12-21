package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.client.CellClient;
import portal.dataset.*;
import portal.notebook.service.*;
import toolkit.services.Transactional;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

@SessionScoped
@Transactional
public class NotebookSession implements Serializable {

    private final Map<Long, Map<UUID, MoleculeObject>> fileObjectsMap = new HashMap<>();
    private final Map<Long, IDatasetDescriptor> datasetDescriptorMap = new HashMap<>();
    private long lastDatasetId = 0;
    @Inject
    private NotebookService notebookService;
    @Inject
    private CellClient cellClient;
    private NotebookModel currentNotebookModel;
    private NotebookInfo currentNotebookInfo;

    public NotebookSession() {
        fileObjectsMap.put(0l, new HashMap<>());
    }

    public NotebookInfo preparePocNotebook() {
        List<NotebookInfo> list = notebookService.listNotebookInfo();
        if (list.isEmpty()) {
            EditNotebookData notebookData = new EditNotebookData();
            notebookData.setName("POC");
            notebookService.createNotebook(notebookData);
            currentNotebookInfo = notebookService.listNotebookInfo().get(0);
            NotebookContents notebookContents = new NotebookContents();
            UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
            updateNotebookContentsData.setId(currentNotebookInfo.getId());
            updateNotebookContentsData.setNotebookContents(notebookContents);
            notebookService.updateNotebookContents(updateNotebookContentsData);
            list = notebookService.listNotebookInfo();
        }
        return list.get(0);
    }

    public List<NotebookInfo> listNotebookInfo() {
        return notebookService.listNotebookInfo();
    }

    public NotebookInfo retrieveNotebookInfo(Long id) {
        return notebookService.retrieveNotebookInfo(id);
    }

    public Long createNotebook(EditNotebookData editNotebookData) {
        return notebookService.createNotebook(editNotebookData);
    }

    public void updateNotebook(EditNotebookData editNotebookData) {
        notebookService.updateNotebook(editNotebookData);
    }

    public void removeNotebook(Long notebookId) {
        notebookService.removeNotebook(notebookId);
        if (currentNotebookInfo != null && notebookId.equals(currentNotebookInfo.getId())) {
            currentNotebookInfo = null;
            currentNotebookModel = null;
        }
    }

    public void loadCurrentNotebook(Long id) {
        currentNotebookInfo = notebookService.retrieveNotebookInfo(id);
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(id);
        currentNotebookModel = new NotebookModel(notebookContents);
    }

    public void reloadCurrentNotebook() {
        loadCurrentNotebook(currentNotebookInfo.getId());
    }

    public NotebookModel getCurrentNotebookModel() {
        return currentNotebookModel;
    }

    public NotebookInfo getCurrentNotebookInfo() {
        return currentNotebookInfo;
    }

    public void storeCurrentNotebook() {
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(currentNotebookInfo.getId());
        updateNotebookContentsData.setNotebookContents(currentNotebookModel.getNotebookContents());
        notebookService.updateNotebookContents(updateNotebookContentsData);
    }

    public CellModel addCell(CellType cellType, int x, int y) {
        Cell cell = currentNotebookModel.getNotebookContents().addCell(cellType);
        cell.setPositionTop(y);
        cell.setPositionLeft(x);
        CellModel cellModel = currentNotebookModel.addCellModel(cell);
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(currentNotebookInfo.getId());
        updateNotebookContentsData.setNotebookContents(currentNotebookModel.getNotebookContents());
        notebookService.updateNotebookContents(updateNotebookContentsData);
        return cellModel;
    }

    public void removeCell(CellModel cellModel) {
        currentNotebookModel.removeCellModel(cellModel);
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(currentNotebookInfo.getId());
        updateNotebookContentsData.setNotebookContents(currentNotebookModel.getNotebookContents());
        notebookService.updateNotebookContents(updateNotebookContentsData);
    }

    public List<CellType> listCellType() {
        return cellClient.listCellType();
    }


    public List<UUID> listAllUuids(IDatasetDescriptor datasetDescriptor) {
        Map<UUID, MoleculeObject> map = fileObjectsMap.get(datasetDescriptor.getId());
        return new ArrayList<>(map.keySet());
    }

    public IDatasetDescriptor createDatasetFromMolecules(List<MoleculeObject> list, String name) {
        Map<UUID, MoleculeObject> objectMap = new HashMap<>();
        for (MoleculeObject moleculeObject : list) {
            objectMap.put(moleculeObject.getUUID(), moleculeObject);
        }
        Long datasetId = nextDatasetId();
        fileObjectsMap.put(datasetId, objectMap);

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

    public IDatasetDescriptor loadDatasetFromSquonkDataset(VariableModel inputVariableModel) {
        try {
            List<MoleculeObject> list = notebookService.squonkDatasetAsMolecules(currentNotebookInfo.getId(), inputVariableModel.getProducerCellModel().getName(), inputVariableModel.getName());
            return createDatasetFromMolecules(list, inputVariableModel.getProducerCellModel().getName() + "." + inputVariableModel.getName());
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
        Map<UUID, MoleculeObject> datasetContents = fileObjectsMap.get(datasetDescriptor.getId());

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


    public void executeCell(String cellName) {
        if (currentNotebookModel.findCellModel(cellName).getCellType().getExecutable()) {
            cellClient.executeCell(currentNotebookInfo.getId(), cellName);
        }
    }

    public CellType findCellType(String dropDataId) {
        return cellClient.retrieveCellType(dropDataId);
    }

    public void writeVariableFileContents(VariableModel variableModel, InputStream inputStream) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(currentNotebookInfo.getId());
        Variable variable = notebookContents.findVariable(variableModel.getProducerCellModel().getName(), variableModel.getName());
        notebookService.storeStreamingContents(currentNotebookInfo.getId(), variable, inputStream);
    }
}

