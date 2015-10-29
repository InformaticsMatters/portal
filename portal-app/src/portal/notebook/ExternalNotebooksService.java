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
public class ExternalNotebooksService {
    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;
    @Inject
    private NotebooksService notebooksService;


    @Path("createNotebook")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookMetadataDTO createNotebook(NotebookMetadataDTO notebookMetadataDTO) {
        NotebookInfo notebookInfo = new NotebookInfo();
        notebookInfo.setName(notebookMetadataDTO.getName());
        NotebookContents notebookContents = new NotebookContents();
        StoreNotebookData storeNotebookData = new StoreNotebookData();
        storeNotebookData.setNotebookInfo(notebookInfo);
        storeNotebookData.setNotebookContents(notebookContents);
        Long id = notebooksService.storeNotebook(storeNotebookData);
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
        NotebookContents notebookContents = notebooksService.retrieveNotebookContents(notebookId);
        NotebookDefinitionDTO notebookDefinitionDTO = new NotebookDefinitionDTO();
        Map<String, VariableDefinitionDTO> variableDefinitionDTOMap = new HashMap<>();
        for (Variable variable : notebookContents.getVariableList()) {
            VariableDefinitionDTO variableDefinitionDTO = new VariableDefinitionDTO();
            variableDefinitionDTO.setName(variable.getName());
            variableDefinitionDTO.setProducerName(variable.getProducer().getName());
            notebookDefinitionDTO.getVariableDefinitionList().add(variableDefinitionDTO);
            variableDefinitionDTOMap.put(variable.getProducer().getName() + "." + variable.getName(), variableDefinitionDTO);
        }
        for (Cell cell : notebookContents.getCellList()) {
            CellDefinitionDTO cellDefinitionDTO = new CellDefinitionDTO();
            cellDefinitionDTO.setName(cell.getName());
            cellDefinitionDTO.setCellType(cell.getCellType());
            for (Variable variable : cell.getInputVariableList()) {
                VariableDefinitionDTO variableDefinitionDTO = variableDefinitionDTOMap.get(variable.getProducer().getName() + "." + variable.getName());
                cellDefinitionDTO.getInputVariableDefinitionList().add(variableDefinitionDTO);
            }
            cellDefinitionDTO.getOutputVariableNameList().addAll(cell.getOutputVariableNameList());
            notebookDefinitionDTO.getCellDefinitionList().add(cellDefinitionDTO);
        }
        return notebookDefinitionDTO;
    }

    @Path("variableValue")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String variableValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name) {
        NotebookContents notebookContents = notebooksService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, name);
        return variable.getValue() == null ? null : variable.getValue().toString();
    }

    @Path("fileVariableContents")
    @GET
    public StreamingOutput fileContents(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("name") String name) {
        NotebookContents notebookContents = notebooksService.retrieveNotebookContents(notebookId);
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


}
