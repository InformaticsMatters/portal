package portal.notebook.api;

import com.im.lac.job.jobdef.JobDefinition;

/**
 * temporary
 */
public class SimpleCellDefinition extends CellDefinition {
    @Override
    protected String executeJob(NotebookInstance notebookInstance, Long cellId) {
        return null;
    }

    @Override
    protected JobDefinition buildJobDefinition(CellInstance cell) {
        return null;
    }
}
