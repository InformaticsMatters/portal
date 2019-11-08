package portal.notebook.api;

import org.keycloak.representations.AccessToken;
import org.squonk.core.client.StructureIOClient;
import org.squonk.io.IODescriptor;
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition;
import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.CellExecutorJobDefinition;
import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.StepsCellExecutorJobDefinition;
import org.squonk.client.JobStatusClient;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.notebook.api.VariableKey;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.TypeDescriptor;
import org.squonk.options.types.Structure;
import portal.SessionContext;
import portal.notebook.webapp.StructureFieldEditorPanel;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 16/01/16.
 */
public abstract class AbstractJobCellExecutor extends CellExecutor implements Serializable {

    private static final Logger LOG = Logger.getLogger(AbstractJobCellExecutor.class.getName());

    private static final JobStatusClient jobStatusClient = createCDIClient(JobStatusClient.class);
    private static final StructureIOClient structureIOClient = createCDIClient(StructureIOClient.class);

    @Override
    public JobStatus execute(CellInstance cell, CellExecutionData data) throws Exception {
        Instance<SessionContext> sessionContextInstance = CDI.current().select(SessionContext.class);
        SessionContext sessionContext = sessionContextInstance.get();
        String username = sessionContext.getLoggedInUserDetails().getUserid();
        String authHeader = sessionContext.getAuthorizationHeader();

        Integer workunits = null; // null means "I don't know", but we can probably get the number from the dataset metadata

        // create the job
        CellExecutorJobDefinition jobdef = buildJobDefinition(cell, data);
        // execute the job
//        JobStatusClient client = createJobStatusClient();
//        LOG.info("Executing job using client " + client);
        JobStatus status = jobStatusClient.submit(jobdef, username, authHeader, workunits);
        // job is now running. we can either poll the JobStatusRestClient for its status or listen on the message queue for updates
        return status;
    }

    protected static <T> T createCDIClient(Class<T> cls) {
        Instance<T> instance = CDI.current().select(cls);
        return instance.get();
    }

    /**
     * Build the JobDefinition that will be submitted for execution.
     */
    protected abstract CellExecutorJobDefinition buildJobDefinition(CellInstance cell, CellExecutionData cellExecutionData);


    protected Map<String, Object> collectAllOptions(CellInstance cell) {

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, OptionInstance> e : cell.getOptionInstanceMap().entrySet()) {
            String key = e.getKey();
            OptionInstance i = e.getValue();
            LOG.fine("checking option " + key);
            if (i != null && i.getValue() != null) {
                //LOG.info("option: " + key + " value: " + i.getValue());
                putOptionValue(result, i.getOptionDescriptor().getTypeDescriptor(), key, i.getValue());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected void putOptionValue(Map<String, Object> options, TypeDescriptor td, String key, Object value) {
        LOG.fine("Handling option " + key + " of type " + (value == null ? "null" : value.getClass().getName()));
        // ----- start of huge hack --------------
        // This is a temp workaround until we find a way of asking the sketcher for the molecule in the required format.
        // The MoleculeTypeDescriptor defines what formats the services can handle, but the sketcher knows how to convert
        //   its internal format to the required format.
        // This needs to be generalised to support any cell/option type.
        if (td instanceof MoleculeTypeDescriptor) {
            MoleculeTypeDescriptor mtd = (MoleculeTypeDescriptor) td;
            try {
                if (value instanceof String) {
                    String mol = (String) value;
                    Structure converted = structureIOClient.convertMol(mol, mtd.getFormats());
                    if (converted != null) {
                        LOG.info("Converted mol to: " + converted.getFormat());
                        //LOG.info("Putting value " + key + " = " + converted.getSource());
                        mtd.putOptionValue(options, key, converted);
                        return;
                    }
                } else if (value instanceof Structure) {
                    Structure struct = (Structure) value;
                    for (String format : mtd.getFormats()) {
                        if (struct.getFormat().startsWith(format)) {
                            // we already have a supported format
                            mtd.putOptionValue(options, key, struct);
                            return;
                        }
                    }
                    Structure converted = structureIOClient.convertMol(struct.getSource(), mtd.getFormats());
                    if (converted != null) {
                        mtd.putOptionValue(options, key, converted);
                        return;
                    }
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to generate structure of the required format", ex);
            }
            throw new IllegalStateException("Value not a String or Structure: " + value.getClass().getName());

            // ----- end of huge hack --------------
        } else {
            if (value != null) {
                td.putOptionValue(options, key, value);
            }
        }
    }

    /**
     * Creates a VariableKey for this variable, looking up the producer cell from the notebook
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
    protected StepsCellExecutorJobDefinition buildJobDefinition(CellExecutionData cellExData, CellInstance cell, IODescriptor[] inputs, IODescriptor[] outputs, StepDefinition... steps) {
        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition();
        jobdef.configureCellAndSteps(cellExData.getNotebookId(), cellExData.getEditableId(), cell.getId(), inputs, outputs, steps);
        return jobdef;
    }

    /**
     * Build the JobDefinition using the specified StepDefinition(s) where there is just one input and one output
     *
     * @param cellExData
     * @param cell
     * @param input
     * @param output
     * @param steps
     * @return
     */
    protected StepsCellExecutorJobDefinition buildJobDefinition(CellExecutionData cellExData, CellInstance cell, IODescriptor input, IODescriptor output, StepDefinition... steps) {
        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition();
        jobdef.configureCellAndSteps(cellExData.getNotebookId(), cellExData.getEditableId(), cell.getId(),
                input == null ? null : new IODescriptor[]{input},
                output == null ? null : new IODescriptor[]{output},
                steps);
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