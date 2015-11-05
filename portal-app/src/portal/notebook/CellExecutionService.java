package portal.notebook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.im.lac.types.MoleculeObject;
import com.squonk.types.io.JsonHandler;
import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("cell")
@ApplicationScoped
@Transactional
public class CellExecutionService {

    @Inject
    private NotebookService notebookService;

    @Path("retrieveNotebookDefinition")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public NotebookDefinitionDTO retrieveNotebookDefinition(@QueryParam("notebookId") Long notebookId) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        NotebookDefinitionDTO notebookDefinitionDTO = new NotebookDefinitionDTO();
        for (Cell cell : notebookContents.getCellList()) {
            CellDefinitionDTO cellDefinitionDTO = new CellDefinitionDTO();
            cellDefinitionDTO.setName(cell.getName());
            cellDefinitionDTO.setCellType(cell.getCellType());
            for (Variable variable : cell.getOutputVariableList()) {
                cellDefinitionDTO.getOutputVariableNameList().add(variable.getName());
            }
            for (Variable variable : cell.getInputVariableList()) {
                VariableDefinitionDTO variableDefinitionDTO = new VariableDefinitionDTO();
                variableDefinitionDTO.setName(variable.getName());
                variableDefinitionDTO.setProducerName(variable.getProducerCell().getName());
                cellDefinitionDTO.getInputVariableDefinitionList().add(variableDefinitionDTO);
            }
            cellDefinitionDTO.getPropertyMap().putAll(cell.getPropertyMap());
            notebookDefinitionDTO.getCellDefinitionList().add(cellDefinitionDTO);
        }
        return notebookDefinitionDTO;
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
                File file = resolveFile(notebookId, variable);
                if (file.exists()) {
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

    @Path("writeObjectValue")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void writeValueAsJson(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, Object value) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        variable.setValue(value);
        notebookService.storeNotebookContents(notebookId, notebookContents);
    }

    @Path("writeStreamValue")
    @POST
    public void writeStreamValue(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName, InputStream inputStream) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        try {
            File file = resolveFile(notebookId, variable);
            OutputStream outputStream = new FileOutputStream(file);
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

    private File resolveFile(Long notebookId, Variable variable) throws UnsupportedEncodingException {
        if (variable.getVariableType().equals(VariableType.FILE)) {
             return new File("files/" + variable.getValue());
        } if (variable.getVariableType().equals(VariableType.STREAM)) {
            String fileName = URLEncoder.encode(variable.getProducerCell().getName() + "_" + variable.getName(), "US-ASCII");
            return new File("files/" + fileName);
        }  else {
            return null;
        }
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
