package portal.notebook.client;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.util.IOUtils;
import toolkit.services.AbstractServiceClient;

import javax.ws.rs.core.MultivaluedMap;
import java.io.Serializable;
import java.util.logging.Logger;

public class NotebookClient extends AbstractServiceClient implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(NotebookClient.class.getName());

    private final String url;

    public NotebookClient() {
        url = IOUtils.getConfiguration("SERVICE_NOTEBOOK", "http://localhost:8080/ws/notebook");
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