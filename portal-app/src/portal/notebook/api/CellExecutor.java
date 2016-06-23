package portal.notebook.api;

import org.squonk.jobdef.JobStatus;

public abstract class CellExecutor {

    public abstract JobStatus execute(CellInstance cell, CellExecutionData data) throws Exception;

}
