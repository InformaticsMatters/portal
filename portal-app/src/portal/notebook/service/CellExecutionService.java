package portal.notebook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import portal.notebook.api.CellDTO;
import portal.notebook.api.NotebookDTO;
import portal.notebook.api.VariableDTO;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Path("cell")
@ApplicationScoped
@Transactional
public class CellExecutionService {

    @Inject
    private NotebookService notebookService;

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

    @Path("retrieveCell")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CellDTO retrieveCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
        NotebookDTO notebookDTO = retrieveNotebook(notebookId);
        return notebookDTO.findCell(cellName);
    }


    @Path("readTextValue")
    @GET
    public String readTextValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue().toString();
    }


    @Path("readObjectValue")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object readObjectValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue();
    }

    @Path("readStreamValue")
    @GET
    public StreamingOutput readStreamValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                notebookService.outputStreamingContents(notebookId, variable, outputStream);
            }
        };

    }

    @Path("writeTextValue")
    @POST
    public void writeValueAsText(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, @QueryParam("value") String value) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        variable.setValue(value);
        notebookService.storeNotebookContents(notebookId, notebookContents);
    }

    @Path("writeIntegerValue")
    @POST
    public void writeIntegerValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, @QueryParam("value") Integer value) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        variable.setValue(value);
        notebookService.storeNotebookContents(notebookId, notebookContents);
    }

    @Path("writeObjectValue")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void writeObjectValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, Object value) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        variable.setValue(value);
        notebookService.storeNotebookContents(notebookId, notebookContents);
    }

    @Path("writeStreamContents")
    @POST
    public void writeStreamContents(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, InputStream inputStream) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        notebookService.storeStreamingContents(notebookId, variable, inputStream);
    }

    @Path("readFileValueAsMolecules")
    @GET
    public StreamingOutput readFileValueAsMolecules(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
                Variable variable = notebookContents.findVariable(producerName, variableName);
                List<MoleculeObject> list = notebookService.retrieveFileContentAsMolecules(variable.getValue().toString());
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(outputStream, list);
                outputStream.flush();
            }
        };

    }

}
