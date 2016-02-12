package portal.notebook.cells;

import com.im.lac.job.jobdef.JobStatus;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellExecutionData;
import portal.notebook.api.CellExecutor;

/**
 * @author simetrias
 */
public class ServiceCellDefinition extends CellDefinition {

    @Override
    public CellExecutor getCellExecutor() {
        return new CellExecutor() {

            @Override
            public JobStatus execute(CellExecutionData data) throws Exception {
                return null;
            }
        };
    }
}
