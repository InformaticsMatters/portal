package portal.notebook.api;

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition;
import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import com.im.lac.types.MoleculeObject;
import org.squonk.client.JobStatusClient;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.notebook.api.*;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.TypeDescriptor;
import portal.SessionContext;
import portal.notebook.webapp.BindingsPanel;
import portal.notebook.webapp.StructureFieldEditorPanel;

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
    public JobStatus execute(CellInstance cell, CellExecutionData data) throws Exception {
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
    protected abstract JobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData);


    protected Map<String, Object> collectAllOptions(CellInstance cell) {

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String,OptionInstance> e :cell.getOptionInstanceMap().entrySet()) {
            String key = e.getKey();
            OptionInstance i = e.getValue();
            LOG.fine("checking option " + key);
            if (i != null && i.getValue() != null) {
                Object converted = getOptionValue(i);
                result.put(key, converted);
                LOG.info("value for option: " + key + " -> " + converted);
            }
        }
        return result;
    }

    protected Object getOptionValue(OptionInstance i) {
        TypeDescriptor td = i.getOptionDescriptor().getTypeDescriptor();
        // ----- start of huge hack --------------
        // This is a temp workaround until we find a way of asking the sketcher for the molecule in the required format.
        // The MoleculeTypeDescriptor defines what formats the services can handle, but the sketcher knows how to convert
        //   its internal format to the required format.
        // This needs to be generalised to support any cell/option type.
        if (td instanceof MoleculeTypeDescriptor) {
            MoleculeTypeDescriptor mtd = (MoleculeTypeDescriptor)td;
            Object value = i.getValue();
            if (value instanceof String) { // is it always a String?
                String mol = (String)value;
                String converted = StructureFieldEditorPanel.convertMolecule(mol, mtd.getFormats());
                LOG.info("Converted mol to: " + converted);
                return converted;
            }
        }
        // ----- end of huge hack --------------
        return i.getValue();
    }

    /** Creates a VariableKey for this variable, looking up the producer cell from the notebook
     *
     * @param producer
     * @param varName
     * @return
     */
    protected VariableKey createVariableKey(CellInstance producer, String varName) {
        BindingInstance binding = producer.getBindingInstanceMap().get(varName);
        if (binding != null) {
            VariableInstance variable = binding.getVariableInstance();
            if (variable != null) {
                return new VariableKey(variable.getCellId(), variable.getVariableDefinition().getName());
            }
        }
        return null;
    }

    protected VariableKey createVariableKeyRequired(CellInstance cell, String varName) {
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
    protected StepsCellExecutorJobDefinition buildJobDefinition(CellExecutionData cellExData, CellInstance cell, StepDefinition... steps) {
        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition();
        jobdef.configureCellAndSteps(cellExData.getNotebookId(), cellExData.getEditableId(), cell.getId(), steps);
        return jobdef;
    }

    public static class MockExecutor1 extends CellExecutor {

        @Override
        public JobStatus execute(CellInstance cell, CellExecutionData cellExecutionData) throws Exception {
            // do something to set the output variable(s)
            // create a fake jobstatus
            return JobStatus.create(null /* JobDefinition */, "username", new Date(), null).withStatus(JobStatus.Status.COMPLETED, 0, 0, null);
        }
    }

}