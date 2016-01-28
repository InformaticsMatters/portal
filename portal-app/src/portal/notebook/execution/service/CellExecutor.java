package portal.notebook.execution.service;

import com.im.lac.job.jobdef.JobStatus;
import portal.notebook.CellModel;
import portal.notebook.api.CellInstance;

/**
 * Created by timbo on 28/01/16.
 */
public interface CellExecutor {

    JobStatus execute(Long notebookId, CellInstance cell) throws Exception;
}
