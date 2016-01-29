package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.client.JobStatusClient;

import javax.enterprise.inject.Alternative;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Alternative
public class MockJobStatusClient implements JobStatusClient, Serializable {
    @Override
    public JobStatus submit(JobDefinition jobDefinition, String s, Integer integer) throws IOException {
        System.out.println("submit");
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
}
