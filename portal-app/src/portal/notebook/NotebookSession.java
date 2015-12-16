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
    private NotebookModel notebookModel;
    private NotebookInfo notebookInfo;

    public NotebookSession() {
        fileObjectsMap.put(0l, new HashMap<>());
    }

    public NotebookInfo preparePocNotebook() {
        List<NotebookInfo> list = notebookService.listNotebookInfo();
        if (list.isEmpty()) {
            EditNotebookData notebookData = new EditNotebookData();
            notebookData.setName("POC");
            notebookService.createNotebook(notebookData);
            notebookInfo = notebookService.listNotebookInfo().get(0);
            NotebookContents notebookContents = new NotebookContents();
            UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
            updateNotebookContentsData.setId(notebookInfo.getId());
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

    public void createNotebook(EditNotebookData editNotebookData) {
        notebookService.createNotebook(editNotebookData);
    }

    public void updateNotebook(EditNotebookData editNotebookData) {
        notebookService.updateNotebook(editNotebookData);
    }

    public void removeNotebook(Long notebookId) {
        notebookService.removeNotebook(notebookId);
    }

    public void loadCurrentNotebook(Long id) {
        notebookInfo = notebookService.retrieveNotebookInfo(id);
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(id);
        notebookModel = new NotebookModel(notebookContents);
    }

    public void reloadCurrentNotebook() {
        loadCurrentNotebook(notebookInfo.getId());
    }

    public NotebookModel getCurrentNotebookModel() {
        return notebookModel;
    }

    public NotebookInfo getCurrentNotebookInfo() {
        return notebookInfo;
    }

    public void storeCurrentNotebook() {
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(notebookInfo.getId());
        updateNotebookContentsData.setNotebookContents(notebookModel.getNotebookContents());
        notebookService.updateNotebookContents(updateNotebookContentsData);
    }

    public CellModel addCell(CellType cellType, int x, int y) {
        Cell cell = notebookModel.getNotebookContents().addCell(cellType);
        cell.setPositionTop(y);
        cell.setPositionLeft(x);
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(notebookInfo.getId());
        updateNotebookContentsData.setNotebookContents(notebookModel.getNotebookContents());
        notebookService.updateNotebookContents(updateNotebookContentsData);
        return notebookModel.addCellModel(cell);
    }

    public void removeCell(CellModel cellModel) {
        notebookModel.getNotebookContents().removeCell(cellModel.getName());
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(notebookInfo.getId());
        updateNotebookContentsData.setNotebookContents(notebookModel.getNotebookContents());
        notebookService.updateNotebookContents(updateNotebookContentsData);
        notebookModel.removeCellModel(cellModel);
    }

    public List<CellType> listCellType() {
        return cellClient.listCellType();
    }


    public List<VariableModel> listAvailableInputVariablesFor(CellModel excludedCellModel, BindingModel bindingModel, NotebookModel notebookModel) {
        List<VariableModel> list = new ArrayList<>();
        for (CellModel cellModel : notebookModel.getCellModels()) {
            if (cellModel != excludedCellModel) {
                for (VariableModel variableModel : cellModel.getOutputVariableModelMap().values()) {
                    if (bindingModel.getAcceptedVariableTypeList().contains(variableModel.getVariableType())) {
                        list.add(variableModel);
                    }
                }
            }
        }

        Collections.sort(list, new Comparator<VariableModel>() {
            @Override
            public int compare(VariableModel o1, VariableModel o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });
        return list;
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
        MoleculeObject moleculeObject = list.isEmpty() ? null : list.get(0);
        if (moleculeObject != null) {
            for (String key : moleculeObject.getValues().keySet()) {
                propertyCount++;
                PropertyDescriptor plainPropertyDescriptor = new PropertyDescriptor();
                plainPropertyDescriptor.setDescription(key);
                plainPropertyDescriptor.setId(propertyCount);
                rowDescriptor.addPropertyDescriptor(plainPropertyDescriptor);
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
            List<MoleculeObject> list = notebookService.squonkDatasetAsMolecules(notebookInfo.getId(), inputVariableModel.getProducerCellModel().getName(), inputVariableModel.getName());
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
        if (notebookModel.findCellModel(cellName).getCellType().getExecutable()) {
            cellClient.executeCell(notebookInfo.getId(), cellName);
        }
    }

    public CellType findCellType(String dropDataId) {
        return cellClient.retrieveCellType(dropDataId);
    }

    public void writeVariableFileContents(VariableModel variableModel, InputStream inputStream) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookInfo.getId());
        Variable variable = notebookContents.findVariable(variableModel.getProducerCellModel().getName(), variableModel.getName());
        notebookService.storeStreamingContents(notebookInfo.getId(), variable, inputStream);
    }
}

