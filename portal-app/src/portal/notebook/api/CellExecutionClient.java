package portal.notebook.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import toolkit.services.AbstractServiceClient;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

public class CellExecutionClient extends AbstractServiceClient implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(CellExecutionClient.class.getName());
    @Inject
    private CellExecutionClientConfig config;
    ;

    public NotebookDTO retrieveNotebookDefinition(Long notebookId) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        return newResourceBuilder("/retrieveNotebook", queryParams).get(NotebookDTO.class);

    }

    public CellDTO retrieveCell(Long notebookId, String cellName) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("cellName", cellName);
        return newResourceBuilder("/retrieveCell", queryParams).get(CellDTO.class);
    }

    public String readTextValue(Long notebookId, String producerName, String variableName) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        return newResourceBuilder("/readTextValue", queryParams).get(String.class);
    }

    public Integer readIntegerValue(Long notebookId, String producerName, String variableName) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        return newResourceBuilder("/readTextValue", queryParams).get(Integer.class);
    }

    public InputStream readStreamValue(Long notebookId, String producerName, String variableName) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = newResourceBuilder("/readStreamValue", queryParams);
        return builder.get(InputStream.class);
    }

    public List<MoleculeObject> readFileValueAsMolecules(Long notebookId, String producerName, String variableName) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = newResourceBuilder("/readFileValueAsMolecules", queryParams);
        String json = builder.get(String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<List<MoleculeObject>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeTextValue(Long notebookId, String producerName, String variableName, String value) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        queryParams.add("value", value);
        newResourceBuilder("/writeTextValue", queryParams).post();
    }


    public void writeIntegerValue(Long notebookId, String cellName, String variableName, Integer value) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", cellName);
        queryParams.add("variableName", variableName);
        queryParams.add("value", value == null ? null : value.toString());
        newResourceBuilder("/writeIntegerValue", queryParams).post();
    }

    public void writeStreamContents(Long notebookId, String producerName, String variableName, InputStream inputStream) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = newResourceBuilder("/writeStreamContents", queryParams);
        builder.post(new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                transfer(inputStream, outputStream);
                outputStream.flush();
            }
        });
    }

    public void writeStreamContents(Long notebookId, String producerName, String variableName, StreamingOutput streamingOutput) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        queryParams.add("producerName", producerName);
        queryParams.add("variableName", variableName);
        WebResource.Builder builder = newResourceBuilder("/writeStreamContents", queryParams);
        builder.post(streamingOutput);
    }


    private void transfer(InputStream responseStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int r = responseStream.read(buffer, 0, buffer.length);
        while (r > -1) {
            outputStream.write(buffer, 0, r);
            r = responseStream.read(buffer, 0, buffer.length);
        }
    }

    @Override
    protected String getServiceBaseUri() {
        return config.getBaseUri();
    }


}