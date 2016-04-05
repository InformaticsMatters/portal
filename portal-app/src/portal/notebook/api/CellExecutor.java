package portal.notebook.api;

import com.im.lac.job.jobdef.JobStatus;

public abstract class CellExecutor {

    public abstract JobStatus execute(CellInstance cell, CellExecutionData data) throws Exception;

}
