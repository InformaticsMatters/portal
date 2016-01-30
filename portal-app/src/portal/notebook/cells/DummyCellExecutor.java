package portal.notebook.cells;


import com.im.lac.job.jobdef.JobStatus;
import portal.notebook.api.CellExecutor;
import portal.notebook.api.CellInstance;

public class DummyCellExecutor implements CellExecutor {

    @Override
    public JobStatus execute(Long notebookId, CellInstance cell) throws Exception {
        return null;
    }
}
