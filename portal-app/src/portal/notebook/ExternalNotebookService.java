package portal.notebook;

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


    @Path("createNotebook")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookMetadataDTO createNotebook(NotebookMetadataDTO notebookMetadataDTO) {
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setName(notebookMetadataDTO.getName());
        NotebookModel notebookModel = new NotebookModel();
        StoreNotebookData storeNotebookData = new StoreNotebookData();
        storeNotebookData.setNotebookInfo(notebookInfo);
        storeNotebookData.setNotebookModel(notebookModel);
        Long id = notebookService.storeNotebook(storeNotebookData);
        notebookMetadataDTO.setId(id);
        notebookMetadataDTO.setOwnerName("poc");
        return notebookMetadataDTO;
    }


    @Path("listNotebookMetadata")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<NotebookMetadataDTO> listNotebookMetadata() {
        List<NotebookMetadataDTO> list = new ArrayList<>();
        for (Notebook notebook : entityManager.createQuery("select o from Notebook o order by o.name", Notebook.class).getResultList()) {
            NotebookMetadataDTO notebookMetadataDTO = new NotebookMetadataDTO();
            notebookMetadataDTO.setId(notebook.getId());
            notebookMetadataDTO.setName(notebook.getName());
            notebookMetadataDTO.setOwnerName("poc");
            list.add(notebookMetadataDTO);
        }
        return list;
    }

    @Path("notebookDefinition")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookDefinitionDTO notebookDefinition(@QueryParam("notebookId") Long notebookId) {
        NotebookModel notebookModel = notebookService.retrieveNotebookContents(notebookId);
        NotebookDefinitionDTO notebookDefinitionDTO = new NotebookDefinitionDTO();
        Map<String, VariableDefinitionDTO> variableDefinitionDTOMap = new HashMap<>();
        for (VariableModel variableModel : notebookModel.getVariableModelList()) {
            VariableDefinitionDTO variableDefinitionDTO = new VariableDefinitionDTO();
            variableDefinitionDTO.setName(variableModel.getName());
            variableDefinitionDTO.setProducerName(variableModel.getProducer().getName());
            notebookDefinitionDTO.getVariableDefinitionList().add(variableDefinitionDTO);
            variableDefinitionDTOMap.put(variableModel.getProducer().getName() + "." + variableModel.getName(), variableDefinitionDTO);
        }
        for (CellModel cellModel : notebookModel.getCellModelList()) {
            CellDefinitionDTO cellDefinitionDTO = new CellDefinitionDTO();
            cellDefinitionDTO.setName(cellModel.getName());
            cellDefinitionDTO.setCellType(cellModel.getCellType());
            for (VariableModel variableModel : cellModel.getInputVariableModelList()) {
                VariableDefinitionDTO variableDefinitionDTO = variableDefinitionDTOMap.get(variableModel.getProducer().getName() + "." + variableModel.getName());
                cellDefinitionDTO.getInputVariableDefinitionList().add(variableDefinitionDTO);
            }
            cellDefinitionDTO.getOutputVariableNameList().addAll(cellModel.getOutputVariableNameList());
            notebookDefinitionDTO.getCellDefinitionList().add(cellDefinitionDTO);
        }
        return notebookDefinitionDTO;
    }

    @Path("retrieveStringValue")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String retrieveStringValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name) {
        NotebookModel notebookModel = notebookService.retrieveNotebookContents(notebookId);
        VariableModel variableModel = notebookModel.findVariable(producerName, name);
        return variableModel.getValue() == null ? null : variableModel.getValue().toString();
    }

    @Path("retrieveStreamingContents")
    @GET
    public StreamingOutput retrieveStreamingContents(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name) {
        NotebookModel notebookModel = notebookService.retrieveNotebookContents(notebookId);
        VariableModel variableModel = notebookModel.findVariable(producerName, name);
        if (variableModel.getValue() == null) {
            return null;
        }
        final String fileName = "files/" + variableModel.getValue();
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

    @Path("updateVariableValue")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void updateStringValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name, @QueryParam("value") String value) {
        NotebookModel notebookModel = notebookService.retrieveNotebookContents(notebookId);
        VariableModel variableModel = notebookModel.findVariable(producerName, name);
        variableModel.setValue(value);
    }



}
