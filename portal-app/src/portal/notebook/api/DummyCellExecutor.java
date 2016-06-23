package portal.notebook.api;

import org.squonk.jobdef.JobStatus;

public class DummyCellExecutor extends CellExecutor {
    @Override
    public JobStatus execute(CellInstance cell, CellExecutionData data) throws Exception {
        return null;
    }
}
