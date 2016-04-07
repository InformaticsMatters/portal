package portal.notebook.api;

import com.im.lac.job.jobdef.JobStatus;
import portal.notebook.webapp.BindingsPanel;

public abstract class CellExecutor {

    public abstract JobStatus execute(BindingsPanel.CellInstance cell, CellExecutionData data) throws Exception;

}
