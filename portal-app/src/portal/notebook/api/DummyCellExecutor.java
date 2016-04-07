package portal.notebook.api;


import com.im.lac.job.jobdef.JobStatus;
import portal.notebook.webapp.BindingsPanel;

public class DummyCellExecutor extends CellExecutor {

    @Override
    public JobStatus execute(BindingsPanel.CellInstance cell, CellExecutionData data) throws Exception {
        return null;
    }
}
