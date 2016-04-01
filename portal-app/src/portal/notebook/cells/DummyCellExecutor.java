package portal.notebook.cells;


import com.im.lac.job.jobdef.JobStatus;
import org.squonk.notebook.api.CellExecutionData;
import org.squonk.notebook.api.CellExecutor;

public class DummyCellExecutor implements CellExecutor {

    @Override
    public JobStatus execute(CellExecutionData data) throws Exception {
        return null;
    }
}
