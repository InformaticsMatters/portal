package portal.notebook.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import toolkit.services.AbstractServiceClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class NotebookClient extends AbstractServiceClient implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(NotebookClient.class.getName());
    @Inject
    private NotebookClientConfig config;


    public NotebookInstance retrieveNotebookInstance(Long notebookId) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        return (NotebookInstance) this.newResourceBuilder("/retrieveNotebookInstance", queryParams).get(NotebookInstance.class);
    }

    public CellInstance retrieveCellInstance(Long notebookId, String cellName) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("cellName", cellName);
        return (CellInstance) this.newResourceBuilder("/retrieveCellInstance", queryParams).get(CellInstance.class);
    }

    public String readTextValue(Long notebookId, String producerName, String variableName) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        return (String) this.newResourceBuilder("/readTextValue", queryParams).get(String.class);
    }

    public Integer readIntegerValue(Long notebookId, String producerName, String variableName) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        String string = (String) this.newResourceBuilder("/readTextValue", queryParams).get(String.class);
        return string == null ? null : new Integer(string);
    }

    public InputStream readStreamValue(Long notebookId, String producerName, String variableName) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = this.newResourceBuilder("/readStreamValue", queryParams);
        return (InputStream) builder.get(InputStream.class);
    }

    public List<MoleculeObject> readFileValueAsMolecules(Long notebookId, String producerName, String variableName) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = this.newResourceBuilder("/readFileValueAsMolecules", queryParams);
        String json = (String) builder.get(String.class);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, new TypeReference<List<MoleculeObject>>() {
            });
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        }
    }

    public void writeTextValue(Long notebookId, String producerName, String variableName, String value) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        queryParams.add("value", value);
        this.newResourceBuilder("/writeTextValue", queryParams).post();
    }

    public void writeIntegerValue(Long notebookId, String cellName, String variableName, Integer value) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", cellName);
        queryParams.add("variableName", variableName);
        queryParams.add("value", value == null ? null : value.toString());
        this.newResourceBuilder("/writeIntegerValue", queryParams).post();
    }

    public void writeStreamContents(Long notebookId, String producerName, String variableName, final InputStream inputStream) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = this.newResourceBuilder("/writeStreamContents", queryParams);
        builder.post(new StreamingOutput() {
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                NotebookClient.this.transfer(inputStream, outputStream);
                outputStream.flush();
            }
        });
    }

    public void writeStreamContents(Long notebookId, String producerName, String variableName, StreamingOutput streamingOutput) {
        MultivaluedMapImpl queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = this.newResourceBuilder("/writeStreamContents", queryParams);
        builder.post(streamingOutput);
    }

    private void transfer(InputStream responseStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];

        for (int r = responseStream.read(buffer, 0, buffer.length); r > -1; r = responseStream.read(buffer, 0, buffer.length)) {
            outputStream.write(buffer, 0, r);
        }

    }

    protected String getServiceBaseUri() {
        return this.config.getBaseUri();
    }

}