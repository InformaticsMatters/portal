package portal.notebook.api;


import com.im.lac.job.jobdef.JobStatus;
import portal.notebook.api.CellExecutionData;
import portal.notebook.api.CellExecutor;

public class DummyCellExecutor implements CellExecutor {

    @Override
    public JobStatus execute(CellExecutionData data) throws Exception {
        return null;
    }
}
