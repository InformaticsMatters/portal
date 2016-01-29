package portal.notebook.api;

import com.im.lac.job.jobdef.JobDefinition;

/**
 * temporary
 */
public class SimpleCellDefinition extends CellDefinition {


    @Override
    public CellExecutor getCellExecutor() {
        return new DummyJobCellExecutor();
    }
}
