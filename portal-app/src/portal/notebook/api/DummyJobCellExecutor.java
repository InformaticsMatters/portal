package portal.notebook.api;


import com.im.lac.job.jobdef.JobStatus;

public class DummyJobCellExecutor implements CellExecutor {

    @Override
    public JobStatus execute(Long notebookId, CellInstance cell) throws Exception {
        return null;
    }
}
