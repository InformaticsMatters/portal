package portal.notebook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import portal.notebook.api.*;
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

@Path("callback")
@ApplicationScoped
@Transactional
public class CallbackWebService {

    @Inject
    private NotebookService notebookService;

    @Path("retrieveNotebook")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookInstance retrieveNotebook(@QueryParam("notebookId") Long notebookId) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        return notebookInstance;
    }

    @Path("retrieveCell")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CellInstance retrieveCell(@QueryParam("notebookId") Long notebookId, @QueryParam("cellName") String cellName) {
        NotebookInstance notebookDTO = retrieveNotebook(notebookId);
        return notebookDTO.findCellByName(cellName);
    }


    @Path("readTextValue")
    @GET
    public String readTextValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue().toString();
    }


    @Path("readObjectValue")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object readObjectValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue();
    }

    @Path("readStreamValue")
    @GET
    public StreamingOutput readStreamValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
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
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        variable.setValue(value);
        notebookService.storeNotebookContents(notebookId, notebookInstance);
    }

    @Path("writeIntegerValue")
    @POST
    public void writeIntegerValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, @QueryParam("value") Integer value) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        variable.setValue(value);
        notebookService.storeNotebookContents(notebookId, notebookInstance);
    }

    @Path("writeObjectValue")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void writeObjectValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, Object value) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        variable.setValue(value);
        notebookService.storeNotebookContents(notebookId, notebookInstance);
    }

    @Path("writeStreamContents")
    @POST
    public void writeStreamContents(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, InputStream inputStream) {
        NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
        VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
        notebookService.storeStreamingContents(notebookId, variable, inputStream);
    }

    @Path("readFileValueAsMolecules")
    @GET
    public StreamingOutput readFileValueAsMolecules(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                NotebookInstance notebookInstance = notebookService.retrieveNotebookContents(notebookId);
                VariableInstance variable = notebookInstance.findVariable(producerName, variableName);
                List<MoleculeObject> list = notebookService.retrieveFileContentAsMolecules(variable.getValue().toString());
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(outputStream, list);
                outputStream.flush();
            }
        };

    }

}
