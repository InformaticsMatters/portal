package portal.notebook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.squonk.client.JobStatusClient;
import portal.notebook.api.NotebookClient;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

@Alternative
public class MockJobStatusClient implements JobStatusClient, Serializable {
    @Inject
    private NotebookClient notebookClient;

    @Override
    public JobStatus submit(JobDefinition jobDefinition, String s, Integer integer) throws IOException {
        new JobThread(jobDefinition, "http://localhost:8080/ws/jobs").start();
        return null;
    }

   @Override
    public JobStatus get(String s) throws IOException {
        return null;
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
