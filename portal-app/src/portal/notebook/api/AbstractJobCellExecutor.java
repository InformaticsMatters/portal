package portal.notebook.api;

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import org.squonk.client.JobStatusClient;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.notebook.api.*;
import portal.SessionContext;
import portal.notebook.webapp.BindingsPanel;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 16/01/16.
 */
public abstract class AbstractJobCellExecutor extends CellExecutor implements Serializable {

    private static final Logger LOG = Logger.getLogger(AbstractJobCellExecutor.class.getName());

    @Override
    public JobStatus execute(BindingsPanel.CellInstance cell, CellExecutionData data) throws Exception {
        Instance<SessionContext> sessionContextInstance = CDI.current().select(SessionContext.class);
        SessionContext sessionContext = sessionContextInstance.get();
        String username = sessionContext.getLoggedInUserDetails().getUserid();
        Integer workunits = null; // null means "I don't know", but we can probably get the number from the dataset metadata

        // create the job
        JobDefinition jobdef = buildJobDefinition(cell, data);
        // execute the job
        JobStatusClient client = createJobStatusClient();
        LOG.info("Executing job using client " + client);
        JobStatus status = client.submit(jobdef, username, workunits);
        // job is now running. we can either poll the JobStatusRestClient for its status or listen on the message queue for updates
        return status;
    }

    protected JobStatusClient createJobStatusClient() {
        Instance<JobStatusClient> instance = CDI.current().select(JobStatusClient.class);
        return instance.get();
    }

    /**
     * Build the JobDefinition that will be submitted for execution.
     */
    protected abstract JobDefinition buildJobDefinition(BindingsPanel.CellInstance cell, CellExecutionData cellExecutionData);


    protected Map<String, Object> collectAllOptions(BindingsPanel.CellInstance cell) {

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String,BindingsPanel.OptionInstance> e :cell.getOptionInstanceMap().entrySet()) {
            String key = e.getKey();
            BindingsPanel.OptionInstance i = e.getValue();
            LOG.fine("checking option " + key);
            if (i != null && i.getValue() != null) {
                result.put(key, i.getValue());
                LOG.info("value for option: " + key + " -> " + i.getValue());
            }
        }
        return result;

    }

    /** Creates a VariableKey for this variable, looking up the producer cell from the notebook
     *
     * @param producer
     * @param varName
     * @return
     */
    protected VariableKey createVariableKey(BindingsPanel.CellInstance producer, String varName) {
        BindingsPanel.BindingInstance binding = producer.getBindingInstanceMap().get(varName);
        if (binding != null) {
            BindingsPanel.VariableInstance variable = binding.getVariableInstance();
            if (variable != null) {
                return new VariableKey(variable.getCellId(), variable.getVariableDefinition().getName());
            }
        }
        return null;
    }

    protected VariableKey createVariableKeyRequired(BindingsPanel.CellInstance cell, String varName) {
        VariableKey key = createVariableKey(cell, varName);
        if (key == null) {
            throw new IllegalStateException("Input variable " + varName + " not bound");
        }
        return key;
    }

    /**
     * Build the JobDefinition using the specified StepDefinition(s).
     *
     * @param cellExData
     * @param cell
     * @param steps
     * @return
     */
    protected StepsCellExecutorJobDefinition buildJobDefinition(CellExecutionData cellExData, BindingsPanel.CellInstance cell, StepDefinition... steps) {
        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition();
        jobdef.configureCellAndSteps(cellExData.getNotebookId(), cellExData.getEditableId(), cell.getId(), steps);
        return jobdef;
    }

    public static class MockExecutor1 extends CellExecutor {

        @Override
        public JobStatus execute(BindingsPanel.CellInstance cell, CellExecutionData cellExecutionData) throws Exception {
            // do something to set the output variable(s)
            // create a fake jobstatus
            return JobStatus.create(null /* JobDefinition */, "username", new Date(), null).withStatus(JobStatus.Status.COMPLETED, 0, 0, null);
        }
    }

}