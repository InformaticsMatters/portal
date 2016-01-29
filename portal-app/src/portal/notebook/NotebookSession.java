package portal.notebook;

import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.client.ServicesClient;
import com.im.lac.types.MoleculeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.client.CellClient;
import org.squonk.options.OptionDescriptor;
import portal.SessionContext;
import portal.dataset.*;
import portal.notebook.api.*;
import portal.notebook.api.CellDefinitionRegistry;
import portal.notebook.service.*;
import toolkit.services.Transactional;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

@SessionScoped
@Transactional
public class NotebookSession implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(NotebookSession.class);
    private final Map<Long, Map<UUID, MoleculeObject>> moleculeObjectMapMap = new HashMap<>();
    private final Map<Long, List<UUID>> uuidListMap = new HashMap<>();
    private final Map<Long, IDatasetDescriptor> datasetDescriptorMap = new HashMap<>();
    private long lastDatasetId = 0;
    private NotebookModel currentNotebookModel;
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
            notebookService.createNotebook(data);
            currentNotebookInfo = notebookService.listNotebookInfo(sessionContext.getLoggedInUserDetails().getUserid()).get(0);
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
            currentNotebookModel = null;
        }
    }

    public void loadCurrentNotebook(Long id) {
        currentNotebookInfo = notebookService.retrieveNotebookInfo(id);
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(id);
        currentNotebookModel = new NotebookModel(notebookInstance);
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
        updateNotebookContentsData.setNotebookInstance(currentNotebookModel.getNotebookInstance());
        notebookService.updateNotebookContents(updateNotebookContentsData);
    }

    public CellModel addCell(CellDefinition cellDefinition, int x, int y) {
        CellInstance cell = currentNotebookModel.getNotebookInstance().addCell(cellDefinition);
        cell.setPositionTop(y);
        cell.setPositionLeft(x);
        CellModel cellModel = currentNotebookModel.addCellModel(cell);
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(currentNotebookInfo.getId());
        updateNotebookContentsData.setNotebookInstance(currentNotebookModel.getNotebookInstance());
        notebookService.updateNotebookContents(updateNotebookContentsData);
        return cellModel;
    }

    public void removeCell(CellModel cellModel) {
        currentNotebookModel.removeCellModel(cellModel);
        UpdateNotebookContentsData updateNotebookContentsData = new UpdateNotebookContentsData();
        updateNotebookContentsData.setId(currentNotebookInfo.getId());
        updateNotebookContentsData.setNotebookInstance(currentNotebookModel.getNotebookInstance());
        notebookService.updateNotebookContents(updateNotebookContentsData);
    }

    public List<CellDefinition> listCellDefinition() {
//        List<CellType> cellTypes = cellClient.listCellDefinition();
//        addServiceCellTypes(cellTypes);
//        this.cellDefinitionList = cellTypes;
//        return cellTypes;

        List<CellDefinition> list = new ArrayList<>();
        list.addAll(cellDefinitionRegistry.listCellDefinition());
        // TODO - better to add these to the registry in the first place?
        //addServiceCellTypes(cellTypes);
        this.cellDefinitionList = list;
        return list;
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


    public void executeCell(String cellName) {
        if (currentNotebookModel.findCellModel(cellName).getCellDefinition().getExecutable()) {
            CellInstance cell = currentNotebookModel.getNotebookInstance().findCell(cellName);
            CellDefinition celldef = cell.getCellDefinition();
            try {
                JobStatus status = celldef.getCellExecutor().execute(currentNotebookInfo.getId(), cell);
                // TODO - do something with the status
            } catch (Exception e) {
                // TODO - handle nicely
                throw new RuntimeException("Failed to execute cell", e);
            }
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

    public void writeVariableFileContents(VariableModel variableModel, InputStream inputStream) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(currentNotebookInfo.getId());
        VariableInstance variable = notebookInstance.findVariable(variableModel.getProducerCellModel().getName(), variableModel.getName());
        notebookService.storeStreamingContents(currentNotebookInfo.getId(), variable, inputStream);
    }


    private void addServiceCellTypes(List<CellType> cellTypes) {
        for (ServiceDescriptor serviceDescriptor : listServiceDescriptors()) {
            cellTypes.add(buildCellTypeForServiceDescriptor(serviceDescriptor));
        }
    }

    private List<ServiceDescriptor> listServiceDescriptors() {
        ServicesClient servicesClient = new ServicesClient();
        List<ServiceDescriptor> serviceDescriptors;
        try {
            serviceDescriptors = servicesClient.getServiceDefinitions(sessionContext.getLoggedInUserDetails().getUserid());
        } catch (IOException e) {
            serviceDescriptors = new ArrayList<>();
            logger.error(null, e);
        }
        return serviceDescriptors;
    }

    private CellType buildCellTypeForServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        CellType result = new CellType();
        result.setExecutable(true);
        result.setName(serviceDescriptor.getName());
        result.setDescription(serviceDescriptor.getDescription());

        OptionDescriptor[] properties = serviceDescriptor.getAccessModes()[0].getParameters();
        if (properties != null) {

            logger.info(properties.length + " properties found for service " + serviceDescriptor.getName());

            for (OptionDescriptor propertyDescriptor : properties) {

                System.out.println("property type: " + propertyDescriptor.getTypeDescriptor().getType());
                result.getOptionDefinitionList().add(propertyDescriptor);

//                if (propertyDescriptor.getTypeDescriptor().getType() == String.class) {
//                    OptionDescriptor optionDescriptor = new OptionDescriptor()
//                    OptionDefinition<String> optionDefinition = new OptionDefinition<>();
//                    optionDefinition.setName("missing.property.name");
//                    optionDefinition.setDisplayName(propertyDescriptor.getLabel());
//                    optionDefinition.setOptionType(OptionType.SIMPLE);
//                    result.getOptionDefinitionList().add(optionDefinition);
//                } else if (propertyDescriptor.getType().equals(ServicePropertyDescriptor.Type.STRUCTURE)) {
//                    OptionDefinition<String> optionDefinition = new OptionDefinition<>();
//                    optionDefinition.setName("missing.property.name");
//                    optionDefinition.setDisplayName(propertyDescriptor.getLabel());
//                    optionDefinition.setOptionType(OptionType.PICKLIST);
//                    result.getOptionDefinitionList().add(optionDefinition);
//                }

            }
        }

        return result;
    }
}

