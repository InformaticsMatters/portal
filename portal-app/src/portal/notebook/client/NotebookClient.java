package portal.notebook.client;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import portal.notebook.execution.api.CallbackClient;
import portal.notebook.execution.api.NotebookDTO;
import toolkit.services.AbstractServiceClient;

import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.logging.Logger;

public class NotebookClient extends AbstractServiceClient implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(CallbackClient.class.getName());

    private final String url;

    public NotebookClient() {
        String s = System.getenv("SERVICE_NOTEBOOK");
        if (s == null) {
            url = "http://localhost:8080/ws/notebook";
        } else {
            url = s;
        }
    }


    public NotebookData retrieveNotebook(Long notebookId) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("notebookId", notebookId.toString());
        return newResourceBuilder("/retrieveNotebook", queryParams).get(NotebookData.class);
    }

    public NotebookData storeNotebook(NotebookDTO notebookData) {
        return newResourceBuilder("/storeNotebook").post(NotebookData.class, notebookData);
    }

    @Override
    protected String getServiceBaseUri() {
        return url;
    }
}