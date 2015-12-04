package portal.notebook.service;

import com.squonk.notebook.client.CellClient;
import portal.notebook.client.CellData;
import portal.notebook.client.NotebookData;
import portal.notebook.client.VariableData;
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
    public NotebookData retrieveNotebook(@QueryParam("notebookId") Long notebookId) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        NotebookData notebookDTO = new NotebookData();
        for (Cell cell : notebookContents.getCellList()) {
            CellData cellDTO = new CellData();
            cellDTO.setName(cell.getName());
            cellDTO.setCellType(cell.getCellType());
            for (Variable variable : cell.getOutputVariableMap().values()) {
                cellDTO.getOutputVariableNameList().add(variable.getName());
            }
            for (Binding binding : cell.getBindingMap().values()) {
                Variable variable = binding.getVariable();
                if (variable != null) {
                    VariableData variableDTO = new VariableData();
                    variableDTO.setName(variable.getName());
                    variableDTO.setProducerName(variable.getProducerCell().getName());
                    cellDTO.getInputVariableList().add(variableDTO);
                }
            }
            cellDTO.getPropertyMap().putAll(cell.getOptionMap());
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
