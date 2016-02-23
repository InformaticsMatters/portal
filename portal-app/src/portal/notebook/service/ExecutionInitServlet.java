package portal.notebook.service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import portal.notebook.api.NotebookClientConfig;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutionInitServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ExecutionInitServlet.class.getName());
    private Timer timer;
    @Inject
    private NotebookClientConfig notebookClientConfig;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        timer = new Timer();
        timer.schedule(new UpdateExecutionsTask(), 5000, 5000);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                timer.cancel();
            }
        });

    }

    private void updateExecutions() {
        Client client = Client.create();
        WebResource resource = client.resource(notebookClientConfig.getBaseUri() + "/listActiveExecution");
        GenericType<List<Execution>> genericType = new GenericType<List<Execution>>(){};
        List<Execution> list = resource.get(genericType);
        for (Execution execution : list) {
            LOGGER.log(Level.INFO, "Processng execution id " + execution.getId());
            try {
                client.resource(notebookClientConfig.getBaseUri() + "/updateExecutionStatus").queryParam("id", execution.getId().toString()).post();
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "Error processing execution id " + execution.getId(), t);
            }
        }
    }

    class UpdateExecutionsTask extends TimerTask {

        @Override
        public void run() {
           updateExecutions();
        }
    }
}
