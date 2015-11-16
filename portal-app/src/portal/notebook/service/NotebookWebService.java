package portal.notebook.service;

import portal.notebook.api.CellClient;
import portal.notebook.api.CellDTO;
import portal.notebook.api.NotebookDTO;
import portal.notebook.api.VariableDTO;
import toolkit.services.PU;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("notebook")
@ApplicationScoped
@Transactional
public class NotebookWebService {
    @Inject
    @PU(puName = NotebookConstants.PU_NAME)
    private EntityManager entityManager;
    @Inject
    private NotebookService notebookService;
    @Inject
    private CellClient cellClient;

    @Path("retrieveNotebook")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookDTO retrieveNotebook(@QueryParam("notebookId") Long notebookId) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        NotebookDTO notebookDTO = new NotebookDTO();
        for (Cell cell : notebookContents.getCellList()) {
            CellDTO cellDTO = new CellDTO();
            cellDTO.setName(cell.getName());
            cellDTO.setCellType(cell.getCellType());
            for (Variable variable : cell.getOutputVariableList()) {
                cellDTO.getOutputVariableNameList().add(variable.getName());
            }
            for (Variable variable : cell.getInputVariableList()) {
                VariableDTO variableDTO = new VariableDTO();
                variableDTO.setName(variable.getName());
                variableDTO.setProducerName(variable.getProducerCell().getName());
                cellDTO.getInputVariableList().add(variableDTO);
            }
            cellDTO.getPropertyMap().putAll(cell.getPropertyMap());
            notebookDTO.getCellList().add(cellDTO);
        }
        return notebookDTO;
    }

    @Path("executeCell")
    @POST
    public void executeCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
        cellClient.executeCell(notebookId, cellName);
    }


}
