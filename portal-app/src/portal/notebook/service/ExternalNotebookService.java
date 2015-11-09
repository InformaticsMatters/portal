package portal.notebook.service;

import portal.notebook.api.CellDTO;
import portal.notebook.api.NotebookDTO;
import portal.notebook.api.NotebookMetadataDTO;
import portal.notebook.api.VariableDTO;
import toolkit.services.PU;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("notebook")
@ApplicationScoped
@Transactional
public class ExternalNotebookService {
    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;
    @Inject
    private NotebookService notebookService;


    @Path("listNotebookMetadata")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<NotebookMetadataDTO> listNotebookMetadata() {
        List<NotebookMetadataDTO> list = new ArrayList<>();
        for (Notebook notebook : entityManager.createQuery("select o from Notebook o order by o.name", Notebook.class).getResultList()) {
            NotebookMetadataDTO notebookMetadataDTO = new NotebookMetadataDTO();
            notebookMetadataDTO.fromNotebook(notebook);
            list.add(notebookMetadataDTO);
        }
        return list;
    }

    @Path("retrieveNotebookDefinition")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookMetadataDTO storeNotebook(StoreNotebookDTO storeNotebookDTO) {
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setId(storeNotebookDTO.getNotebookMetadataDTO().getId());
        notebookInfo.setName(storeNotebookDTO.getNotebookMetadataDTO().getName());
        NotebookContents notebookContents = new NotebookContents();
        Map<String, Cell> cellMap = new HashMap<>();
        for (CellDTO cellDTO : storeNotebookDTO.getNotebookDTO().getCellList()) {
            Cell cell = new Cell();
            cell.setName(cellDTO.getName());
            cell.setCellType(cellDTO.getCellType());
            cell.setPositionTop(cellDTO.getPositionTop());
            cell.setPositionLeft(cellDTO.getPositionLeft());
            cell.getPropertyMap().putAll(cellDTO.getPropertyMap());
            for (String variableName : cellDTO.getOutputVariableNameList()) {
                Variable variable = new Variable();
                variable.setProducerCell(cell);
                variable.setName(variableName);
                cell.getOutputVariableList().add(variable);
            }
            notebookContents.getCellList().add(cell);
            cellMap.put(cell.getName(), cell);
        }
        for (CellDTO cellDTO : storeNotebookDTO.getNotebookDTO().getCellList()) {
            for (VariableDTO variableDTO : cellDTO.getInputVariableList()) {
                Cell cell = cellMap.get(variableDTO.getProducerName());
                Variable variable = notebookContents.findVariable(variableDTO.getProducerName(), variableDTO.getName());
                cell.getInputVariableList().add(variable);
            }
        }
        StoreNotebookData storeNotebookData = new StoreNotebookData();
        storeNotebookData.setNotebookInfo(notebookInfo);
        storeNotebookData.setNotebookContents(notebookContents);
        Long id = notebookService.storeNotebook(storeNotebookData);
        Notebook notebook = entityManager.find(Notebook.class, id);
        NotebookMetadataDTO notebookMetadataDTO = new NotebookMetadataDTO();
        notebookMetadataDTO.fromNotebook(notebook);
        return notebookMetadataDTO;
    }

    @Path("retrieveNotebookDefinition")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookDTO retrieveNotebookDefinition(@QueryParam("notebookId") Long notebookId) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        NotebookDTO notebookDTO = new NotebookDTO();
        Map<String, VariableDTO> variableDefinitionDTOMap = new HashMap<>();
        for (Cell cell : notebookContents.getCellList()) {
            CellDTO cellDTO = new CellDTO();
            cellDTO.setName(cell.getName());
            cellDTO.setCellType(cell.getCellType());
            for (Variable variable : cell.getInputVariableList()) {
                VariableDTO variableDTO = variableDefinitionDTOMap.get(variable.getProducerCell().getName() + "." + variable.getName());
                cellDTO.getInputVariableList().add(variableDTO);
            }
            for (Variable variable : cell.getOutputVariableList()) {
                cellDTO.getOutputVariableNameList().add(variable.getName());
            }
            cellDTO.getPropertyMap().putAll(cell.getPropertyMap());
            notebookDTO.getCellList().add(cellDTO);
        }
        return notebookDTO;
    }

    @Path("retrieveStringValue")
    @GET
    public String retrieveStringValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, name);
        return variable.getValue() == null ? null : variable.getValue().toString();
    }

    @Path("retrieveStreamingContents")
    @GET
    public StreamingOutput retrieveStreamingContents(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, name);
        if (variable.getValue() == null) {
            return null;
        }
        final String fileName = "files/" + variable.getValue();
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                InputStream inputStream = new FileInputStream(file);
                try {
                    byte[] buffer = new byte[4096];
                    int r = inputStream.read(buffer);
                    while (r > -1) {
                        outputStream.write(buffer, 0, r);
                        r = inputStream.read(buffer);
                    }
                    outputStream.flush();
                } finally {
                    inputStream.close();
                }
            }
        };

    }

    @Path("updateStringValue")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void updateStringValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name, @QueryParam("value") String value) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, name);
        variable.setValue(value);
    }

    @Path("updateStreamingContents")
    @POST
    public void updateStreamingValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name, InputStream inputStream) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, name);
        try {
            OutputStream outputStream = new FileOutputStream("files/" + variable.getValue());
            try {
                byte[] buffer = new byte[4096];
                int r = inputStream.read(buffer);
                while (r > -1) {
                    outputStream.write(buffer, 0, r);
                    r = inputStream.read(buffer);
                }
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Path("executeCell")
    @POST
    public void executeCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
         notebookService.executeCell(notebookId, cellName);
    }


}
