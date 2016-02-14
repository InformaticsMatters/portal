package portal.notebook.cells;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.notebook.api.VariableKey;
import portal.notebook.api.*;

import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class ServiceCellDefinition extends CellDefinition {

    private static final Logger LOG = Logger.getLogger(ServiceCellDefinition.class.getName());

    public static final String OPT_SERVICE_ENDPOINT = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_ENDPOINT;
    public static final String OPT_SERVICE_PARAMS = StepDefinitionConstants.ServiceExecutor.OPTION_SERVICE_PARAMS;
    public static final String OPT_SERVICE_PRESERVE_STRUCTURE = StepDefinitionConstants.ServiceExecutor.OPTION_PRESERVE_STRUCTURE;

    private ServiceDescriptor serviceDescriptor;

    public ServiceCellDefinition(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
        setExecutable(Boolean.TRUE);
        getBindingDefinitionList().add(new BindingDefinition(VAR_NAME_INPUT, VAR_DISPLAYNAME_INPUT, VariableType.DATASET));
        getOutputVariableDefinitionList().add(new VariableDefinition(VAR_NAME_OUTPUT, VAR_DISPLAYNAME_OUTPUT, VariableType.DATASET));
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new Executor();
    }

    @Override
    public String getName() {
        return serviceDescriptor.getName();
    }

    @Override
    public String getDescription() {
        return serviceDescriptor.getDescription();
    }

    @Override
    public Boolean getExecutable() {
        return true;
    }


    class Executor extends AbstractJobCellExecutor {

        @Override
        protected JobDefinition buildJobDefinition(CellExecutionData cellExecutionData) {

            LOG.info("Building JobDefinition for service " + serviceDescriptor.getAccessModes()[0].getExecutionEndpoint());

            NotebookInstance notebook = cellExecutionData.getNotebookInstance();
            CellInstance cell = notebook.findCellById(cellExecutionData.getCellId());
            VariableKey key = createVariableKey(notebook, cell, VAR_NAME_INPUT);

            StepDefinition step1 = new StepDefinition(StepDefinitionConstants.ServiceExecutor.CLASSNAME)
                    .withInputVariableMapping(StepDefinitionConstants.VARIABLE_INPUT_DATASET, key)
                    .withOutputVariableMapping(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, DefaultCellDefinitionRegistry.VAR_NAME_OUTPUT)
                    .withOption(OPT_SERVICE_ENDPOINT, serviceDescriptor.getAccessModes()[0].getExecutionEndpoint())
                    .withOption(OPT_SERVICE_PRESERVE_STRUCTURE, true) // TODO - this will need to be deined at the ServiceDescriptor level
                    .withOption(OPT_SERVICE_PARAMS, collectAllOptions(cell));

            return buildJobDefinition(cellExecutionData.getNotebookId(), cell, step1);
        }
    }
}
