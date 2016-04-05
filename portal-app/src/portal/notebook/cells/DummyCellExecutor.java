package portal.notebook.cells;


import com.im.lac.job.jobdef.JobStatus;
import portal.notebook.api.CellExecutionData;
import portal.notebook.api.CellExecutor;
import portal.notebook.api.CellInstance;

public class DummyCellExecutor extends CellExecutor {

    @Override
    public JobStatus execute(CellInstance cell, CellExecutionData data) throws Exception {
        return null;
    }
}
