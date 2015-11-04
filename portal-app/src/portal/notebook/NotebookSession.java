package portal.notebook;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import portal.dataset.*;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.*;
import java.util.*;

@SessionScoped
public class NotebookSession implements Serializable {

    private final Map<Long, Map<UUID, MoleculeObject>> fileObjectsMap = new HashMap<>();
    private final Map<Long, IDatasetDescriptor> datasetDescriptorMap = new HashMap<>();
    private long lastDatasetId = 0;
    @Inject
    private NotebookService notebookService;
    @Inject
    private CellHandlerProvider cellHandlerProvider;
    private NotebookModel notebookModel;
    private NotebookInfo notebookInfo;

    public NotebookSession() {
        fileObjectsMap.put(0l, new HashMap<>());
    }

    public NotebookInfo preparePocNotebook() {
        List<NotebookInfo> list = notebookService.listNotebookInfo();
        if (list.isEmpty()) {
            NotebookInfo notebookInfo = new NotebookInfo();
            notebookInfo.setName("POC");
            NotebookContents notebookContents = new NotebookContents();
            StoreNotebookData storeNotebookData = new StoreNotebookData();
            storeNotebookData.setNotebookInfo(notebookInfo);
            storeNotebookData.setNotebookContents(notebookContents);
            notebookService.storeNotebook(storeNotebookData);
            list = notebookService.listNotebookInfo();
        }
        return list.get(0);
    }

    public List<NotebookInfo> listNotebookInfo() {
        return notebookService.listNotebookInfo();
    }

    public void loadNotebook(Long id) {
        notebookInfo = notebookService.retrieveNotebookInfo(id);
        notebookModel = new NotebookModel();
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(id);
        notebookModel.fromNotebookContents(notebookContents);
    }

    public NotebookInfo getNotebookInfo() {
        return notebookInfo;
    }

    public NotebookModel getNotebookModel() {
        return notebookModel;
    }

    public void storeNotebook() {
        StoreNotebookData storeNotebookData = new StoreNotebookData();
        storeNotebookData.setNotebookInfo(notebookInfo);
        NotebookContents notebookContents = new NotebookContents();
        notebookModel.toNotebookContents(notebookContents, cellHandlerProvider);
        storeNotebookData.setNotebookContents(notebookContents);
        notebookService.storeNotebook(storeNotebookData);
    }

    public CellModel addCell(CellType cellType, int x, int y) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookInfo.getId());
        Cell cell = cellHandlerProvider.getCellHandler(cellType).createCell();
        notebookContents.addCell(cell);
        cell.setPositionTop(y);
        cell.setPositionLeft(x);
        StoreNotebookData storeNotebookData = new StoreNotebookData();
        storeNotebookData.setNotebookInfo(notebookInfo);
        storeNotebookData.setNotebookContents(notebookContents);
        notebookService.storeNotebook(storeNotebookData);
        CellModel cellModel = NotebookModel.createCellModel(cellType);
        cellModel.load(notebookModel, cell);
        notebookModel.addCell(cellModel);
        return cellModel;
    }

    public List<CellDescriptor> listCellDescriptor() {
        return Arrays.asList(new ScriptCellDescriptor(), new FileUploadCellDescriptor(), new PropertyCalculateCellDescriptor(), new TableDiplayCellDescriptor());
    }


      public List<VariableModel> listAvailableInputVariablesFor(CellModel cellModel, NotebookModel notebookModel) {
        List<VariableModel> list = new ArrayList<>();
        for (VariableModel variableModel : notebookModel.getVariableModelList()) {
            if (!variableModel.getProducer().equals(cellModel)) {
                list.add(variableModel);
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

        TableDisplayDescriptor datasetDescriptor = new TableDisplayDescriptor(datasetId, name, list.size());

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

    public IDatasetDescriptor loadDatasetFromFile(String fileName) {
        try {
            List<MoleculeObject> list = notebookService.parseFile(fileName);
            File file = new File("files/" + fileName);
            if (file.exists()) {
                return createDatasetFromMolecules(list, fileName);
            } else {
                return null;
            }
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
        notebookService.executeCell(getNotebookInfo().getId(), cellName);
    }
}
