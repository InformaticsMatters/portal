package portal.notebook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import javafx.animation.Animation;
import org.squonk.client.JobStatusClient;
import portal.notebook.api.NotebookClient;
import portal.notebook.api.NotebookClientConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Alternative
@ApplicationScoped
public class MockJobStatusClient implements JobStatusClient, Serializable {
    private static final Logger LOGGER = Logger.getLogger(MockJobStatusClient.class.getName());
    @Inject
    private NotebookClientConfig notebookClientConfig;
    private final Map<String, JobStatus> jobStatusMap = new HashMap<>();

    @Override
    public JobStatus submit(JobDefinition jobDefinition, String username, Integer integer) throws IOException {
        new JobThread(jobDefinition, notebookClientConfig.getBaseUri().replaceAll("notebook", "jobs")).start();
        JobStatus<JobDefinition> jobStatus = JobStatus.create(jobDefinition, username, new Date(), 1);
        String jobId = jobStatus.getJobId();
        LOGGER.log(Level.INFO, jobId);
        jobStatusMap.put(jobId, jobStatus);
        return jobStatus;
    }

   @Override
    public JobStatus get(String jobId) throws IOException {
       LOGGER.log(Level.INFO, jobId);
       JobStatus oldJobStatus = jobStatusMap.get(jobId);
       if (oldJobStatus == null) {
           JobStatus<JobDefinition> jobStatus = new JobStatus(jobId, "some", JobStatus.Status.COMPLETED, 2, 1, 0, null, null, null, null, null);
           jobStatusMap.put(jobStatus.getJobId(), jobStatus);
           return jobStatus;
       }  else {
           JobStatus.Status newStatus = oldJobStatus.getStatus().equals(JobStatus.Status.RUNNING) ? JobStatus.Status.COMPLETED : JobStatus.Status.RUNNING;
           JobStatus newJobStatus = new JobStatus(jobId, oldJobStatus.getUsername(), newStatus, 0, 0, 0, oldJobStatus.getStarted(), new Date(), oldJobStatus.getJobDefinition(), null, null);
           jobStatusMap.put(jobId, newJobStatus);
           return newJobStatus;
       }
    }

    @Override
    public List<JobStatus> list(JobQuery jobQuery) throws IOException {
        return null;
    }

    @Override
    public JobStatus updateStatus(String s, JobStatus.Status status, String s1, Integer integer, Integer integer1) throws IOException {
        return null;
    }

    @Override
    public JobStatus incrementCounts(String s, int i, int i1) throws IOException {
        return null;
    }

    class JobThread extends Thread {
        private final JobDefinition jobDefinition;
        private final String baseUri;

        JobThread(JobDefinition jobDefinition, String baseUri) {
            this.jobDefinition = jobDefinition;
            this.baseUri = baseUri;
        }

        @Override
        public void run() {
            WebResource resource = Client.create().resource(baseUri + "/execute");
            resource.type(MediaType.APPLICATION_JSON).post(new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(outputStream, jobDefinition);
                    outputStream.flush();
                }
            });
        }

    }

}
