package portal.notebook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.CellExecutorJobDefinition;
import org.squonk.jobdef.ExternalJobDefinition;
import org.squonk.jobdef.JobQuery;
import org.squonk.jobdef.JobStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.squonk.client.JobStatusClient;
import org.squonk.execution.steps.StepDefinitionConstants;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Alternative
@ApplicationScoped
public class MockJobStatusClient implements JobStatusClient, Serializable {

    private static final Logger LOGGER = Logger.getLogger(MockJobStatusClient.class.getName());
    private final Map<String, JobStatus> jobStatusMap = new HashMap<>();
    private final Map<String, JobDefinition> jobDefinitionMap = new HashMap<>();

    @Override
    public JobStatus submit(CellExecutorJobDefinition jobDefinition, String username, String accessToken, Integer integer) throws IOException {
        new JobThread(jobDefinition, "http://localhost:8080/ws/mockJobs").start();
        JobStatus<JobDefinition> jobStatus = JobStatus.create(jobDefinition, username, new Date(), 1);
        String jobId = jobStatus.getJobId();
        LOGGER.log(Level.INFO, jobId);
        jobStatusMap.put(jobId, jobStatus);
        jobDefinitionMap.put(jobId, jobDefinition);
        return jobStatus;
    }

    public JobStatus create(ExternalJobDefinition jobdef, String username, Integer totalCount) throws IOException {
        throw new IllegalStateException("NYI");
    }

    @Override
    public JobStatus get(String jobId) throws IOException {
       LOGGER.log(Level.INFO, jobId);
       JobStatus oldJobStatus = jobStatusMap.get(jobId);
       if (oldJobStatus == null) {
           String message = resolveMessage(JobStatus.Status.COMPLETED);
           JobStatus<? extends JobDefinition> jobStatus = new JobStatus<>(jobId, "some", JobStatus.Status.COMPLETED, 2, 1, 0, null, null, null, Collections.singletonList("message"));
           jobStatusMap.put(jobStatus.getJobId(), jobStatus);
           return jobStatus;
       }  else {
           JobStatus.Status newStatus = calculateStatus(jobId, oldJobStatus);
           String message = resolveMessage(newStatus);
           JobStatus<? extends JobDefinition> newJobStatus = new JobStatus<>(jobId, oldJobStatus.getUsername(), newStatus, 0, 0, 0, oldJobStatus.getStarted(), new Date(), oldJobStatus.getJobDefinition(), message == null ? null : Collections.singletonList(message));
           jobStatusMap.put(jobId, newJobStatus);
           return newJobStatus;
       }
    }

    private String resolveMessage(JobStatus.Status status) {
        if (JobStatus.Status.COMPLETED.equals(status)) {
            return "A lot of items processed";
        } else if (JobStatus.Status.ERROR.equals(status)) {
            return "Ugly errors occurred";
        } else if (JobStatus.Status.RUNNING.equals(status)) {
            return "Processing a lot of items";
        } else {
            return null;
        }
    }

    private JobStatus.Status calculateStatus(String jobId, JobStatus oldJobStatus) {
        if (hasToFail(jobId)) {
            return JobStatus.Status.ERROR;
        } else {
            return oldJobStatus.getStatus().equals(JobStatus.Status.RUNNING) ? JobStatus.Status.COMPLETED : JobStatus.Status.RUNNING;
        }
    }

    private boolean hasToFail(String jobId) {
        JobDefinition jobDefinition = jobDefinitionMap.get(jobId);
        if (jobDefinition instanceof ExecuteCellUsingStepsJobDefinition) {
            ExecuteCellUsingStepsJobDefinition executeCellUsingStepsJobDefinition = (ExecuteCellUsingStepsJobDefinition) jobDefinition;
            if (executeCellUsingStepsJobDefinition.getSteps()[0].getImplementationClass().equals(StepDefinitionConstants.ChemblActivitiesFetcher.CLASSNAME)) {
                return "2".equals(executeCellUsingStepsJobDefinition.getSteps()[0].getOptions().get("prefix"));
            } else {
                return false;
            }
        } else {
            return false;
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

        private final CellExecutorJobDefinition jobDefinition;
        private final String baseUri;

        JobThread(CellExecutorJobDefinition jobDefinition, String baseUri) {
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
