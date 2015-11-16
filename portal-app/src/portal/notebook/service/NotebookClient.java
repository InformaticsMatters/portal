package portal.notebook.service;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import portal.notebook.api.CallbackClient;
import portal.notebook.api.NotebookDTO;
import toolkit.services.AbstractServiceClient;

import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.logging.Logger;

public class NotebookClient extends AbstractServiceClient implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(CallbackClient.class.getName());

    public NotebookDTO retrieveNotebook(Long notebookId) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        return newResourceBuilder("/retrieveNotebook", queryParams).get(NotebookDTO.class);
    }

    @Override
    protected String getServiceBaseUri() {
        return "http://localhost:8080/ws/notebook";
    }
}