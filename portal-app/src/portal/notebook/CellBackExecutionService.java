package portal.notebook;

import toolkit.services.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URLEncoder;

@Path("cell")
@ApplicationScoped
@Transactional
public class CellBackExecutionService {

    @Inject
    private NotebookService notebookService;
    @Path("readValueAsText")
    @GET
    public String readValueAsText(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue().toString();
    }

    @Path("readValueAsJson")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Object readValueAsJson(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        return variable.getValue() == null ? null : variable.getValue();
    }

    @Path("readValueAsStream")
    @GET
    public StreamingOutput readValueAsStream(@QueryParam("notebookId") Long notebookId, @QueryParam("producerName") String producerName, @QueryParam("variableName") String variableName) {
        NotebookContents notebookContents = notebookService.retrieveNotebookContents(notebookId);
        Variable variable = notebookContents.findVariable(producerName, variableName);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                File file = resolveFile(variable);
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

    private File resolveFile(Variable variable) throws UnsupportedEncodingException {
        if (variable.getVariableType().equals(VariableType.FILE)) {
             return new File("files/" + variable.getValue());
        } if (variable.getVariableType().equals(VariableType.STREAM)) {
            String fileName = URLEncoder.encode(variable.getProducerCell().getName() + "_" + variable.getName(), "US-ASCII");
            return new File("files/" + fileName);
        }  else {
            return null;
        }
    }

}
