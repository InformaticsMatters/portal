package portal;

import com.im.lac.job.client.JobClient;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
@SessionScoped
public class JobsSession implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(JobsSession.class.getName());
    private JobClient jobClient;

    @Inject
    private SessionContext sessionContext;

    public JobsSession() {
        jobClient = new JobClient();
    }

    public List<JobStatus> listJobStatuses() {
        try {
            return jobClient.getJobStatuses(sessionContext.getLoggedInUserDetails().getUserid(), 0, null, null, null, null, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void submitJob(JobDefinition jobDefinition) {
        try {
            jobClient.submitJob(sessionContext.getLoggedInUserDetails().getUserid(), jobDefinition);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
